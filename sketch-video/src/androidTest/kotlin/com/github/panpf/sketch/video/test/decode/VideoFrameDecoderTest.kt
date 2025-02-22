/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.sketch.video.test.decode

import android.graphics.Bitmap.Config.RGB_565
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.cache.CachePolicy.DISABLED
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.decode.VideoFrameDecoder
import com.github.panpf.sketch.decode.internal.createInSampledTransformed
import com.github.panpf.sketch.decode.internal.createResizeTransformed
import com.github.panpf.sketch.decode.supportVideoFrame
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.images.toDataSource
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.colorType
import com.github.panpf.sketch.request.videoFrameMillis
import com.github.panpf.sketch.request.videoFrameOption
import com.github.panpf.sketch.request.videoFramePercent
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.source.DataFrom.LOCAL
import com.github.panpf.sketch.test.singleton.getTestContextAndSketch
import com.github.panpf.sketch.test.utils.corners
import com.github.panpf.sketch.test.utils.createDecoderOrDefault
import com.github.panpf.sketch.test.utils.createDecoderOrNull
import com.github.panpf.sketch.test.utils.decode
import com.github.panpf.sketch.test.utils.getBitmapOrThrow
import com.github.panpf.sketch.test.utils.shortInfoColorSpace
import com.github.panpf.sketch.test.utils.toRequestContext
import com.github.panpf.sketch.util.toShortInfoString
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class VideoFrameDecoderTest {

    @Test
    fun testSupportApkIcon() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return

        ComponentRegistry().apply {
            assertEquals(
                expected = "ComponentRegistry(" +
                        "fetcherFactoryList=[]," +
                        "decoderFactoryList=[]," +
                        "requestInterceptorList=[]," +
                        "decodeInterceptorList=[]" +
                        ")",
                actual = toString()
            )
        }

        ComponentRegistry {
            supportVideoFrame()
        }.apply {
            assertEquals(
                expected = "ComponentRegistry(" +
                        "fetcherFactoryList=[]," +
                        "decoderFactoryList=[VideoFrameDecoder]," +
                        "requestInterceptorList=[]," +
                        "decodeInterceptorList=[]" +
                        ")",
                actual = toString()
            )
        }

        ComponentRegistry {
            supportVideoFrame()
            supportVideoFrame()
        }.apply {
            assertEquals(
                expected = "ComponentRegistry(" +
                        "fetcherFactoryList=[]," +
                        "decoderFactoryList=[VideoFrameDecoder,VideoFrameDecoder]," +
                        "requestInterceptorList=[]," +
                        "decodeInterceptorList=[]" +
                        ")",
                actual = toString()
            )
        }
    }

    @Test
    fun testConstructor() = runTest {
        val (context, sketch) = getTestContextAndSketch()

        val request = ImageRequest(context, ResourceImages.mp4.uri)
        val requestContext = request.toRequestContext(sketch)
        val dataSource = ResourceImages.mp4.toDataSource(context)

        VideoFrameDecoder(requestContext, dataSource, "video/mp4")
        VideoFrameDecoder(
            requestContext = requestContext,
            dataSource = dataSource,
            mimeType = "video/mp4"
        )
    }

    @Test
    fun testImageInfo() = runTest {
        val (context, sketch) = getTestContextAndSketch()
        val factory = VideoFrameDecoder.Factory()

        ImageRequest(context, ResourceImages.mp4.uri)
            .createDecoderOrDefault(sketch, factory)
            .apply {
                assertEquals(
                    expected = ImageInfo(500, 250, "video/mp4"),
                    actual = imageInfo
                )
            }
    }

    @Test
    fun testDecode() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return@runTest

        val (context, sketch) = getTestContextAndSketch()
        val factory = VideoFrameDecoder.Factory()

        ImageRequest(context, ResourceImages.mp4.uri)
            .decode(sketch, factory).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    assertEquals(
                        expected = "Bitmap(500x250,ARGB_8888${shortInfoColorSpace("SRGB")})",
                        actual = image.getBitmapOrThrow().toShortInfoString()
                    )
                } else {
                    assertEquals(
                        expected = "Bitmap(500x250,RGB_565${shortInfoColorSpace("SRGB")})",
                        actual = image.getBitmapOrThrow().toShortInfoString()
                    )
                }
                assertEquals(
                    expected = "ImageInfo(500x250,'video/mp4')",
                    actual = imageInfo.toShortString()
                )
                assertEquals(expected = LOCAL, actual = dataFrom)
                assertNull(actual = transformeds)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ImageRequest(context, ResourceImages.mp4.uri) {
                colorType(RGB_565)
            }.decode(sketch, factory).apply {
                assertEquals(
                    expected = "Bitmap(500x250,RGB_565${shortInfoColorSpace("SRGB")})",
                    actual = image.getBitmapOrThrow().toShortInfoString()
                )
                assertEquals(
                    expected = "ImageInfo(500x250,'video/mp4')",
                    actual = imageInfo.toShortString()
                )
                assertEquals(expected = LOCAL, actual = dataFrom)
                assertNull(actual = transformeds)
            }
        }

        ImageRequest(context, ResourceImages.mp4.uri) {
            resize(300, 300, LESS_PIXELS)
        }.decode(sketch, factory).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                assertEquals(
                    expected = "Bitmap(250x125,ARGB_8888${shortInfoColorSpace("SRGB")})",
                    actual = image.getBitmapOrThrow().toShortInfoString()
                )
                assertEquals(listOf(createInSampledTransformed(2)), transformeds)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                assertEquals(
                    expected = "Bitmap(250x125,RGB_565${shortInfoColorSpace("SRGB")})",
                    actual = image.getBitmapOrThrow().toShortInfoString()
                )
                assertEquals(listOf(createInSampledTransformed(2)), transformeds)
            } else {
                assertEquals(
                    expected = "Bitmap(250x125,RGB_565${shortInfoColorSpace("SRGB")})",
                    actual = image.getBitmapOrThrow().toShortInfoString()
                )
                assertEquals(
                    expected = listOf(createResizeTransformed(Resize(300, 300, LESS_PIXELS))),
                    actual = transformeds
                )
            }
            assertEquals(
                expected = "ImageInfo(500x250,'video/mp4')",
                actual = imageInfo.toShortString()
            )
            assertEquals(expected = LOCAL, actual = dataFrom)
        }

        assertFailsWith(NullPointerException::class) {
            val request = ImageRequest(context, ResourceImages.png.uri)
            val requestContext = request.toRequestContext(sketch)
            val fetcher = sketch.components.newFetcherOrThrow(requestContext)
            val fetchResult = fetcher.fetch().getOrThrow()
            factory.create(requestContext, fetchResult)!!.decode()
        }
    }

    @Test
    fun testDecodeVideoFrameMicros() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return@runTest

        val (context, sketch) = getTestContextAndSketch()
        val factory = VideoFrameDecoder.Factory()

        val bitmap1 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        val bitmap11 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        val bitmap2 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
            videoFrameMillis(500)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        assertEquals(expected = bitmap1.corners(), actual = bitmap11.corners())
        assertNotEquals(illegal = bitmap1.corners(), actual = bitmap2.corners())
    }

    @Test
    fun testDecodeVideoFramePercent() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return@runTest

        val (context, sketch) = getTestContextAndSketch()
        val factory = VideoFrameDecoder.Factory()

        val bitmap1 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        val bitmap11 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        val bitmap2 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
            videoFramePercent(0.45f)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        assertEquals(expected = bitmap1.corners(), actual = bitmap11.corners())
        assertNotEquals(illegal = bitmap1.corners(), actual = bitmap2.corners())
    }

    @Test
    fun testDecodeVideoOption() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return@runTest

        val (context, sketch) = getTestContextAndSketch()
        val factory = VideoFrameDecoder.Factory()

        val bitmap1 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFramePercent(0.5f)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        val bitmap2 = ImageRequest(context, ResourceImages.mp4.uri) {
            memoryCachePolicy(DISABLED)
            resultCachePolicy(DISABLED)
            videoFramePercent(0.5f)
            videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST)
        }.decode(sketch, factory).image.getBitmapOrThrow()

        assertNotEquals(illegal = bitmap1.corners(), actual = bitmap2.corners())
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val (context, sketch) = getTestContextAndSketch()
        val request = ImageRequest(context, ResourceImages.mp4.uri)
        val requestContext = request.toRequestContext(sketch)
        val dataSource = ResourceImages.mp4.toDataSource(context)
        val element1 = VideoFrameDecoder(requestContext, dataSource, "video/mp4")
        val element11 = VideoFrameDecoder(requestContext, dataSource, "video/mp4")

        assertNotEquals(illegal = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())
        assertNotEquals(illegal = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val (context, sketch) = getTestContextAndSketch()
        val request = ImageRequest(context, ResourceImages.mp4.uri)
        val requestContext = request.toRequestContext(sketch)
        val dataSource = ResourceImages.mp4.toDataSource(context)
        val decoder = VideoFrameDecoder(requestContext, dataSource, "video/mp4")
        assertTrue(
            actual = decoder.toString().contains("VideoFrameDecoder"),
            message = decoder.toString()
        )
        assertTrue(actual = decoder.toString().contains("@"), message = decoder.toString())
    }

    @Test
    fun testFactoryConstructor() {
        VideoFrameDecoder.Factory()
    }

    @Test
    fun testFactoryKey() {
        assertEquals(
            expected = "VideoFrameDecoder",
            actual = VideoFrameDecoder.Factory().key
        )
    }

    @Test
    fun testFactoryCreate() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return@runTest

        val (context, sketch) = getTestContextAndSketch()
        val factory = VideoFrameDecoder.Factory()

        ImageRequest(context, ResourceImages.mp4.uri)
            .createDecoderOrNull(sketch, factory) {
                it.copy(mimeType = "video/mp4")
            }.apply {
                assertTrue(this is VideoFrameDecoder)
            }

        ImageRequest(context, ResourceImages.mp4.uri)
            .createDecoderOrNull(sketch, factory) {
                it.copy(mimeType = null)
            }.apply {
                assertNull(this)
            }

        ImageRequest(context, ResourceImages.mp4.uri)
            .createDecoderOrNull(sketch, factory) {
                it.copy(mimeType = "image/png")
            }.apply {
                assertNull(this)
            }

        ImageRequest(context, ResourceImages.png.uri)
            .createDecoderOrNull(sketch, factory) {
                it.copy(mimeType = "video/mp4")
            }.apply {
                assertTrue(this is VideoFrameDecoder)
            }
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val element1 = VideoFrameDecoder.Factory()
        val element11 = VideoFrameDecoder.Factory()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testFactoryToString() = runTest {
        assertEquals(
            expected = "VideoFrameDecoder",
            actual = VideoFrameDecoder.Factory().toString()
        )
    }
}