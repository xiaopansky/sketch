/*
 * Copyright 2023 Coil Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ------------------------------------------------------------------------
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

import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.LifecycleObserver
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asSketchImage
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageOptionsProvider
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.LifecycleResolver
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.ListenerProvider
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.internal.RequestDelegate
import com.github.panpf.sketch.request.internal.ViewTargetRequestDelegate
import com.github.panpf.sketch.resize.AndroidResizeOnDrawHelper
import com.github.panpf.sketch.resize.ResizeOnDrawHelper
import com.github.panpf.sketch.resize.SizeResolver
import com.github.panpf.sketch.resize.internal.ViewSizeResolver
import com.github.panpf.sketch.transition.Crossfade
import com.github.panpf.sketch.transition.CrossfadeTransition
import com.github.panpf.sketch.transition.Transition
import com.github.panpf.sketch.util.asOrNull
import kotlinx.coroutines.Job

/**
 * A [Target] with an associated [View]. Prefer this to [Target] if the given drawables will only
 * be used by [view].
 *
 * Optionally, [ViewTarget]s can implement [LifecycleObserver]. They are automatically registered
 * when the request starts and unregistered when the request completes.
 */
interface ViewTarget<T : View> : Target {

    /**
     * The [View] used by this [Target]. This field should be immutable.
     */
    val view: T?

    /**
     * Th e [view]'s current [Drawable].
     */
    var drawable: Drawable?

    override val currentImage: Image?
        get() = drawable?.asSketchImage()

    override fun getImageOptions(): ImageOptions? =
        view?.asOrNull<ImageOptionsProvider>()?.imageOptions

    override fun getSizeResolver(): SizeResolver? =
        view?.let { ViewSizeResolver(it) }

    override fun getResizeOnDrawHelper(): ResizeOnDrawHelper? {
        // TODO rename to DrawableImageSizeWrapper
        return AndroidResizeOnDrawHelper
    }

    override fun getCrossfadeTransitionFactory(crossfade: Crossfade): Transition.Factory? {
        return CrossfadeTransition.Factory(crossfade)
    }

    override fun getLifecycleResolver(): LifecycleResolver? =
        view?.let { com.github.panpf.sketch.request.ViewLifecycleResolver(it) }

    override fun newRequestDelegate(
        sketch: Sketch,
        initialRequest: ImageRequest,
        job: Job
    ): RequestDelegate = ViewTargetRequestDelegate(sketch, initialRequest, this, job)

    override fun getListener(): Listener? =
        view?.asOrNull<ListenerProvider>()?.getListener()

    override fun getProgressListener(): ProgressListener? =
        view?.asOrNull<ListenerProvider>()?.getProgressListener()
}