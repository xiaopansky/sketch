package com.github.panpf.sketch.http.ktor3.wasmjs.test

import com.github.panpf.sketch.http.KtorStack
import kotlin.test.Test
import kotlin.test.assertEquals

class EngineTest {

    @Test
    fun test() {
        val engine = KtorStack().client.engine
        assertEquals("class WasmJsClientEngine", engine::class.toString())
    }
}