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

import androidx.annotation.AnyThread
import androidx.compose.runtime.Stable
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.cache.MemoryCache.DefaultFactory
import com.github.panpf.sketch.decode.DecodeInterceptor
import com.github.panpf.sketch.decode.Decoder
import com.github.panpf.sketch.decode.internal.EngineDecodeInterceptor
import com.github.panpf.sketch.fetch.Base64UriFetcher
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.fetch.FileUriFetcher
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.http.HttpStack
import com.github.panpf.sketch.http.HurlStack
import com.github.panpf.sketch.request.Disposable
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.RequestInterceptor
import com.github.panpf.sketch.request.internal.EngineRequestInterceptor
import com.github.panpf.sketch.request.internal.GlobalImageOptionsRequestInterceptor
import com.github.panpf.sketch.request.internal.MemoryCacheRequestInterceptor
import com.github.panpf.sketch.request.internal.RequestExecutor
import com.github.panpf.sketch.target.TargetLifecycle
import com.github.panpf.sketch.transform.internal.TransformationDecodeInterceptor
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.SystemCallbacks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicBoolean

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
@Stable
class Sketch private constructor(options: Options) {
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate + CoroutineExceptionHandler { _, throwable ->
            logger.e("scope", throwable, "exception")
        }
    )
    private val requestExecutor = RequestExecutor()
    private val isShutdown = AtomicBoolean(false)

    /** Application Context */
    val context: PlatformContext = options.context

    /** Output log */
    val logger: Logger = options.logger

    /** Memory cache of previously loaded images */
    val memoryCache: MemoryCache by lazy { options.memoryCacheFactory.create(options.context) }

