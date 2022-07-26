package com.github.panpf.sketch.gif.movie.test.decode

import android.graphics.Canvas
import android.os.Build
import androidx.core.graphics.drawable.updateBounds
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.datasource.AssetDataSource
import com.github.panpf.sketch.datasource.DataFrom.LOCAL
import com.github.panpf.sketch.decode.GifMovieDrawableDecoder
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.drawable.MovieDrawable
import com.github.panpf.sketch.drawable.SketchAnimatableDrawable
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.animatedTransformation
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.request.onAnimationEnd
import com.github.panpf.sketch.request.onAnimationStart
import com.github.panpf.sketch.request.repeatCount
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.transform.PixelOpacity
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GifMovieDrawableDecoderTest {

    @Test
    fun testFactory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return

        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = GifMovieDrawableDecoder.Factory()

        Assert.assertEquals("GifMovieDrawableDecoder", factory.toString())

        // normal
        DisplayRequest(context, newAssetUri("sample_anim.gif")).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, "sample_anim.gif"), "image/gif")
            Assert.assertNotNull(factory.create(sketch, it, RequestContext(it), fetchResult))
        }.apply {
            Assert.assertNotNull(this)
        }

        DisplayRequest(context, newAssetUri("sample_anim.gif")).let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, "sample_anim.gif"), null)
            factory.create(sketch, it, RequestContext(it), fetchResult)
        }.apply {
            Assert.assertNotNull(this)
        }

        // disallowAnimatedImage true
        DisplayRequest(context, newAssetUri("sample_anim.gif")) {
            disallowAnimatedImage()
        }.let {
            val fetchResult =
                FetchResult(AssetDataSource(sketch, it, "sample_anim.gif"), null)
            factory.create(sketch, it, RequestContext(it), fetchResult)
        }.apply {
            Assert.assertNull(this)
        }

        // data error
        DisplayRequest(context, newAssetUri("sample.png")).let {
            val fetchResult = FetchResult(AssetDataSource(sketch, it, "sample.png"), null)
            factory.create(sketch, it, RequestContext(it), fetchResult)
        }.apply {
            Assert.assertNull(this)
        }

        // mimeType error
        DisplayRequest(context, newAssetUri("sample_anim.gif")).let {
            val fetchResult = FetchResult(
                AssetDataSource(sketch, it, "sample_anim.gif"),
                "image/jpeg",
            )
            factory.create(sketch, it, RequestContext(it), fetchResult)
        }.apply {
            Assert.assertNull(this)
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return

        val element1 = GifMovieDrawableDecoder.Factory()
        val element11 = GifMovieDrawableDecoder.Factory()
        val element2 = GifMovieDrawableDecoder.Factory()

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
    fun testDecodeDrawable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return

        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = GifMovieDrawableDecoder.Factory()

        DisplayRequest(context, newAssetUri("sample_anim.gif")) {
            onAnimationStart { }
            disallowReuseBitmap()
        }.apply {
            val fetchResult = sketch.components.newFetcher(this).let { runBlocking { it.fetch() } }
            factory.create(sketch, this, RequestContext(this), fetchResult)!!
                .let { runBlocking { it.decode() } }.apply {
                    Assert.assertEquals(ImageInfo(480, 480, "image/gif", 0), this.imageInfo)
                    Assert.assertEquals(480, this.drawable.intrinsicWidth)
                    Assert.assertEquals(480, this.drawable.intrinsicHeight)
                    Assert.assertEquals(LOCAL, this.dataFrom)
                    Assert.assertNull(this.transformedList)
                    val movieDrawable =
                        (this.drawable as SketchAnimatableDrawable).wrappedDrawable as MovieDrawable
                    Assert.assertEquals(-1, movieDrawable.getRepeatCount())
                    Assert.assertNull(movieDrawable.getAnimatedTransformation())
                }
        }

        DisplayRequest(context, newAssetUri("sample_anim.gif")) {
            repeatCount(3)
            animatedTransformation { PixelOpacity.TRANSLUCENT }
            onAnimationEnd { }
            resize(300, 300)
        }.apply {
            val fetchResult1 = sketch.components.newFetcher(this).let { runBlocking { it.fetch() } }
            factory.create(sketch, this, RequestContext(this), fetchResult1)!!
                .let { runBlocking { it.decode() } }.apply {
                    Assert.assertEquals(ImageInfo(480, 480, "image/gif", 0), this.imageInfo)
                    Assert.assertEquals(480, this.drawable.intrinsicWidth)
                    Assert.assertEquals(480, this.drawable.intrinsicHeight)
                    Assert.assertEquals(LOCAL, this.dataFrom)
                    Assert.assertNull(this.transformedList)
                    val movieDrawable =
                        (this.drawable as SketchAnimatableDrawable).wrappedDrawable as MovieDrawable
                    Assert.assertEquals(3, movieDrawable.getRepeatCount())
                    Assert.assertNotNull(movieDrawable.getAnimatedTransformation())
                }
        }
    }
}