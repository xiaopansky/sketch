package com.github.panpf.sketch.request.internal

import android.net.Uri
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.Parameters
import com.github.panpf.sketch.request.RequestDepth

interface ImageRequest {
    val uri: Uri
    val uriString: String
    val key: String
    val depth: RequestDepth
    val parameters: Parameters?
    val listener: Listener<ImageRequest, ImageResult, ImageResult>?

    val depthFrom: String?
        get() = parameters?.value(REQUEST_DEPTH_FROM)

    companion object {
        const val REQUEST_DEPTH_FROM = "REQUEST_DEPTH_FROM"
    }
}