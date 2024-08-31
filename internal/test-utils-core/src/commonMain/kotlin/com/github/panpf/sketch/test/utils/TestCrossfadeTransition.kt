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
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.transition.Transition
import com.github.panpf.sketch.transition.TransitionTarget

class TestCrossfadeTransition(
    val requestContext: RequestContext,
    val target: TransitionTarget,
    val result: ImageResult
) : Transition {

    override fun transition() {
        when (result) {
            is ImageResult.Success -> target.onSuccess(
                requestContext,
                TestCrossfadeImage(result.image)
            )

            is ImageResult.Error -> target.onError(requestContext,
                result.image?.let { TestCrossfadeImage(it) })
        }
    }

    class Factory : Transition.Factory {

        override fun create(
            requestContext: RequestContext,
            target: TransitionTarget,
            result: ImageResult,
        ): Transition {
            return TestCrossfadeTransition(requestContext, target, result)
        }

        override val key: String = "TestTransition.Factory"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String {
            return "TestTransition"
        }
    }
}

data class TestCrossfadeImage(val image: Image) : Image by image