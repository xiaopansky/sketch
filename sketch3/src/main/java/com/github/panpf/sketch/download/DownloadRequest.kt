package com.github.panpf.sketch.download

import android.net.Uri
import android.os.Bundle
import com.github.panpf.sketch.common.cache.CachePolicy
import com.github.panpf.sketch.download.internal.DownloadableRequest

class DownloadRequest constructor(
    override val uri: Uri,
    override val extras: Bundle?,
    override val diskCacheKey: String,
    override val diskCachePolicy: CachePolicy,
) : DownloadableRequest {

    fun newBuilder(
        configBlock: (Builder.() -> Unit)? = null
    ): Builder = Builder(this).apply {
        configBlock?.invoke(this)
    }

    fun new(
        configBlock: (Builder.() -> Unit)? = null
    ): DownloadRequest = Builder(this).apply {
        configBlock?.invoke(this)
    }.build()

    companion object {
        fun new(
            uri: Uri,
            configBlock: (Builder.() -> Unit)? = null
        ): DownloadRequest = Builder(uri).apply {
            configBlock?.invoke(this)
        }.build()

        fun new(
            uriString: String,
            configBlock: (Builder.() -> Unit)? = null
        ): DownloadRequest = Builder(uriString).apply {
            configBlock?.invoke(this)
        }.build()
    }

    class Builder {
        private val uri: Uri
        private var extras: Bundle?
        private var diskCacheKey: String?
        private var diskCachePolicy: CachePolicy?

        constructor(uri: Uri) {
            this.uri = uri
            this.extras = null
            this.diskCacheKey = null
            this.diskCachePolicy = null
        }

        constructor(uriString: String) : this(Uri.parse(uriString))

        internal constructor(request: DownloadRequest) {
            this.uri = request.uri
            this.extras = request.extras
            this.diskCacheKey = request.diskCacheKey
            this.diskCachePolicy = request.diskCachePolicy
        }

        fun extras(extras: Bundle?): Builder = apply {
            this.extras = extras
        }

        fun diskCacheKey(diskCacheKey: String?): Builder = apply {
            this.diskCacheKey = diskCacheKey
        }

        fun diskCachePolicy(diskCachePolicy: CachePolicy?): Builder = apply {
            this.diskCachePolicy = diskCachePolicy
        }

        fun build(): DownloadRequest = DownloadRequest(
            uri = uri,
            extras = extras,
            diskCacheKey = diskCacheKey ?: uri.toString(),
            diskCachePolicy = diskCachePolicy ?: CachePolicy.ENABLED,
        )
    }
}