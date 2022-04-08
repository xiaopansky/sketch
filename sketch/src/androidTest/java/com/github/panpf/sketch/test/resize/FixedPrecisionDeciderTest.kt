package com.github.panpf.sketch.test.resize

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.resize.Precision.EXACTLY
import com.github.panpf.sketch.resize.Precision.KEEP_ASPECT_RATIO
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.resize.fixedPrecision
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FixedPrecisionDeciderTest {

    @Test
    fun testPrecision() {
        fixedPrecision(KEEP_ASPECT_RATIO).apply {
            Assert.assertEquals(KEEP_ASPECT_RATIO, precision(100, 48, 50, 50))
            Assert.assertEquals(KEEP_ASPECT_RATIO, precision(100, 49, 50, 50))
            Assert.assertEquals(KEEP_ASPECT_RATIO, precision(100, 50, 50, 50))
            Assert.assertEquals(KEEP_ASPECT_RATIO, precision(100, 51, 50, 50))
            Assert.assertEquals(KEEP_ASPECT_RATIO, precision(100, 52, 50, 50))
        }

        fixedPrecision(EXACTLY).apply {
            Assert.assertEquals(EXACTLY, precision(100, 48, 50, 50))
            Assert.assertEquals(EXACTLY, precision(100, 49, 50, 50))
            Assert.assertEquals(EXACTLY, precision(100, 50, 50, 50))
            Assert.assertEquals(EXACTLY, precision(100, 51, 50, 50))
            Assert.assertEquals(EXACTLY, precision(100, 52, 50, 50))
        }

        fixedPrecision(LESS_PIXELS).apply {
            Assert.assertEquals(LESS_PIXELS, precision(100, 32, 50, 50))
            Assert.assertEquals(LESS_PIXELS, precision(100, 33, 50, 50))
            Assert.assertEquals(LESS_PIXELS, precision(100, 34, 50, 50))
        }
    }

    @Test
    fun testToString() {
        Assert.assertEquals(
            "FixedPrecisionDecider(KEEP_ASPECT_RATIO)",
            fixedPrecision(KEEP_ASPECT_RATIO).toString()
        )
        Assert.assertEquals(
            "FixedPrecisionDecider(EXACTLY)",
            fixedPrecision(EXACTLY).toString()
        )
        Assert.assertEquals(
            "FixedPrecisionDecider(LESS_PIXELS)",
            fixedPrecision(LESS_PIXELS).toString()
        )
    }
}