package com.github.panpf.sketch.request

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.BitmapPool
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.cache.CachePolicy.ENABLED
import com.github.panpf.sketch.decode.BitmapConfig
import com.github.panpf.sketch.drawable.internal.CrossfadeDrawable
import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.request.RequestDepth.NETWORK
import com.github.panpf.sketch.request.internal.CombinedListener
import com.github.panpf.sketch.request.internal.CombinedProgressListener
import com.github.panpf.sketch.request.internal.newCacheKey
import com.github.panpf.sketch.request.internal.newKey
import com.github.panpf.sketch.resize.DisplaySizeResolver
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.resize.Precision.EXACTLY
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.resize.PrecisionDecider
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.resize.Scale
import com.github.panpf.sketch.resize.Scale.CENTER_CROP
import com.github.panpf.sketch.resize.Scale.END_CROP
import com.github.panpf.sketch.resize.Scale.START_CROP
import com.github.panpf.sketch.resize.ScaleDecider
import com.github.panpf.sketch.resize.SizeResolver
import com.github.panpf.sketch.resize.ViewSizeResolver
import com.github.panpf.sketch.resize.fixedPrecision
import com.github.panpf.sketch.resize.fixedScale
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.stateimage.ErrorStateImage
import com.github.panpf.sketch.stateimage.StateImage
import com.github.panpf.sketch.target.ListenerProvider
import com.github.panpf.sketch.target.Target
import com.github.panpf.sketch.target.ViewTarget
import com.github.panpf.sketch.transform.Transformation
import com.github.panpf.sketch.transition.Transition
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.asOrNull
import com.github.panpf.sketch.util.getLifecycle

interface ImageRequest {

    companion object {
        const val REQUEST_DEPTH_FROM = "sketch#requestDepthFrom"
    }

    val sketch: Sketch
    val context: Context    // todo Request 不持有 Sketch
    val uriString: String
    val listener: Listener<ImageRequest, ImageResult.Success, ImageResult.Error>?
    val parameters: Parameters?
    val depth: RequestDepth
    val httpHeaders: HttpHeaders?
    val downloadDiskCachePolicy: CachePolicy
    val progressListener: ProgressListener<ImageRequest>?

    /**
     * Specify [Bitmap.Config] to use when creating the bitmap.
     * KITKAT and above [Bitmap.Config.ARGB_4444] will be forced to be replaced with [Bitmap.Config.ARGB_8888].
     *
     * Applied to [android.graphics.BitmapFactory.Options.inPreferredConfig]
     */
    val bitmapConfig: BitmapConfig?

    @get:RequiresApi(VERSION_CODES.O)
    val colorSpace: ColorSpace?

    /**
     * From Android N (API 24), this is ignored.  The output will always be high quality.
     *
     * In {@link android.os.Build.VERSION_CODES#M} and below, if
     * inPreferQualityOverSpeed is set to true, the decoder will try to
     * decode the reconstructed image to a higher quality even at the
     * expense of the decoding speed. Currently the field only affects JPEG
     * decode, in the case of which a more accurate, but slightly slower,
     * IDCT method will be used instead.
     *
     * Applied to [android.graphics.BitmapFactory.Options.inPreferQualityOverSpeed]
     */
    @Deprecated("From Android N (API 24), this is ignored. The output will always be high quality.")
    val preferQualityOverSpeed: Boolean

    /** The size of the desired bitmap */
    val resize: Resize?
    val resizeSizeResolver: SizeResolver
    val resizePrecisionDecider: PrecisionDecider
    val resizeScaleDecider: ScaleDecider

    /** The list of [Transformation]s to be applied to this request. */
    val transformations: List<Transformation>?

    /** Disabled reuse of Bitmap from [BitmapPool] */
    val disabledReuseBitmap: Boolean

    /** Ignore exif orientation */
    val ignoreExifOrientation: Boolean

    /** @see com.github.panpf.sketch.decode.internal.BitmapResultDiskCacheDecodeInterceptor */
    val bitmapResultDiskCachePolicy: CachePolicy
    val target: Target?
    val lifecycle: Lifecycle

    val disabledAnimatedImage: Boolean
    val bitmapMemoryCachePolicy: CachePolicy
    val placeholderImage: StateImage?
    val errorImage: StateImage?
    val transition: Transition.Factory?
    val resizeApplyToDrawable: Boolean

