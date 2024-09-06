package com.github.panpf.sketch.core.ios.test.util

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.util.maxMemory
import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformContextsIosTest {

    @Test
    fun testMaxMemory() {
        assertEquals(
            expected = 512L * 1024L * 1024L,
            actual = PlatformContext.INSTANCE.maxMemory(),
        )
    }

    @Test
    fun testAppCacheDirectory() {
        // TODO test
    }

    @Test
    fun testScreenSize() {
        // TODO test
    }
}