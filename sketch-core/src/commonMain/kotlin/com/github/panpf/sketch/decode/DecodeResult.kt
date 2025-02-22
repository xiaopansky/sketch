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

package com.github.panpf.sketch.decode

import com.github.panpf.sketch.Image
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.source.DataFrom

/**
 * The result of [Decoder.decode]
 *
 * @see com.github.panpf.sketch.core.common.test.decode.DecodeResultTest
 */
data class DecodeResult(
    /**
     * The decoded Image
     */
    val image: Image,

    /**
     * The decoded Image width, height, type, etc.
     */
    val imageInfo: ImageInfo,

    /**
     * Where image comes from
     */
    val dataFrom: DataFrom,

    /**
     * Use this Resize to resize the image when decoding
     */
    val resize: Resize,

    /**
     * Store the transformation history of the Image
     */
    val transformeds: List<String>?,

    /**
     * Store some additional information for consumer use,
     * You can add information here during decoding, transformation, interceptor, etc.
     */
    val extras: Map<String, String>?,
) {

    fun newResult(
        image: Image = this.image,
        imageInfo: ImageInfo = this.imageInfo,
        dataFrom: DataFrom = this.dataFrom,
        resize: Resize = this.resize,
        block: (Builder.() -> Unit)? = null
    ): DecodeResult = Builder(
        image = image,
        imageInfo = imageInfo,
        dataFrom = dataFrom,
        resize = resize,
        transformeds = transformeds?.toMutableList(),
        extras = extras?.toMutableMap(),
    ).apply {
        block?.invoke(this)
    }.build()

    override fun toString(): String = "DecodeResult(" +
            "image=$image, " +
            "imageInfo=$imageInfo, " +
            "dataFrom=$dataFrom, " +
            "resize=$resize, " +
            "transformeds=$transformeds, " +
            "extras=$extras" +
            ")"

    class Builder internal constructor(
        private val image: Image,
        private val imageInfo: ImageInfo,
        private val dataFrom: DataFrom,
        private val resize: Resize,
        private var transformeds: MutableList<String>? = null,
        private var extras: MutableMap<String, String>? = null,
    ) {

        fun addTransformed(transformed: String): Builder = apply {
            this.transformeds = (this.transformeds ?: mutableListOf()).apply {
                add(transformed)
            }
        }

        fun addExtras(key: String, value: String): Builder = apply {
            this.extras = (this.extras ?: mutableMapOf()).apply {
                put(key, value)
            }
        }

        fun build(): DecodeResult = DecodeResult(
            image = image,
            imageInfo = imageInfo,
            dataFrom = dataFrom,
            resize = resize,
            transformeds = transformeds?.toList(),
            extras = extras?.toMap(),
        )
    }
}