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
package com.github.panpf.sketch.util

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.WorkerThread
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.component3
import androidx.core.graphics.component4

/**
 * Drawable into new Bitmap. Each time a new bitmap is drawn
 */
@WorkerThread
internal fun Drawable.toNewBitmap(
    preferredConfig: Config? = null,
    targetSize: Size? = null
): Bitmap {
    val (oldLeft, oldTop, oldRight, oldBottom) = bounds
    val targetWidth = targetSize?.width ?: intrinsicWidth
    val targetHeight = targetSize?.height ?: intrinsicHeight
    setBounds(0, 0, targetWidth, targetHeight)

    val config = preferredConfig ?: ARGB_8888
    val bitmap: Bitmap = Bitmap.createBitmap(
        /* width = */ targetWidth,
        /* height = */ targetHeight,
        /* config = */ config,
    )
    val canvas = Canvas(bitmap)
    draw(canvas)

    setBounds(oldLeft, oldTop, oldRight, oldBottom) // restore bounds
    return bitmap
}

internal val Drawable.widthWithBitmapFirst: Int
    get() = (this as? BitmapDrawable)?.bitmap?.width ?: intrinsicWidth

internal val Drawable.heightWithBitmapFirst: Int
    get() = (this as? BitmapDrawable)?.bitmap?.height ?: intrinsicHeight