package com.github.panpf.sketch.svg.android.test.decode.internal

import android.graphics.Bitmap
import com.github.panpf.sketch.decode.SvgDecoder
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.colorType
import com.github.panpf.sketch.test.singleton.getTestContextAndSketch
import com.github.panpf.sketch.test.utils.decode
import com.github.panpf.sketch.test.utils.getBitmapOrThrow
import com.github.panpf.sketch.test.utils.shortInfoColorSpace
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.computeScaleMultiplierWithOneSide
import com.github.panpf.sketch.util.times
import com.github.panpf.sketch.util.toShortInfoString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SvgsAndroidTest {

    @Test
    fun testReadSvgImageInfo() = runTest {
        // TODO test
    }

    @Test
    fun testDecodeSvg() = runTest {
        // TODO test
    }

    @Test
    fun testColorType() = runTest {
        val (context, sketch) = getTestContextAndSketch()

        val factory = SvgDecoder.Factory()

        val screenSize = context.resources.displayMetrics
            .let { Size(it.widthPixels, it.heightPixels) }
        ImageRequest(context, ResourceImages.svg.uri)
            .decode(sketch, factory).apply {
                assertEquals(
                    expected = "ImageInfo(256x225,'image/svg+xml')",
                    actual = imageInfo.toShortString()
                )
                val sizeMultiplier = computeScaleMultiplierWithOneSide(imageInfo.size, screenSize)
                val bitmapSize = imageInfo.size.times(sizeMultiplier)
                assertEquals(
                    "Bitmap($bitmapSize,ARGB_8888${shortInfoColorSpace("SRGB")})",
                    image.getBitmapOrThrow().toShortInfoString()
                )
            }

        val targetSize = context.resources.displayMetrics
            .let { Size(it.widthPixels / 2, it.heightPixels / 2) }
        ImageRequest(context, ResourceImages.svg.uri) {
            size(targetSize)
            colorType(Bitmap.Config.RGB_565)
        }.decode(sketch, factory).apply {
            assertEquals(
                "ImageInfo(256x225,'image/svg+xml')",
                imageInfo.toShortString()
            )
            val sizeMultiplier = computeScaleMultiplierWithOneSide(imageInfo.size, targetSize)
            val bitmapSize = imageInfo.size.times(sizeMultiplier)
            assertEquals(
                "Bitmap(${bitmapSize},RGB_565${shortInfoColorSpace("SRGB")})",
                image.getBitmapOrThrow().toShortInfoString()
            )
        }
    }
}