    val definedOptions: ImageOptions
    val globalOptions: ImageOptions?

    val uri: Uri

    val key: String

    /** Used to cache bitmaps in memory and on disk */
    val cacheKey: String

    val depthFrom: String?
        get() = parameters?.value(REQUEST_DEPTH_FROM)

    val downloadDiskCacheKey: String
        get() = uriString

    abstract class BaseImageRequest : ImageRequest {
        override val uri: Uri by lazy { Uri.parse(uriString) }

        override val key: String by lazy { newKey() }

        /** Used to cache bitmaps in memory and on disk */
        override val cacheKey: String by lazy { newCacheKey() }

        override fun toString(): String = key
    }

    fun newBuilder(
        configBlock: (Builder.() -> Unit)? = null
    ): Builder

    fun newRequest(
        configBlock: (Builder.() -> Unit)? = null
    ): ImageRequest

    abstract class Builder {
        private val sketch: Sketch
        private val context: Context
        private val uriString: String
        private var listener: Listener<ImageRequest, ImageResult.Success, ImageResult.Error>? = null
        private var progressListener: ProgressListener<ImageRequest>? = null
        private var target: Target? = null
        private var targetViewOptions: ImageOptions? = null
        private var lifecycle: Lifecycle? = null
        private var globalOptions: ImageOptions? = null
        private val definedOptionsBuilder: ImageOptions.Builder

        constructor(context: Context, uriString: String?) {
            this.context = context
            this.sketch = context.sketch
            this.uriString = uriString.orEmpty()
            this.definedOptionsBuilder = ImageOptionsBuilder()
        }

        internal constructor(request: ImageRequest) {
            this.context = request.context
            this.sketch = request.sketch
            this.uriString = request.uriString
            this.listener = request.listener
                .asOrNull<CombinedListener<ImageRequest, ImageResult.Success, ImageResult.Error>>()
                ?.fromBuilderListener
                ?: request.listener
            this.progressListener = request.progressListener
                .asOrNull<CombinedProgressListener<ImageRequest>>()
                ?.fromBuilderProgressListener
                ?: request.progressListener
            this.target = request.target
            this.lifecycle = request.lifecycle
            this.globalOptions = request.globalOptions
            this.definedOptionsBuilder = request.definedOptions.newBuilder()
        }

        internal fun listener(listener: Listener<ImageRequest, ImageResult.Success, ImageResult.Error>?): Builder =
            apply {
                this.listener = listener
            }

        /**
         * Convenience function to create and set the [Listener].
         */
        internal inline fun listener(
            crossinline onStart: (request: ImageRequest) -> Unit = {},
            crossinline onCancel: (request: ImageRequest) -> Unit = {},
            crossinline onError: (request: ImageRequest, result: ImageResult.Error) -> Unit = { _, _ -> },
            crossinline onSuccess: (request: ImageRequest, result: ImageResult.Success) -> Unit = { _, _ -> }
        ): Builder =
            listener(object : Listener<ImageRequest, ImageResult.Success, ImageResult.Error> {
                override fun onStart(request: ImageRequest) = onStart(request)
                override fun onCancel(request: ImageRequest) = onCancel(request)
                override fun onError(request: ImageRequest, result: ImageResult.Error) =
                    onError(request, result)

                override fun onSuccess(request: ImageRequest, result: ImageResult.Success) =
                    onSuccess(request, result)
            })

        internal fun progressListener(
            progressListener: ProgressListener<ImageRequest>?
        ): Builder = apply {
            this.progressListener = progressListener
        }

        open fun lifecycle(lifecycle: Lifecycle?): Builder = apply {
            this.lifecycle = lifecycle
        }

        internal fun target(target: Target?): Builder = apply {
            this.target = target
            this.targetViewOptions = target.asOrNull<ViewTarget<*>>()
                ?.view.asOrNull<ImageOptionsProvider>()
                ?.displayImageOptions
        }


        open fun depth(depth: RequestDepth?): Builder = apply {
            definedOptionsBuilder.depth(depth)
        }

        open fun depthFrom(from: String?): Builder = apply {
            definedOptionsBuilder.depthFrom(from)
        }

        open fun parameters(parameters: Parameters?): Builder = apply {
            definedOptionsBuilder.parameters(parameters)
        }

