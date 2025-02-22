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

package com.github.panpf.sketch.state

import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.isCausedBySaveCellularTraffic


/**
 * Set the state image when the save cellular traffic
 *
 * @see com.github.panpf.sketch.extensions.core.common.test.state.SaveCellularTrafficExtensionsTest.testSaveCellularTrafficError
 */
fun ConditionStateImage.Builder.saveCellularTrafficError(
    stateImage: StateImage
): ConditionStateImage.Builder = apply {
    addState(SaveCellularTrafficCondition, stateImage)
}

/**
 * Set the state image when the save cellular traffic
 *
 * @see com.github.panpf.sketch.extensions.core.common.test.state.SaveCellularTrafficExtensionsTest.testSaveCellularTrafficCondition
 */
data object SaveCellularTrafficCondition : ConditionStateImage.Condition {

    override val key: String = "SaveCellularTraffic"

    override fun accept(request: ImageRequest, throwable: Throwable?): Boolean =
        isCausedBySaveCellularTraffic(request, throwable)

    override fun toString(): String = "SaveCellularTrafficCondition"
}