/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.sketch

import com.github.panpf.sketch.annotation.AnyThread
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.cache.internal.ResultCacheDecodeInterceptor
import com.github.panpf.sketch.decode.DecodeInterceptor
import com.github.panpf.sketch.decode.Decoder
import com.github.panpf.sketch.decode.internal.EngineDecodeInterceptor
import com.github.panpf.sketch.fetch.Base64UriFetcher
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.fetch.FileUriFetcher
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.http.HttpStack
import com.github.panpf.sketch.request.Disposable
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.OneShotDisposable
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.sketch.request.internal.EngineRequestInterceptor
import com.github.panpf.sketch.request.internal.MemoryCacheRequestInterceptor
import com.github.panpf.sketch.request.internal.PlaceholderRequestInterceptor
import com.github.panpf.sketch.request.internal.RequestExecutor
import com.github.panpf.sketch.target.TargetLifecycle
import com.github.panpf.sketch.transform.internal.TransformationDecodeInterceptor
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.SystemCallbacks
import com.github.panpf.sketch.util.application
import com.github.panpf.sketch.util.defaultFileSystem
import com.github.panpf.sketch.util.ioCoroutineDispatcher
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import okio.FileSystem

/**
 * A service class that performs an [ImageRequest] to load an image.
 *
 * Sketch is responsible for handling data acquisition, image decoding, image conversion,
 * processing cache, request management, memory management and other functions.
 *
 * You just need to create an instance of the ImageRequest subclass and pass it to the [enqueue] or [execute] method for execution.
 *
 * Sketch is designed to be sharable and works best when the same instance is used throughout the
 * application via the built-in extension function `Context.sketch`
 */