        /**
         * Set a parameter for this request.
         *
         * @see Parameters.Builder.set
         */
        @JvmOverloads
        open fun setParameter(
            key: String, value: Any?, cacheKey: String? = value?.toString()
        ): Builder = apply {
            definedOptionsBuilder.setParameter(key, value, cacheKey)
        }

        /**
         * Remove a parameter from this request.
         *
         * @see Parameters.Builder.remove
         */
        open fun removeParameter(key: String): Builder = apply {
            definedOptionsBuilder.removeParameter(key)
        }

        open fun httpHeaders(httpHeaders: HttpHeaders?): Builder = apply {
            definedOptionsBuilder.httpHeaders(httpHeaders)
        }

        /**
         * Add a header for any network operations performed by this request.
         */
        open fun addHttpHeader(name: String, value: String): Builder = apply {
            definedOptionsBuilder.addHttpHeader(name, value)
        }

        /**
         * Set a header for any network operations performed by this request.
         */
        open fun setHttpHeader(name: String, value: String): Builder = apply {
            definedOptionsBuilder.setHttpHeader(name, value)
        }

        /**
         * Remove all network headers with the key [name].
         */
        open fun removeHttpHeader(name: String): Builder = apply {
            definedOptionsBuilder.removeHttpHeader(name)
        }

        open fun downloadDiskCachePolicy(cachePolicy: CachePolicy?): Builder = apply {
            definedOptionsBuilder.downloadDiskCachePolicy(cachePolicy)
        }


        open fun bitmapConfig(bitmapConfig: BitmapConfig?): Builder = apply {
            definedOptionsBuilder.bitmapConfig(bitmapConfig)
        }

        open fun bitmapConfig(bitmapConfig: Bitmap.Config?): Builder = apply {
            definedOptionsBuilder.bitmapConfig(bitmapConfig)
        }

        open fun lowQualityBitmapConfig(): Builder = apply {
            definedOptionsBuilder.lowQualityBitmapConfig()
        }

        open fun middenQualityBitmapConfig(): Builder = apply {
            definedOptionsBuilder.middenQualityBitmapConfig()
        }

        open fun highQualityBitmapConfig(): Builder = apply {
            definedOptionsBuilder.highQualityBitmapConfig()
        }

        @RequiresApi(VERSION_CODES.O)
        open fun colorSpace(colorSpace: ColorSpace?): Builder = apply {
            definedOptionsBuilder.colorSpace(colorSpace)
        }

        /**
         * From Android N (API 24), this is ignored.  The output will always be high quality.
         *
         * In {@link android.os.Build.VERSION_CODES#M} and below, if
         * inPreferQualityOverSpeed is set to true, the decoder will try to
         * decode the reconstructed image to a higher quality even at the
         * expense of the decoding speed. Currently the field only affects JPEG
         * decode, in the case of which a more accurate, but slightly slower,
         * IDCT method will be used instead.
         *
         * Applied to [android.graphics.BitmapFactory.Options.inPreferQualityOverSpeed]
         */
        @Deprecated("From Android N (API 24), this is ignored.  The output will always be high quality.")
        open fun preferQualityOverSpeed(inPreferQualityOverSpeed: Boolean?): Builder = apply {
            @Suppress("DEPRECATION")
            definedOptionsBuilder.preferQualityOverSpeed(inPreferQualityOverSpeed)
        }

        open fun resize(resize: Resize?): Builder = apply {
            definedOptionsBuilder.resize(resize)
        }

        open fun resizeSize(sizeResolver: SizeResolver?): Builder = apply {
            definedOptionsBuilder.resizeSize(sizeResolver)
        }

        open fun resizeSize(size: Size?): Builder = apply {
            definedOptionsBuilder.resizeSize(size)
        }

        open fun resizeSize(@Px width: Int, @Px height: Int): Builder = apply {
            definedOptionsBuilder.resizeSize(width, height)
        }

        open fun resizePrecision(precisionDecider: PrecisionDecider?): Builder = apply {
            definedOptionsBuilder.resizePrecision(precisionDecider)
        }

        open fun resizePrecision(precision: Precision): Builder = apply {
            definedOptionsBuilder.resizePrecision(precision)
        }

