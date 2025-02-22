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

package com.github.panpf.sketch.painter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_HIDDEN_WHEN_COMPLETED
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_HIDDEN_WHEN_INDETERMINATE
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_SECTOR_BACKGROUND_COLOR
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_SECTOR_PROGRESS_COLOR
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_SECTOR_SIZE
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_SECTOR_STROKE_COLOR
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_SECTOR_STROKE_WIDTH_PERCENT
import com.github.panpf.sketch.ability.PROGRESS_INDICATOR_STEP_ANIMATION_DURATION
import com.github.panpf.sketch.painter.internal.AbsProgressPainter


/**
 * Create a [SectorProgressPainter] and remember it
 *
 * @see com.github.panpf.sketch.extensions.compose.common.test.painter.SectorProgressPainterTest.testRememberSectorProgressPainter
 */
@Composable
fun rememberSectorProgressPainter(
    size: Dp = PROGRESS_INDICATOR_SECTOR_SIZE.dp,
    backgroundColor: Color = Color(PROGRESS_INDICATOR_SECTOR_BACKGROUND_COLOR),
    strokeColor: Color = Color(PROGRESS_INDICATOR_SECTOR_STROKE_COLOR),
    progressColor: Color = Color(PROGRESS_INDICATOR_SECTOR_PROGRESS_COLOR),
    strokeWidth: Dp = size * PROGRESS_INDICATOR_SECTOR_STROKE_WIDTH_PERCENT,
    hiddenWhenIndeterminate: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_INDETERMINATE,
    hiddenWhenCompleted: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_COMPLETED,
    stepAnimationDuration: Int = PROGRESS_INDICATOR_STEP_ANIMATION_DURATION,
): SectorProgressPainter {
    val density = LocalDensity.current
    return remember(
        density,
        size,
        backgroundColor,
        strokeColor,
        progressColor,
        strokeWidth,
        hiddenWhenIndeterminate,
        hiddenWhenCompleted,
        stepAnimationDuration
    ) {
        SectorProgressPainter(
            density = density,
            size = size,
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            progressColor = progressColor,
            strokeWidth = strokeWidth,
            hiddenWhenIndeterminate = hiddenWhenIndeterminate,
            hiddenWhenCompleted = hiddenWhenCompleted,
            stepAnimationDuration = stepAnimationDuration
        )
    }
}

/**
 * A [ProgressPainter] that uses a sector to draw progress
 *
 * @see com.github.panpf.sketch.extensions.compose.common.test.painter.SectorProgressPainterTest
 */
@Stable
class SectorProgressPainter(
    density: Density,
    private val size: Dp = PROGRESS_INDICATOR_SECTOR_SIZE.dp,
    private val backgroundColor: Color = Color(PROGRESS_INDICATOR_SECTOR_BACKGROUND_COLOR),
    private val strokeColor: Color = Color(PROGRESS_INDICATOR_SECTOR_STROKE_COLOR),
    private val progressColor: Color = Color(PROGRESS_INDICATOR_SECTOR_PROGRESS_COLOR),
    private val strokeWidth: Dp = size * PROGRESS_INDICATOR_SECTOR_STROKE_WIDTH_PERCENT,
    hiddenWhenIndeterminate: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_INDETERMINATE,
    hiddenWhenCompleted: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_COMPLETED,
    stepAnimationDuration: Int = PROGRESS_INDICATOR_STEP_ANIMATION_DURATION,
) : AbsProgressPainter(
    hiddenWhenIndeterminate = hiddenWhenIndeterminate,
    hiddenWhenCompleted = hiddenWhenCompleted,
    stepAnimationDuration = stepAnimationDuration
), SketchPainter {

    override val intrinsicSize: Size = with(density) { Size(size.toPx(), size.toPx()) }

    override fun DrawScope.drawProgress(drawProgress: Float) {
        // background
        val widthRadius = size.width / 2f
        val heightRadius = size.height / 2f
        val radius = widthRadius.coerceAtMost(heightRadius)
        val cx = widthRadius
        val cy = heightRadius
        val center = Offset(widthRadius, heightRadius)
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = center,
        )

        // stroke
        drawCircle(
            color = strokeColor,
            radius = radius,
            center = center,
            style = Stroke(strokeWidth.toPx())
        )

        // progress
        val space = strokeWidth.toPx() * 3f
        val sweepAngle = drawProgress * 360f
        drawArc(
            color = progressColor,
            startAngle = 270f,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(cx - radius + space, cy - radius + space),
            size = Size((radius - space) * 2f, (radius - space) * 2f),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SectorProgressPainter
        if (size != other.size) return false
        if (backgroundColor != other.backgroundColor) return false
        if (strokeColor != other.strokeColor) return false
        if (progressColor != other.progressColor) return false
        if (strokeWidth != other.strokeWidth) return false
        if (hiddenWhenIndeterminate != other.hiddenWhenIndeterminate) return false
        if (hiddenWhenCompleted != other.hiddenWhenCompleted) return false
        if (stepAnimationDuration != other.stepAnimationDuration) return false
        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + backgroundColor.hashCode()
        result = 31 * result + strokeColor.hashCode()
        result = 31 * result + progressColor.hashCode()
        result = 31 * result + strokeWidth.hashCode()
        result = 31 * result + hiddenWhenIndeterminate.hashCode()
        result = 31 * result + hiddenWhenCompleted.hashCode()
        result = 31 * result + stepAnimationDuration.hashCode()
        return result
    }

    override fun toString(): String {
        return "SectorProgressPainter(" +
                "size=$size, " +
                "backgroundColor=${backgroundColor.toArgb()}, " +
                "strokeColor=${strokeColor.toArgb()}, " +
                "progressColor=${progressColor.toArgb()}, " +
                "strokeWidth=$strokeWidth, " +
                "hiddenWhenIndeterminate=$hiddenWhenIndeterminate, " +
                "hiddenWhenCompleted=$hiddenWhenCompleted, " +
                "stepAnimationDuration=$stepAnimationDuration" +
                ")"
    }
}