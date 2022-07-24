package com.github.panpf.sketch.test.util.pool

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.Config.RGB_565
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.decode.internal.sizeString
import com.github.panpf.sketch.util.pool.SizeConfigStrategy
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SizeConfigStrategyTest {

    @Test
    fun testPutGet() {
        SizeConfigStrategy().apply {
            Assert.assertNull(get(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(100, 100, ARGB_8888))
            Assert.assertNotNull(get(100, 100, ARGB_8888))

            Assert.assertNull(get(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(100, 100, ARGB_8888))
            Assert.assertNotNull(get(100, 100, ARGB_8888))
            Assert.assertNotNull(get(100, 100, ARGB_8888))
        }

        SizeConfigStrategy().apply {
            Assert.assertNull(get(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(100, 100, ARGB_8888))
            Assert.assertNotNull(get(100, 100, ARGB_8888))
            Assert.assertNotNull(get(100, 100, ARGB_8888))
        }

        SizeConfigStrategy().apply {
            Assert.assertNull(get(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(1000, 10, ARGB_8888))
            Assert.assertNotNull(get(100, 100, ARGB_8888))
        }

        SizeConfigStrategy().apply {
            Assert.assertNull(get(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(110, 110, ARGB_8888))
            Assert.assertNotNull(get(100, 100, ARGB_8888))
        }

        SizeConfigStrategy().apply {
            Assert.assertNull(get(100, 100, ARGB_8888))
            put(Bitmap.createBitmap(90, 90, ARGB_8888))
            Assert.assertNull(get(100, 100, ARGB_8888))
            Assert.assertNotNull(get(90, 90, ARGB_8888))
        }
    }

    @Test
    fun testRemoveLast() {
        SizeConfigStrategy().apply {
            Assert.assertNull(removeLast())
            put(Bitmap.createBitmap(50, 50, ARGB_8888))
            put(Bitmap.createBitmap(70, 70, ARGB_8888))
            put(Bitmap.createBitmap(90, 90, ARGB_8888))
            Assert.assertEquals("90x90", removeLast()!!.sizeString)
            Assert.assertEquals("70x70", removeLast()!!.sizeString)
            Assert.assertEquals("50x50", removeLast()!!.sizeString)
        }
    }

    @Test
    fun testLogBitmap() {
        SizeConfigStrategy().apply {
            Assert.assertEquals(
                "[10000](ARGB_8888)",
                logBitmap(Bitmap.createBitmap(50, 50, ARGB_8888))
            )
            Assert.assertEquals(
                "[9800](RGB_565)",
                logBitmap(Bitmap.createBitmap(70, 70, RGB_565))
            )

            Assert.assertEquals("[10000](ARGB_8888)", logBitmap(50, 50, ARGB_8888))
            Assert.assertEquals("[9800](RGB_565)", logBitmap(70, 70, RGB_565))
        }
    }

    @Test
    fun testGetSize() {
        SizeConfigStrategy().apply {
            Assert.assertEquals(10000, getSize(Bitmap.createBitmap(50, 50, ARGB_8888)))
            Assert.assertEquals(5000, getSize(Bitmap.createBitmap(50, 50, RGB_565)))
        }
    }

    @Test
    fun testToString() {
        SizeConfigStrategy().apply {
            Assert.assertNull(removeLast())
            put(Bitmap.createBitmap(50, 50, ARGB_8888))
            put(Bitmap.createBitmap(70, 70, RGB_565))
            val toString1 =
                "SizeConfigStrategy(groupedMap=GroupedLinkedMap({[10000](ARGB_8888):1}, {[9800](RGB_565):1}), sortedSizes=(RGB_565[{9800=1}], ARGB_8888[{10000=1}]))"
            val toString2 =
                "SizeConfigStrategy(groupedMap=GroupedLinkedMap({[10000](ARGB_8888):1}, {[9800](RGB_565):1}), sortedSizes=(ARGB_8888[{10000=1}], RGB_565[{9800=1}]))"
            val toString = toString()
            // sortedSizes map order problem
            Assert.assertTrue(toString == toString1 || toString == toString2)
        }
    }
}