//@Stable // TODO
class Sketch private constructor(options: Options) {
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate + CoroutineExceptionHandler { _, throwable ->
            logger.e(throwable, "CoroutineScope. An uncaught exception")
        }
    )
    private val requestExecutor = RequestExecutor()
    private val isShutdown = atomic(false)

    /** Application Context */
    val context: PlatformContext = options.context.application

    /** Output log */
    val logger: Logger = options.logger

    /** File system */
    val fileSystem: FileSystem = options.fileSystem

    /** Memory cache of previously loaded images */
    val memoryCache: MemoryCache by options.memoryCacheLazy

    /** Disk caching of http downloads images */
    val downloadCache: DiskCache by options.downloadCacheLazy

    /** Disk caching of transformed images */
    val resultCache: DiskCache by options.resultCacheLazy

    /** Execute HTTP request */
    val httpStack: HttpStack = options.httpStack

    /** Fill unset [ImageRequest] value */
    val globalImageOptions: ImageOptions? = options.globalImageOptions

    /** Register components that are required to perform [ImageRequest] and can be extended,
     * such as [Fetcher], [Decoder], [RequestInterceptor], [DecodeInterceptor] */
    val components: Components

    /** Monitor network connection and system status */
    val systemCallbacks = SystemCallbacks(this)

    /* Limit the number of concurrent network tasks, too many network tasks will cause network congestion */
    @OptIn(ExperimentalCoroutinesApi::class)
    val networkTaskDispatcher: CoroutineDispatcher = ioCoroutineDispatcher().limitedParallelism(10)

    /* Limit the number of concurrent decoding tasks because too many concurrent BitmapFactory tasks can affect UI performance */
    @OptIn(ExperimentalCoroutinesApi::class)
    val decodeTaskDispatcher: CoroutineDispatcher = ioCoroutineDispatcher().limitedParallelism(4)

    init {
        val componentRegistry = options.componentRegistry
            .merged(platformComponents())
            .merged(defaultComponents())
            ?: ComponentRegistry.Builder().build()
        components = Components(this, componentRegistry)

        systemCallbacks.register()

        logger.d {
            buildString {
                append("Configuration. ")
                appendLine().append("logger: $logger")
                appendLine().append("httpStack: $httpStack")
                appendLine().append("memoryCache: $memoryCache")
                appendLine().append("resultCache: $resultCache")
                appendLine().append("downloadCache: $downloadCache")
                appendLine().append("fetchers: ${componentRegistry.fetcherFactoryList}")
                appendLine().append("decoders: ${componentRegistry.decoderFactoryList}")
                appendLine().append("requestInterceptors: ${componentRegistry.requestInterceptorList}")
                appendLine().append("decodeInterceptors: ${componentRegistry.decodeInterceptorList}")
            }
        }
    }


    /**
     * Execute the ImageRequest asynchronously.
     *
     * Note: The request will not start executing until TargetLifecycle state is STARTED
     * reaches [TargetLifecycle.State.STARTED] state and View is attached to window
     *
     * @return A [Disposable] which can be used to cancel or check the status of the request.
     */
    @AnyThread
    fun enqueue(request: ImageRequest): Disposable {
        val job = scope.async {
            requestExecutor.execute(this@Sketch, request, enqueue = true)
        }
        // Update the current request attached to the view and return a new disposable.
        val requestManager = request.target?.getRequestManager()
        return requestManager?.getDisposable(job) ?: OneShotDisposable(job)
    }

    /**
     * Execute the ImageRequest synchronously in the current coroutine scope.
     *
     * Note: The request will not start executing until TargetLifecycle state is STARTED
     * reaches [TargetLifecycle.State.STARTED] state and View is attached to window
     *
     * @return A [ImageResult.Success] if the request completes successfully. Else, returns an [ImageResult.Error].
     */
    suspend fun execute(request: ImageRequest): ImageResult = coroutineScope {
        val job = async(Dispatchers.Main.immediate) {
            requestExecutor.execute(this@Sketch, request, enqueue = false)
        }
        // Update the current request attached to the view and await the result.
        val requestManager = request.target?.getRequestManager()
        val disposable = requestManager?.getDisposable(job) ?: OneShotDisposable(job)
        return@coroutineScope disposable.job.await()
    }


    /**
     * Cancel any new and in progress requests, clear the [MemoryCache] and [DiskCache], and close any open
     * system resources.
     *
     * Shutting down an image loader is optional.
     */
    fun shutdown() {
        if (isShutdown.getAndSet(true)) return
        scope.cancel()
        systemCallbacks.shutdown()
        memoryCache.clear()
        downloadCache.close()
        resultCache.close()
    }

    data class Options(
        val context: PlatformContext,
        val logger: Logger,
        val fileSystem: FileSystem,
        val memoryCacheLazy: Lazy<MemoryCache>,
        val downloadCacheLazy: Lazy<DiskCache>,
        val resultCacheLazy: Lazy<DiskCache>,
        val httpStack: HttpStack,
        val componentRegistry: ComponentRegistry?,
        val globalImageOptions: ImageOptions?,
    )

    class Builder constructor(private val context: PlatformContext) {

        private var logger: Logger? = null
        private var fileSystem: FileSystem? = null
        private var memoryCacheLazy: Lazy<MemoryCache>? = null
        private var downloadCacheLazy: Lazy<DiskCache>? = null
        private var downloadCacheOptionsLazy: Lazy<DiskCache.Options>? = null
        private var resultCacheLazy: Lazy<DiskCache>? = null
        private var resultCacheOptionsLazy: Lazy<DiskCache.Options>? = null

        private var componentRegistry: ComponentRegistry? = null
        private var httpStack: HttpStack? = null
        private var globalImageOptions: ImageOptions? = null

        /**
         * Set the [Logger] to write logs to.
         */
        fun logger(logger: Logger?): Builder = apply {
            this.logger = logger
        }

        /**
         * Set the [FileSystem] used for disk io.
         */
        fun fileSystem(fileSystem: FileSystem?): Builder = apply {
            this.fileSystem = fileSystem
        }

        /**
         * Set the [MemoryCache]
         */
        fun memoryCache(memoryCache: MemoryCache?): Builder = apply {
            this.memoryCacheLazy = memoryCache?.let { lazyOf(it) }
        }

        /**
         * Set the [MemoryCache]
         */
        fun memoryCache(initializer: () -> MemoryCache): Builder = apply {
            this.memoryCacheLazy = lazy { initializer() }
        }

        /**
         * Set the download [DiskCache]
         */
        fun downloadCache(diskCache: DiskCache?): Builder = apply {
            this.downloadCacheLazy = diskCache?.let { lazyOf(it) }
            this.downloadCacheOptionsLazy = null
        }

        /**
         * Set the download [DiskCache]
         */
        fun downloadCache(initializer: () -> DiskCache): Builder = apply {
            this.downloadCacheLazy = lazy { initializer() }
            this.downloadCacheOptionsLazy = null
        }

        /**
         * Set the download [DiskCache]
         */
        fun downloadCacheOptions(options: DiskCache.Options): Builder = apply {
            this.downloadCacheOptionsLazy = lazyOf(options)
            this.downloadCacheLazy = null
        }

        /**
         * Set the download [DiskCache]
         */
        fun downloadCacheOptions(initializer: () -> DiskCache.Options): Builder = apply {
            this.downloadCacheOptionsLazy = lazy { initializer() }
            this.downloadCacheLazy = null
        }

        /**
         * Set the result [DiskCache]
         */
        fun resultCache(diskCache: DiskCache?): Builder = apply {
            this.resultCacheLazy = diskCache?.let { lazyOf(it) }
        }

        /**
         * Set the result [DiskCache]
         */
        fun resultCache(initializer: () -> DiskCache): Builder = apply {
            this.resultCacheLazy = lazy { initializer() }
        }

        /**
         * Set the result [DiskCache]
         */
        fun resultCacheOptions(options: DiskCache.Options): Builder = apply {
            this.resultCacheOptionsLazy = lazyOf(options)
            this.resultCacheLazy = null
        }

        /**
         * Set the result [DiskCache]
         */
        fun resultCacheOptions(initializer: () -> DiskCache.Options): Builder = apply {
            this.resultCacheOptionsLazy = lazy { initializer() }
            this.resultCacheLazy = null
        }

        /**
         * Set the [ComponentRegistry]
         */
        fun components(components: ComponentRegistry?): Builder = apply {
            this.componentRegistry = components
        }

        /**
         * Build and set the [ComponentRegistry]
         */
        fun components(configBlock: (ComponentRegistry.Builder.() -> Unit)): Builder =
            components(ComponentRegistry.Builder().apply(configBlock).build())

        /**
         * Set the [HttpStack] used for network requests.
         */
        fun httpStack(httpStack: HttpStack?): Builder = apply {
            this.httpStack = httpStack
        }

        /**
         * Set an [ImageOptions], fill unset [ImageRequest] value
         */
        fun globalImageOptions(globalImageOptions: ImageOptions?): Builder = apply {
            this.globalImageOptions = globalImageOptions
        }

        fun build(): Sketch {
            val finalFileSystem = fileSystem ?: defaultFileSystem()
            val options = Options(
                context = context,
                logger = this.logger ?: Logger(),
                fileSystem = finalFileSystem,
                memoryCacheLazy = memoryCacheLazy ?: lazy { MemoryCache.Builder(context).build() },
                downloadCacheLazy = downloadCacheLazy ?: lazy {
                    val options = downloadCacheOptionsLazy?.value
                    DiskCache.DownloadBuilder(context, finalFileSystem).apply {
                        if (options != null) {
                            options(options)
                        }
                    }.build()
                },
                resultCacheLazy = resultCacheLazy ?: lazy {
                    val options = resultCacheOptionsLazy?.value
                    DiskCache.ResultBuilder(context, finalFileSystem).apply {
                        if (options != null) {
                            options(options)
                        }
                    }.build()
                },
                httpStack = httpStack ?: defaultHttpStack(),
                componentRegistry = componentRegistry,
                globalImageOptions = globalImageOptions,
            )
            return Sketch(options)
        }
    }
}

internal expect fun platformComponents(): ComponentRegistry
internal expect fun defaultHttpStack(): HttpStack

internal fun defaultComponents(): ComponentRegistry {
    return ComponentRegistry.Builder().apply {
        addFetcher(HttpUriFetcher.Factory())
        addFetcher(FileUriFetcher.Factory())
        addFetcher(Base64UriFetcher.Factory())

        addRequestInterceptor(MemoryCacheRequestInterceptor())
        addRequestInterceptor(PlaceholderRequestInterceptor())
        addRequestInterceptor(EngineRequestInterceptor())

        addDecodeInterceptor(ResultCacheDecodeInterceptor())
        addDecodeInterceptor(TransformationDecodeInterceptor())
        addDecodeInterceptor(EngineDecodeInterceptor())
    }.build()
}