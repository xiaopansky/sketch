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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.sketch.drawable.DrawableEqualizer
import com.github.panpf.sketch.util.IntColor
import com.github.panpf.sketch.util.SketchSize


/* ********************************************* drawable icon ********************************************* */

@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    background: DrawableEqualizer? = null,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    @DrawableRes background: Int? = null,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    background: IntColor? = null,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}


@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    background: DrawableEqualizer? = null,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    @DrawableRes background: Int? = null,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    background: IntColor? = null,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}


@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    icon: DrawableEqualizer,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}


/* ********************************************* res icon ********************************************* */

@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    background: DrawableEqualizer? = null,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    @DrawableRes background: Int? = null,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    background: IntColor? = null,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}


@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    background: DrawableEqualizer? = null,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    @DrawableRes background: Int? = null,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    background: IntColor? = null,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, background, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        background = background,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}


@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    iconSize: SketchSize? = null,
    @ColorRes iconTint: Int,
): IconDrawableStateImage = remember(icon, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}

@Composable
fun rememberIconDrawableStateImage(
    @DrawableRes icon: Int,
    iconSize: SketchSize? = null,
    iconTint: IntColor? = null,
): IconDrawableStateImage = remember(icon, iconSize, iconTint) {
    IconDrawableStateImage(
        icon = icon,
        iconSize = iconSize,
        iconTint = iconTint,
    )
}