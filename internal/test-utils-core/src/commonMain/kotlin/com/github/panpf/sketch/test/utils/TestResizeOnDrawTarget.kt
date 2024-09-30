/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.sketch.test.utils

import com.github.panpf.sketch.Image
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.internal.OneShotRequestDelegate
import com.github.panpf.sketch.request.internal.OneShotRequestManager
import com.github.panpf.sketch.request.internal.RequestDelegate
import com.github.panpf.sketch.request.internal.RequestManager
import com.github.panpf.sketch.resize.ResizeOnDrawHelper
import com.github.panpf.sketch.target.Target
import kotlinx.coroutines.Job

class TestResizeOnDrawTarget(override val currentImage: Image? = null) : Target {

    var startImage: Image? = null
    var successImage: Image? = null
    var errorImage: Image? = null

    override fun onStart(sketch: Sketch, request: ImageRequest, placeholder: Image?) {
        super.onStart(sketch, request, placeholder)
        startImage = placeholder
    }

    override fun onSuccess(sketch: Sketch, request: ImageRequest, result: Image) {
        super.onSuccess(sketch, request, result)
        successImage = result
    }

    override fun onError(sketch: Sketch, request: ImageRequest, error: Image?) {
        super.onError(sketch, request, error)
        errorImage = error
    }

    private val requestManager = OneShotRequestManager()

    override fun getRequestManager(): RequestManager = requestManager

    override fun newRequestDelegate(
        sketch: Sketch,
        initialRequest: ImageRequest,
        job: Job
    ): RequestDelegate = OneShotRequestDelegate(sketch, initialRequest, this, job)

    override fun getResizeOnDrawHelper(): ResizeOnDrawHelper {
        return TestResizeOnDrawHelper()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other != null && this::class == other::class
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String = "TestResizeOnDrawTarget"
}