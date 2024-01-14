/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.sketch.target

import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.LifecycleResolver
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.allowSetNullDrawable
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.request.internal.RequestDelegate
import com.github.panpf.sketch.resize.Scale
import com.github.panpf.sketch.util.toBitmap
import kotlinx.coroutines.Job

/**
 * Set Drawable to RemoteViews
 */
class RemoteViewsTarget constructor(
    private val remoteViews: RemoteViews,
    @IdRes private val imageViewId: Int,
    private val onUpdated: () -> Unit,
) : Target {

    override val supportDisplayCount: Boolean = false

    override fun onStart(requestContext: RequestContext, placeholder: Image?) =
        setDrawable(requestContext, placeholder)

    override fun onError(requestContext: RequestContext, error: Image?) =
        setDrawable(requestContext, error)

    override fun onSuccess(requestContext: RequestContext, result: Image) =
        setDrawable(requestContext, result)

    private fun setDrawable(requestContext: RequestContext, result: Image?) {
        if (result != null || requestContext.request.allowSetNullDrawable) {
            remoteViews.setImageViewBitmap(imageViewId, result?.toBitmap())
            onUpdated()
        }
    }

    override fun getImageOptions(): ImageOptions? = null

    override fun getLifecycleResolver(): LifecycleResolver? = null

    override fun getRequestDelegate(
        sketch: Sketch,
        initialRequest: ImageRequest,
        job: Job
    ): RequestDelegate? = null

    override fun getScale(): Scale? = null

    override fun getListener(): Listener? = null

    override fun getProgressListener(): ProgressListener? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RemoteViewsTarget
        if (remoteViews != other.remoteViews) return false
        if (imageViewId != other.imageViewId) return false
        if (onUpdated != other.onUpdated) return false
        return true
    }

    override fun hashCode(): Int {
        var result = remoteViews.hashCode()
        result = 31 * result + imageViewId
        result = 31 * result + onUpdated.hashCode()
        return result
    }

    override fun toString(): String {
        return "RemoteViewsDisplayTarget(remoteViews=$remoteViews, imageViewId=$imageViewId, onUpdated=$onUpdated)"
    }
}