        open fun resizeScale(scaleDecider: ScaleDecider?): Builder = apply {
            definedOptionsBuilder.resizeScale(scaleDecider)
        }

        open fun resizeScale(scale: Scale): Builder = apply {
            definedOptionsBuilder.resizeScale(scale)
        }

        open fun transformations(transformations: List<Transformation>?): Builder = apply {
            definedOptionsBuilder.transformations(transformations)
        }

        open fun transformations(vararg transformations: Transformation): Builder = apply {
            definedOptionsBuilder.transformations(transformations.toList())
        }

        open fun addTransformations(transformations: List<Transformation>): Builder = apply {
            definedOptionsBuilder.addTransformations(transformations)
        }

        open fun addTransformations(vararg transformations: Transformation): Builder = apply {
            definedOptionsBuilder.addTransformations(transformations.toList())
        }

        open fun removeTransformations(transformations: List<Transformation>): Builder = apply {
            definedOptionsBuilder.removeTransformations(transformations)
        }

        open fun removeTransformations(vararg transformations: Transformation): Builder = apply {
            definedOptionsBuilder.removeTransformations(transformations.toList())
        }

        open fun disabledReuseBitmap(disabled: Boolean? = true): Builder = apply {
            definedOptionsBuilder.disabledReuseBitmap(disabled)
        }

        open fun ignoreExifOrientation(ignore: Boolean? = true): Builder = apply {
            definedOptionsBuilder.ignoreExifOrientation(ignore)
        }

        open fun bitmapResultDiskCachePolicy(cachePolicy: CachePolicy?): Builder = apply {
            definedOptionsBuilder.bitmapResultDiskCachePolicy(cachePolicy)
        }


        open fun disabledAnimatedImage(disabled: Boolean? = true): Builder = apply {
            definedOptionsBuilder.disabledAnimatedImage(disabled)
        }

        open fun placeholder(stateImage: StateImage?): Builder = apply {
            definedOptionsBuilder.placeholder(stateImage)
        }

        open fun placeholder(drawable: Drawable?): Builder = apply {
            definedOptionsBuilder.placeholder(drawable)
        }

        open fun placeholder(@DrawableRes drawableResId: Int?): Builder = apply {
            definedOptionsBuilder.placeholder(drawableResId)
        }

        open fun error(
            stateImage: StateImage?, configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            definedOptionsBuilder.error(stateImage, configBlock)
        }

        open fun error(
            drawable: Drawable?, configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            definedOptionsBuilder.error(drawable, configBlock)
        }

        open fun error(
            drawableResId: Int?, configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            definedOptionsBuilder.error(drawableResId, configBlock)
        }

        open fun transition(transition: Transition.Factory?): Builder = apply {
            definedOptionsBuilder.transition(transition)
        }

        open fun crossfade(
            durationMillis: Int = CrossfadeDrawable.DEFAULT_DURATION,
            preferExactIntrinsicSize: Boolean = false
        ): Builder = apply {
            definedOptionsBuilder.crossfade(durationMillis, preferExactIntrinsicSize)
        }

        open fun resizeApplyToDrawable(resizeApplyToDrawable: Boolean? = true): Builder = apply {
            definedOptionsBuilder.resizeApplyToDrawable(resizeApplyToDrawable)
        }

        open fun bitmapMemoryCachePolicy(cachePolicy: CachePolicy?): Builder = apply {
            definedOptionsBuilder.bitmapMemoryCachePolicy(cachePolicy)
        }


        open fun merge(options: ImageOptions?): Builder = apply {
            definedOptionsBuilder.merge(options)
        }

        open fun global(options: ImageOptions?): Builder = apply {
            this.globalOptions = options
        }


