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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.github.panpf.sketch.decode.BitmapConfig.FixedBitmapConfig
import com.github.panpf.sketch.decode.internal.ImageFormat
import com.github.panpf.sketch.util.Key

/**
 * Build a [BitmapConfig] with the specified [config]
 *
 * @see com.github.panpf.sketch.core.android.test.decode.BitmapConfigTest.testBitmapConfig
 */
fun BitmapConfig(config: Bitmap.Config): BitmapConfig = FixedBitmapConfig(config)

/**
 * Adapt the appropriate [Bitmap.Config] according to the mimeType and set it to the [BitmapFactory.Options.inPreferredConfig] parameter
 *
 * @see com.github.panpf.sketch.core.android.test.decode.BitmapConfigTest
 */
sealed interface BitmapConfig : Key {

    val value: String

    val isLowQuality: Boolean
        get() = this === LowQuality

    val isHighQuality: Boolean
        get() = this === HighQuality

    val isFixed: Boolean
        get() = this is FixedBitmapConfig

    val isDynamic: Boolean
        get() = this !is FixedBitmapConfig

    fun getConfig(mimeType: String?): Bitmap.Config

    companion object {
        fun valueOf(value: String?): BitmapConfig? = when (value) {
            LowQuality.value -> LowQuality
            HighQuality.value -> HighQuality
            else -> if (value != null) FixedBitmapConfig(Bitmap.Config.valueOf(value)) else null
        }
    }

    /**
     * Lower quality bitmap config are preferred.
     * Use [Bitmap.Config.RGB_565] if the image format is JPEG,
     * otherwise use [Bitmap.Config.ARGB_4444] for JELLY_BEAN_MR2 and below,
     * KITKAT and later use [Bitmap.Config.ARGB_8888]
     */
    object LowQuality : BitmapConfig {

        override val key: String by lazy {
            "BitmapConfig(LowQuality)"
        }

        override fun getConfig(mimeType: String?): Bitmap.Config =
            when {
                ImageFormat.parseMimeType(mimeType) == ImageFormat.JPEG -> {
                    Bitmap.Config.RGB_565
                }

                Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT -> {
                    @Suppress("DEPRECATION")
                    Bitmap.Config.ARGB_4444
                }

                else -> {
                    Bitmap.Config.ARGB_8888
                }
            }

        override val value: String
            get() = "LowQuality"

        override fun toString(): String = key
    }

    /**
     * [Bitmap.Config.RGBA_F16] is preferred, otherwise [Bitmap.Config.ARGB_8888] is used.
     */
    object HighQuality : BitmapConfig {

        override val key: String by lazy {
            "BitmapConfig(HighQuality)"
        }

        override val value: String
            get() = "HighQuality"

        override fun getConfig(mimeType: String?): Bitmap.Config =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Bitmap.Config.RGBA_F16
            } else {
                Bitmap.Config.ARGB_8888
            }

        override fun toString(): String = key
    }

    /**
     * Fixed Bitmap Config, whatever mimeTye is will return the specified [Bitmap.Config]
     */
    class FixedBitmapConfig(private val config: Bitmap.Config) : BitmapConfig {

        override val key: String by lazy {
            "BitmapConfig(${config.name})"
        }

        override val value: String
            get() = config.name

        override fun getConfig(mimeType: String?): Bitmap.Config = config

        override fun toString(): String = key

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as FixedBitmapConfig
            if (config != other.config) return false
            return true
        }

        override fun hashCode(): Int {
            return config.hashCode()
        }
    }
}