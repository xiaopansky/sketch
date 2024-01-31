package com.github.panpf.sketch

import com.github.panpf.sketch.decode.internal.ImageReaderDecoder
import com.github.panpf.sketch.fetch.FileUriFetcher
import com.github.panpf.sketch.fetch.ResourceUriFetcher


internal actual fun platformComponents(): ComponentRegistry {
    return ComponentRegistry.Builder().apply {
        addFetcher(FileUriFetcher.Factory())
        // TODO add desktop components
        addFetcher(ResourceUriFetcher.Factory())
        addDecoder(ImageReaderDecoder.Factory())
    }.build()
}