        @SuppressLint("NewApi")
        open fun build(): ImageRequest {
            val listener = combinationListener()
            val progressListener = combinationProgressListener()
            val lifecycle = lifecycle ?: resolveLifecycle() ?: GlobalLifecycle
            definedOptionsBuilder.merge(targetViewOptions)
            val definedOptions = definedOptionsBuilder.build()
            val finalOptions = definedOptionsBuilder.merge(globalOptions).build()
            val depth = finalOptions.depth ?: NETWORK
            val parameters = finalOptions.parameters
            val httpHeaders = finalOptions.httpHeaders
            val downloadDiskCachePolicy = finalOptions.downloadDiskCachePolicy ?: ENABLED
            val bitmapResultDiskCachePolicy = finalOptions.bitmapResultDiskCachePolicy ?: ENABLED
            val bitmapConfig = finalOptions.bitmapConfig
            val colorSpace =
                if (VERSION.SDK_INT >= VERSION_CODES.O) finalOptions.colorSpace else null
            @Suppress("DEPRECATION") val preferQualityOverSpeed =
                if (VERSION.SDK_INT < VERSION_CODES.N)
                    finalOptions.preferQualityOverSpeed ?: false else false
            val resize = finalOptions.resize
            var resolvedResizeSize = false
            val resizeSizeResolver = finalOptions.resizeSizeResolver
                ?: resolveResizeSizeResolver().apply { resolvedResizeSize = true }
            val resizePrecisionDecider = finalOptions.resizePrecisionDecider
                ?: fixedPrecision(if (resize != null || !resolvedResizeSize) EXACTLY else LESS_PIXELS)
            val resizeScaleDecider = finalOptions.resizeScaleDecider
                ?: fixedScale(resolveResizeScale())
            val transformations = finalOptions.transformations
            val disabledReuseBitmap = finalOptions.disabledReuseBitmap ?: false
            val ignoreExifOrientation = finalOptions.ignoreExifOrientation ?: false
            val bitmapMemoryCachePolicy = finalOptions.bitmapMemoryCachePolicy ?: ENABLED
            val disabledAnimatedImage = finalOptions.disabledAnimatedImage ?: false
            val placeholderImage = finalOptions.placeholderImage
            val errorImage = finalOptions.errorImage
            val transition = finalOptions.transition
            val resizeApplyToDrawable = finalOptions.resizeApplyToDrawable ?: false

            return when (this@Builder) {
                is DisplayRequest.Builder -> {
                    DisplayRequest.DisplayRequestImpl(
                        sketch = sketch,
                        context = context,
                        uriString = uriString,
                        listener = listener,
                        progressListener = progressListener,
                        target = target,
                        lifecycle = lifecycle,
                        globalOptions = globalOptions,
                        definedOptions = definedOptions,
                        depth = depth,
                        parameters = parameters,
                        httpHeaders = httpHeaders,
                        downloadDiskCachePolicy = downloadDiskCachePolicy,
                        bitmapResultDiskCachePolicy = bitmapResultDiskCachePolicy,
                        bitmapConfig = bitmapConfig,
                        colorSpace = colorSpace,
                        preferQualityOverSpeed = preferQualityOverSpeed,
                        resize = resize,
                        resizeSizeResolver = resizeSizeResolver,
                        resizePrecisionDecider = resizePrecisionDecider,
                        resizeScaleDecider = resizeScaleDecider,
                        transformations = transformations,
                        disabledReuseBitmap = disabledReuseBitmap,
                        ignoreExifOrientation = ignoreExifOrientation,
                        bitmapMemoryCachePolicy = bitmapMemoryCachePolicy,
                        disabledAnimatedImage = disabledAnimatedImage,
                        placeholderImage = placeholderImage,
                        errorImage = errorImage,
                        transition = transition,
                        resizeApplyToDrawable = resizeApplyToDrawable,
                    )
                }
                is LoadRequest.Builder -> {
                    LoadRequest.LoadRequestImpl(
                        sketch = sketch,
                        context = context,
                        uriString = uriString,
                        listener = listener,
                        progressListener = progressListener,
                        target = target,
                        lifecycle = lifecycle,
                        globalOptions = globalOptions,
                        definedOptions = definedOptions,
                        depth = depth,
                        parameters = parameters,
                        httpHeaders = httpHeaders,
                        downloadDiskCachePolicy = downloadDiskCachePolicy,
                        bitmapResultDiskCachePolicy = bitmapResultDiskCachePolicy,
                        bitmapConfig = bitmapConfig,
                        colorSpace = colorSpace,
                        preferQualityOverSpeed = preferQualityOverSpeed,
                        resize = resize,
                        resizeSizeResolver = resizeSizeResolver,
                        resizePrecisionDecider = resizePrecisionDecider,
                        resizeScaleDecider = resizeScaleDecider,
                        transformations = transformations,
                        disabledReuseBitmap = disabledReuseBitmap,
                        ignoreExifOrientation = ignoreExifOrientation,
                        bitmapMemoryCachePolicy = bitmapMemoryCachePolicy,
                        disabledAnimatedImage = disabledAnimatedImage,
                        placeholderImage = placeholderImage,
                        errorImage = errorImage,
                        transition = transition,
                        resizeApplyToDrawable = resizeApplyToDrawable,
                    )
                }
                is DownloadRequest.Builder -> {
                    DownloadRequest.DownloadRequestImpl(
                        sketch = sketch,
                        context = context,
                        uriString = uriString,
                        listener = listener,
                        progressListener = progressListener,
                        target = target,
                        lifecycle = lifecycle,
                        globalOptions = globalOptions,
                        definedOptions = definedOptions,
                        depth = depth,
                        parameters = parameters,
                        httpHeaders = httpHeaders,
                        downloadDiskCachePolicy = downloadDiskCachePolicy,
                        bitmapResultDiskCachePolicy = bitmapResultDiskCachePolicy,
                        bitmapConfig = bitmapConfig,
                        colorSpace = colorSpace,
                        preferQualityOverSpeed = preferQualityOverSpeed,
                        resize = resize,
                        resizeSizeResolver = resizeSizeResolver,
                        resizePrecisionDecider = resizePrecisionDecider,
                        resizeScaleDecider = resizeScaleDecider,
                        transformations = transformations,
                        disabledReuseBitmap = disabledReuseBitmap,
                        ignoreExifOrientation = ignoreExifOrientation,
                        bitmapMemoryCachePolicy = bitmapMemoryCachePolicy,
                        disabledAnimatedImage = disabledAnimatedImage,
                        placeholderImage = placeholderImage,
                        errorImage = errorImage,
                        transition = transition,
                        resizeApplyToDrawable = resizeApplyToDrawable,
                    )
                }
                else -> throw UnsupportedOperationException("Unsupported ImageRequest.Builder: ${this@Builder::class.java}")
            }
        }

