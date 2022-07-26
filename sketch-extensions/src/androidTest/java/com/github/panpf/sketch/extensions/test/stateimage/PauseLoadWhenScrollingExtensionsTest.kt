package com.github.panpf.sketch.extensions.test.stateimage

import android.R.drawable
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.request.Depth.LOCAL
import com.github.panpf.sketch.request.Depth.MEMORY
import com.github.panpf.sketch.request.Depth.NETWORK
import com.github.panpf.sketch.request.DepthException
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.PAUSE_LOAD_WHEN_SCROLLING_KEY
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.stateimage.ColorStateImage
import com.github.panpf.sketch.stateimage.ErrorStateImage
import com.github.panpf.sketch.stateimage.IntColor
import com.github.panpf.sketch.stateimage.PauseLoadWhenScrollingMatcher
import com.github.panpf.sketch.stateimage.SaveCellularTrafficMatcher
import com.github.panpf.sketch.stateimage.pauseLoadWhenScrollingError
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PauseLoadWhenScrollingExtensionsTest {

    @Test
    fun testPauseLoadWhenScrollingError() {
        ErrorStateImage(ColorStateImage(IntColor(Color.BLACK))).apply {
            Assert.assertNull(matcherList.find { it is PauseLoadWhenScrollingMatcher })
        }

        ErrorStateImage(ColorStateImage(IntColor(Color.BLACK))) {
            pauseLoadWhenScrollingError()
        }.apply {
            Assert.assertNotNull(matcherList.find { it is PauseLoadWhenScrollingMatcher })
        }

        ErrorStateImage(ColorStateImage(IntColor(Color.BLACK))) {
            pauseLoadWhenScrollingError(ColorStateImage(IntColor(Color.BLUE)))
        }.apply {
            Assert.assertNotNull(matcherList.find { it is PauseLoadWhenScrollingMatcher })
        }

        ErrorStateImage(ColorStateImage(IntColor(Color.BLACK))) {
            pauseLoadWhenScrollingError(ColorDrawable(Color.GREEN))
        }.apply {
            Assert.assertNotNull(matcherList.find { it is PauseLoadWhenScrollingMatcher })
        }

        ErrorStateImage(ColorStateImage(IntColor(Color.BLACK))) {
            pauseLoadWhenScrollingError(drawable.btn_dialog)
        }.apply {
            Assert.assertNotNull(matcherList.find { it is PauseLoadWhenScrollingMatcher })
        }
    }

    @Test
    fun testPauseLoadWhenScrollingMatcher() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch

        PauseLoadWhenScrollingMatcher(null).apply {
            val request = DisplayRequest(context, "http://sample.com/sample.jpeg") {
                depth(NETWORK, PAUSE_LOAD_WHEN_SCROLLING_KEY)
            }
            Assert.assertTrue(
                match(
                    request.newDisplayRequest { depth(MEMORY, PAUSE_LOAD_WHEN_SCROLLING_KEY) },
                    DepthException("")
                )
            )
            Assert.assertFalse(
                match(
                    request.newDisplayRequest { depth(LOCAL, PAUSE_LOAD_WHEN_SCROLLING_KEY) },
                    DepthException("")
                )
            )
            Assert.assertFalse(match(request, null))

            Assert.assertNull(getDrawable(sketch, request, null))
        }

        PauseLoadWhenScrollingMatcher(ColorStateImage(IntColor(Color.BLUE))).apply {
            val request = DisplayRequest(context, "http://sample.com/sample.jpeg") {
                depth(NETWORK, PAUSE_LOAD_WHEN_SCROLLING_KEY)
            }

            Assert.assertTrue(getDrawable(sketch, request, null) is ColorDrawable)
        }

        val element1 = PauseLoadWhenScrollingMatcher(ColorStateImage(Color.BLUE))
        val element11 = PauseLoadWhenScrollingMatcher(ColorStateImage(Color.BLUE))
        val element2 = PauseLoadWhenScrollingMatcher(null)

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element2, element11)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertNotEquals(element1, element2)
        Assert.assertNotEquals(element2, element11)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element2.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element11.hashCode())

        PauseLoadWhenScrollingMatcher(ColorStateImage(Color.BLUE)).apply {
            Assert.assertEquals(
                "PauseLoadWhenScrollingMatcher(${stateImage})",
                toString()
            )
        }
    }
}