//    /** Reuse Bitmap */
//    val bitmapPool: BitmapPool  // TODO 4.0 no longer supports inBitmap

    /** Disk caching of http downloads images */
    val downloadCache: DiskCache by lazy { options.downloadCacheFactory.create(options.context) }

    /** Disk caching of transformed images */
    val resultCache: DiskCache by lazy { options.resultCacheFactory.create(options.context) }

    /** Execute HTTP request */
    val httpStack: HttpStack = options.httpStack

    /** Fill unset [ImageRequest] value */
    val globalImageOptions: ImageOptions? = options.globalImageOptions

    /** Register components that are required to perform [ImageRequest] and can be extended,
     * such as [Fetcher], [Decoder], [RequestInterceptor], [DecodeInterceptor] */
    val components: Components

    /** Monitor network connection and system status */
    val systemCallbacks = SystemCallbacks()

    /* Limit the number of concurrent network tasks, too many network tasks will cause network congestion */
    @OptIn(ExperimentalCoroutinesApi::class)
    val networkTaskDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)

    /* Limit the number of concurrent decoding tasks because too many concurrent BitmapFactory tasks can affect UI performance */
    @OptIn(ExperimentalCoroutinesApi::class)
    val decodeTaskDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)

    init {
        memoryCache.logger = logger
//        bitmapPool.logger = logger
        downloadCache.logger = logger
        resultCache.logger = logger

        val componentRegistry = options.componentRegistry
            .merged(platformComponents())
            .merged(defaultComponents())
            ?: ComponentRegistry.Builder().build()
        components = Components(this, componentRegistry)

        systemCallbacks.register(this)

        logger.d("Configuration") {
            buildString {
                append("\n").append("logger: $logger")
                append("\n").append("httpStack: $httpStack")
                append("\n").append("memoryCache: $memoryCache")
//                append("\n").append("bitmapPool: $bitmapPool")
                append("\n").append("downloadCache: $downloadCache")
                append("\n").append("resultCache: $resultCache")
                append("\n").append("fetchers: ${componentRegistry.fetcherFactoryList}")
                append("\n").append("decoders: ${componentRegistry.decoderFactoryList}")
                append("\n").append("requestInterceptors: ${componentRegistry.requestInterceptorList}")
                append("\n").append("decodeInterceptors: ${componentRegistry.decodeInterceptorList}")
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
        return getDisposable(request, job)
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
        return@coroutineScope getDisposable(request, job).job.await()
    }


    /**
     * Cancel any new and in progress requests, clear the [MemoryCache] and [BitmapPool], and close any open
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
//        bitmapPool.clear()
    }

    data class Options(
        val context: PlatformContext,
        val logger: Logger,
        val memoryCacheFactory: MemoryCache.Factory,
        val downloadCacheFactory: DiskCache.Factory,
        val resultCacheFactory: DiskCache.Factory,
        val httpStack: HttpStack,
        val componentRegistry: ComponentRegistry?,
        val globalImageOptions: ImageOptions?,
    )

    class Builder constructor(private val context: PlatformContext) {

        private var logger: Logger? = null
        private var memoryCacheFactory: MemoryCache.Factory? = null
        private var downloadCacheFactory: DiskCache.Factory? = null
        private var resultCacheFactory: DiskCache.Factory? = null

        //        private var bitmapPool: BitmapPool? = null
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
         * Set the [MemoryCache]
         */
        fun memoryCache(memoryCache: MemoryCache.Factory?): Builder = apply {
            this.memoryCacheFactory = memoryCache
        }

        /**
         * Set the [MemoryCache]
         */
        fun memoryCache(memoryCache: MemoryCache): Builder = apply {
            this.memoryCacheFactory = MemoryCache.FixedFactory(memoryCache)
        }

        /**
         * Set the [MemoryCache]
         */
        fun memoryCache(options: MemoryCache.Options): Builder = apply {
            this.memoryCacheFactory = MemoryCache.OptionsFactory(options)
        }

        /**
         * Set the [MemoryCache]
         */
        fun memoryCache(initializer: (PlatformContext) -> MemoryCache): Builder = apply {
            this.memoryCacheFactory = MemoryCache.LazyFactory(initializer)
        }

        /**
         * Set the [MemoryCache]
         */
        fun memoryCache(initializer: MemoryCache.LazyOptions): Builder = apply {
            this.memoryCacheFactory = MemoryCache.LazyOptionsFactory(initializer)
        }

        /**
         * Set the [DiskCache] for download cache
         */
        fun downloadCache(diskCache: DiskCache.Factory?): Builder = apply {
            this.downloadCacheFactory = diskCache
        }

        /**
         * Set the [DiskCache] for download cache
         */
        fun downloadCache(diskCache: DiskCache): Builder = apply {
            this.downloadCacheFactory = DiskCache.FixedFactory(diskCache)
        }

        /**
         * Set the [DiskCache] for download cache
         */
        fun downloadCache(options: DiskCache.Options): Builder = apply {
            this.downloadCacheFactory = DiskCache.OptionsFactory(DiskCache.Type.DOWNLOAD, options)
        }

        /**
         * Set the [DiskCache] for download cache
         */
        fun downloadCache(initializer: (PlatformContext) -> DiskCache): Builder = apply {
            this.downloadCacheFactory = DiskCache.LazyFactory(initializer)
        }

        /**
         * Set the [DiskCache] for download cache
         */
        fun downloadCache(initializer: DiskCache.LazyOptions): Builder = apply {
            this.downloadCacheFactory =
                DiskCache.LazyOptionsFactory(DiskCache.Type.DOWNLOAD, initializer)
        }

        /**
         * Set the [DiskCache] for result cache
         */
        fun resultCache(diskCache: DiskCache.Factory?): Builder = apply {
            this.resultCacheFactory = diskCache
        }

        /**
         * Set the [DiskCache] for result cache
         */
        fun resultCache(diskCache: DiskCache): Builder = apply {
            this.resultCacheFactory = DiskCache.FixedFactory(diskCache)
        }

        /**
         * Set the [DiskCache] for result cache
         */
        fun resultCache(options: DiskCache.Options): Builder = apply {
            this.resultCacheFactory = DiskCache.OptionsFactory(DiskCache.Type.RESULT, options)
        }

        /**
         * Set the [DiskCache] for result cache
         */
        fun resultCache(initializer: (PlatformContext) -> DiskCache): Builder = apply {
            this.resultCacheFactory = DiskCache.LazyFactory(initializer)
        }

        /**
         * Set the [DiskCache] for result cache
         */
        fun resultCache(initializer: DiskCache.LazyOptions): Builder = apply {
            this.resultCacheFactory =
                DiskCache.LazyOptionsFactory(DiskCache.Type.RESULT, initializer)
        }

//        /**
//         * Set the [BitmapPool]
//         */
//        fun bitmapPool(bitmapPool: BitmapPool?): Builder = apply {
//            this.bitmapPool = bitmapPool
//        }

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
            val options = Options(
                context = context,
                logger = this.logger ?: Logger(),
                memoryCacheFactory = memoryCacheFactory ?: DefaultFactory(),
                downloadCacheFactory = downloadCacheFactory
                    ?: DiskCache.DefaultFactory(DiskCache.Type.DOWNLOAD),
                resultCacheFactory = resultCacheFactory
                    ?: DiskCache.DefaultFactory(DiskCache.Type.RESULT),
//            bitmapPool = bitmapPool,
                httpStack = httpStack ?: HurlStack.Builder().build(),
                componentRegistry = componentRegistry,
                globalImageOptions = globalImageOptions,
            )
            return Sketch(options)
        }
    }
}

internal expect fun platformComponents(): ComponentRegistry

internal fun defaultComponents(): ComponentRegistry {
    return ComponentRegistry.Builder().apply {
        addFetcher(HttpUriFetcher.Factory())
        addFetcher(FileUriFetcher.Factory())
        addFetcher(Base64UriFetcher.Factory())

        addRequestInterceptor(GlobalImageOptionsRequestInterceptor())
        addRequestInterceptor(MemoryCacheRequestInterceptor())
        addRequestInterceptor(EngineRequestInterceptor())

        addDecodeInterceptor(TransformationDecodeInterceptor())
        addDecodeInterceptor(EngineDecodeInterceptor())
    }.build()
}

internal expect fun getDisposable(
    request: ImageRequest,
    job: Deferred<ImageResult>,
): Disposable