        private fun resolveResizeSizeResolver(): SizeResolver {
            val target = target
            return if (target is ViewTarget<*>) {
                ViewSizeResolver(target.view)
            } else {
                DisplaySizeResolver(context)
            }
        }


        private fun resolveLifecycle(): Lifecycle? =
            target.asOrNull<ViewTarget<*>>()?.view?.context?.getLifecycle()

        private fun resolveResizeScale(): Scale =
            target.asOrNull<ViewTarget<*>>()
                ?.view?.asOrNull<ImageView>()
                ?.scaleType?.let {
                    when (it) {
                        ScaleType.FIT_START -> START_CROP
                        ScaleType.FIT_CENTER -> CENTER_CROP
                        ScaleType.FIT_END -> END_CROP
                        ScaleType.CENTER_INSIDE -> CENTER_CROP
                        ScaleType.CENTER -> CENTER_CROP
                        ScaleType.CENTER_CROP -> CENTER_CROP
                        else -> Scale.FILL
                    }
                } ?: CENTER_CROP

        private fun combinationListener(): Listener<ImageRequest, ImageResult.Success, ImageResult.Error>? {
            val target = target
            val listener = listener
            val viewListenerProvider =
                target.asOrNull<ViewTarget<*>>()?.view?.asOrNull<ListenerProvider>()
            @Suppress("UNCHECKED_CAST") val viewListener =
                viewListenerProvider?.getListener() as Listener<ImageRequest, ImageResult.Success, ImageResult.Error>?
            return if (listener != null && viewListener != null && listener !== viewListener) {
                CombinedListener(viewListener, listener)
            } else {
                listener ?: viewListener
            }
        }

        private fun combinationProgressListener(): ProgressListener<ImageRequest>? {
            val target = target
            val progressListener = progressListener
            val viewListenerProvider =
                target.asOrNull<ViewTarget<*>>()?.view?.asOrNull<ListenerProvider>()
            @Suppress("UNCHECKED_CAST") val viewProgressListener =
                viewListenerProvider?.getProgressListener() as ProgressListener<ImageRequest>?
            return if (progressListener != null && viewProgressListener != null && progressListener != viewProgressListener) {
                CombinedProgressListener(viewProgressListener, progressListener)
            } else {
                progressListener ?: viewProgressListener
            }
        }
    }
}