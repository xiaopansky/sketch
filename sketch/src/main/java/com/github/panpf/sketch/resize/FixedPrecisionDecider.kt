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
package com.github.panpf.sketch.resize

fun fixedPrecision(precision: Precision): FixedPrecisionDecider = FixedPrecisionDecider(precision)

/**
 * Always return specified precision
 */
data class FixedPrecisionDecider(private val precision: Precision) : PrecisionDecider {

    override val key: String by lazy { "Fixed($precision)" }

    override fun get(
        imageWidth: Int, imageHeight: Int, resizeWidth: Int, resizeHeight: Int
    ): Precision {
        return precision
    }

    override fun toString(): String {
        return "FixedPrecisionDecider($precision)"
    }
}