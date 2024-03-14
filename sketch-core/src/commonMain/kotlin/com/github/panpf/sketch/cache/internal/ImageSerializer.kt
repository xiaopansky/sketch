package com.github.panpf.sketch.cache.internal

import com.github.panpf.sketch.Image
import com.github.panpf.sketch.datasource.DataSource
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.internal.RequestContext
import okio.BufferedSink


expect fun createImageSerializer(): ImageSerializer?

interface ImageSerializer {
    fun supportImage(image: Image): Boolean
    fun compress(image: Image, sink: BufferedSink)
    fun decode(requestContext: RequestContext, imageInfo: ImageInfo, dataSource: DataSource): Image
}