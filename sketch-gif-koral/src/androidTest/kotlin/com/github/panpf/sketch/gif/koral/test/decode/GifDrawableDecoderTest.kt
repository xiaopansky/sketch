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
package com.github.panpf.sketch.gif.koral.test.decode

import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.datasource.AssetDataSource
import com.github.panpf.sketch.datasource.DataFrom.LOCAL
import com.github.panpf.sketch.decode.GifDrawableDecoder
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.decode.internal.createInSampledTransformed
import com.github.panpf.sketch.decode.supportKoralGif
import com.github.panpf.sketch.drawable.GifDrawableWrapperDrawable
import com.github.panpf.sketch.drawable.SketchAnimatableDrawable
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.animatedTransformation
import com.github.panpf.sketch.getDrawableOrThrow
import com.github.panpf.sketch.request.onAnimationEnd
import com.github.panpf.sketch.request.onAnimationStart
import com.github.panpf.sketch.request.repeatCount
import com.github.panpf.sketch.resources.AssetImages
import com.github.panpf.sketch.test.singleton.sketch
import com.github.panpf.sketch.test.utils.toRequestContext
import com.github.panpf.sketch.transform.PixelOpacity
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GifDrawableDecoderTest {

    @Test
    fun testSupportApkIcon() {
        ComponentRegistry.Builder().apply {
            build().apply {
                Assert.assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "decoderFactoryList=[]," +
                            "requestInterceptorList=[]," +
                            "decodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }

            supportKoralGif()
            build().apply {
                Assert.assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "decoderFactoryList=[GifDrawableDecoder]," +
                            "requestInterceptorList=[]," +
                            "decodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }

            supportKoralGif()
            build().apply {
                Assert.assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "decoderFactoryList=[GifDrawableDecoder,GifDrawableDecoder]," +
                            "requestInterceptorList=[]," +
                            "decodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }
        }
    }

    @Test
    fun testFactory() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = GifDrawableDecoder.Factory()

        Assert.assertEquals("GifDrawableDecoder", factory.toString())

        // normal
        ImageRequest(context, AssetImages.animGif.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.animGif.fileName), "image/gif")
            factory.create(
                sketch,
                it.toRequestContext(sketch),
                fetchResult
            )
        }.apply {
            Assert.assertNotNull(this)
        }

        ImageRequest(context, AssetImages.animGif.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.animGif.fileName), null)
            factory.create(
                sketch,
                it.toRequestContext(sketch),
                fetchResult
            )
        }.apply {
            Assert.assertNotNull(this)
        }

        // disallowAnimatedImage true
        ImageRequest(context, AssetImages.animGif.uri) {
            disallowAnimatedImage()
        }.let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.animGif.fileName), null)
            factory.create(
                sketch,
                it.toRequestContext(sketch),
                fetchResult
            )
        }.apply {
            Assert.assertNull(this)
        }

        // data error
        ImageRequest(context, AssetImages.png.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, AssetImages.png.fileName), null)
            factory.create(
                sketch,
                it.toRequestContext(sketch),
                fetchResult
            )
        }.apply {
            Assert.assertNull(this)
        }

        // mimeType error
        ImageRequest(context, AssetImages.animGif.uri).let {
            val fetchResult = FetchResult(
                AssetDataSource(sketch, it, AssetImages.animGif.fileName),
                "image/jpeg",
            )
            factory.create(
                sketch,
                it.toRequestContext(sketch),
                fetchResult
            )
        }.apply {
            Assert.assertNull(this)
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val element1 = GifDrawableDecoder.Factory()
        val element11 = GifDrawableDecoder.Factory()
        val element2 = GifDrawableDecoder.Factory()

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element2, element11)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertEquals(element1, element2)
        Assert.assertEquals(element2, element11)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertEquals(element1.hashCode(), element2.hashCode())
        Assert.assertEquals(element2.hashCode(), element11.hashCode())
    }

    @Test
    fun testDecode() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = GifDrawableDecoder.Factory()

        ImageRequest(context, AssetImages.animGif.uri) {
            onAnimationStart { }
        }.apply {
            val fetchResult = sketch.components.newFetcherOrThrow(this)
                .let { runBlocking { it.fetch() }.getOrThrow() }
            factory.create(
                sketch,
                this@apply.toRequestContext(sketch),
                fetchResult
            )!!.let { runBlocking { it.decode() }.getOrThrow() }.apply {
                Assert.assertEquals(ImageInfo(480, 480, "image/gif", 0), this.imageInfo)
                Assert.assertEquals(480, image.getDrawableOrThrow().intrinsicWidth)
                Assert.assertEquals(480, image.getDrawableOrThrow().intrinsicHeight)
                Assert.assertEquals(LOCAL, this.dataFrom)
                Assert.assertNull(this.transformedList)
                val gifDrawable =
                    ((image.getDrawableOrThrow() as SketchAnimatableDrawable).drawable as GifDrawableWrapperDrawable).gifDrawable
                Assert.assertEquals(0, gifDrawable.loopCount)
                Assert.assertNull(gifDrawable.transform)
            }
        }

        ImageRequest(context, AssetImages.animGif.uri) {
            repeatCount(3)
            animatedTransformation { PixelOpacity.TRANSLUCENT }
            onAnimationEnd {}
            resize(300, 300)
        }.apply {
            val fetchResult1 = sketch.components.newFetcherOrThrow(this)
                .let { runBlocking { it.fetch() }.getOrThrow() }
            factory.create(
                sketch,
                this@apply.toRequestContext(sketch),
                fetchResult1
            )!!.let { runBlocking { it.decode() }.getOrThrow() }.apply {
                Assert.assertEquals(ImageInfo(480, 480, "image/gif", 0), this.imageInfo)
                Assert.assertEquals(240, image.getDrawableOrThrow().intrinsicWidth)
                Assert.assertEquals(240, image.getDrawableOrThrow().intrinsicHeight)
                Assert.assertEquals(LOCAL, this.dataFrom)
                Assert.assertEquals(listOf(createInSampledTransformed(2)), this.transformedList)
                val gifDrawable =
                    ((image.getDrawableOrThrow() as SketchAnimatableDrawable).drawable as GifDrawableWrapperDrawable).gifDrawable
                Assert.assertEquals(3, gifDrawable.loopCount)
                Assert.assertNotNull(gifDrawable.transform)
                gifDrawable.transform!!.onDraw(Canvas(), null, null)
            }
        }
    }
}