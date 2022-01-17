package com.github.panpf.sketch.decode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build.VERSION
import androidx.exifinterface.media.ExifInterface
import com.caverock.androidsvg.SVG
import com.github.panpf.sketch.ImageFormat
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.DataSource
import com.github.panpf.sketch.decode.internal.AbsBitmapDecoder
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.svgBackgroundColor
import kotlin.math.roundToInt

/**
 * Notes: Android 26 and before versions do not support scale to read frames,
 * resulting in slow decoding speed and large memory consumption in the case of large videos and causes memory jitter
 *
 * Notes：LoadRequest's preferQualityOverSpeed, colorSpace attributes will not take effect;
 * The bitmapConfig attribute takes effect only on Android 30 or later
 */
class SVGBitmapDecoder(
    sketch: Sketch,
    request: LoadRequest,
    dataSource: DataSource,
    val useViewBoundsAsIntrinsicSize: Boolean = true,
    val backgroundColor: Int?,
) : AbsBitmapDecoder(sketch, request, dataSource) {

    companion object {
        const val MIME_TYPE = "image/svg+xml"
    }

    private val svg by lazy {
        dataSource.newInputStream().use { SVG.getFromInputStream(it) }
    }

    override fun isCacheToDisk(decodeConfig: DecodeConfig): Boolean = true

    override fun readImageInfo(): ImageInfo {
        val width: Int
        val height: Int
        val viewBox: RectF? = svg.documentViewBox
        if (useViewBoundsAsIntrinsicSize && viewBox != null) {
            width = viewBox.width().toInt()
            height = viewBox.height().toInt()
        } else {
            width = svg.documentWidth.toInt()
            height = svg.documentHeight.toInt()
        }
        return ImageInfo(MIME_TYPE, width, height, ExifInterface.ORIENTATION_UNDEFINED)
    }

    override fun decode(imageInfo: ImageInfo, decodeConfig: DecodeConfig): Bitmap {
        val svgWidth: Float
        val svgHeight: Float
        val viewBox: RectF? = svg.documentViewBox
        if (useViewBoundsAsIntrinsicSize && viewBox != null) {
            svgWidth = viewBox.width()
            svgHeight = viewBox.height()
        } else {
            svgWidth = svg.documentWidth
            svgHeight = svg.documentHeight
        }

        val inSampleSize = decodeConfig.inSampleSize?.toFloat()
        val dstWidth = if (inSampleSize != null) {
            (imageInfo.width / inSampleSize).roundToInt()
        } else {
            imageInfo.width
        }
        val dstHeight = if (inSampleSize != null) {
            (imageInfo.height / inSampleSize).roundToInt()
        } else {
            imageInfo.height
        }

        // Set the SVG's view box to enable scaling if it is not set.
        if (viewBox == null && svgWidth > 0 && svgHeight > 0) {
            svg.setDocumentViewBox(0f, 0f, svgWidth, svgHeight)
        }

        svg.setDocumentWidth("100%")
        svg.setDocumentHeight("100%")

        val bitmap = sketch.bitmapPoolHelper
            .getOrMake(dstWidth, dstHeight, decodeConfig.inPreferredConfig.toSoftware())
        val canvas = Canvas(bitmap).apply {
            backgroundColor?.let {
                drawColor(it)
            }
        }
        svg.renderToCanvas(canvas)
        return bitmap
    }

    override fun canDecodeRegion(imageInfo: ImageInfo, imageFormat: ImageFormat?): Boolean = false

    override fun decodeRegion(
        imageInfo: ImageInfo,
        srcRect: Rect,
        decodeConfig: DecodeConfig
    ): Bitmap = throw UnsupportedOperationException("SVGBitmapDecoder not support decode region")

    override fun close() {

    }

    class Factory(
        val useViewBoundsAsIntrinsicSize: Boolean = true
    ) : BitmapDecoder.Factory {
        override fun create(
            sketch: Sketch,
            request: LoadRequest,
            fetchResult: FetchResult
        ): SVGBitmapDecoder? {
            // todo 支持根据文件头标识识别 svg 文件
            return if (MIME_TYPE.equals(fetchResult.mimeType, ignoreCase = true)) {
                return SVGBitmapDecoder(
                    sketch,
                    request,
                    fetchResult.dataSource,
                    useViewBoundsAsIntrinsicSize,
                    request.svgBackgroundColor
                )
            } else {
                null
            }
        }
    }

    /** Convert null and [Bitmap.Config.HARDWARE] configs to [Bitmap.Config.ARGB_8888]. */
    private fun Bitmap.Config?.toSoftware(): Bitmap.Config {
        return if (this == null || isHardware) Bitmap.Config.ARGB_8888 else this
    }

    private val Bitmap.Config.isHardware: Boolean
        get() = VERSION.SDK_INT >= 26 && this == Bitmap.Config.HARDWARE
}