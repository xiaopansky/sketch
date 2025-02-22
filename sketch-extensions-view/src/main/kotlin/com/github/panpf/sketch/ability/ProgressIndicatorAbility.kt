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

package com.github.panpf.sketch.ability

import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Drawable.Callback
import android.os.SystemClock
import android.view.View
import androidx.annotation.ColorInt
import com.github.panpf.sketch.drawable.MaskProgressDrawable
import com.github.panpf.sketch.drawable.ProgressDrawable
import com.github.panpf.sketch.drawable.RingProgressDrawable
import com.github.panpf.sketch.drawable.SectorProgressDrawable
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.Progress
import com.github.panpf.sketch.util.dp2Px

/**
 * Display a progress indicator, [progressDrawable] is responsible for the specific style
 *
 * @see com.github.panpf.sketch.extensions.view.test.ability.ProgressIndicatorAbilityTest.testShowProgressIndicator
 */
fun ViewAbilityContainer.showProgressIndicator(progressDrawable: ProgressDrawable) {
    removeProgressIndicator()
    addViewAbility(ProgressIndicatorAbility(progressDrawable))
}

/**
 * Remove progress indicator
 *
 * @see com.github.panpf.sketch.extensions.view.test.ability.ProgressIndicatorAbilityTest.testShowProgressIndicator
 */
fun ViewAbilityContainer.removeProgressIndicator() {
    viewAbilityList
        .find { it is ProgressIndicatorAbility }
        ?.let { removeViewAbility(it) }
}

/**
 * Display a sector progress indicator
 *
 * @see com.github.panpf.sketch.extensions.view.test.ability.ProgressIndicatorAbilityTest.testShowProgressIndicator
 */
fun ViewAbilityContainer.showSectorProgressIndicator(
    size: Int = PROGRESS_INDICATOR_SECTOR_SIZE.dp2Px(),
    color: Int = PROGRESS_INDICATOR_SECTOR_PROGRESS_COLOR,
    backgroundColor: Int = PROGRESS_INDICATOR_SECTOR_BACKGROUND_COLOR,
    hiddenWhenIndeterminate: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_INDETERMINATE,
    hiddenWhenCompleted: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_COMPLETED,
    stepAnimationDuration: Int = PROGRESS_INDICATOR_STEP_ANIMATION_DURATION,
) = showProgressIndicator(
    SectorProgressDrawable(
        size = size,
        backgroundColor = backgroundColor,
        strokeColor = color,
        progressColor = color,
        strokeWidth = size * 0.02f,
        hiddenWhenIndeterminate = hiddenWhenIndeterminate,
        hiddenWhenCompleted = hiddenWhenCompleted,
        stepAnimationDuration = stepAnimationDuration,
    )
)

/**
 * Displays a mask progress indicator
 *
 * @see com.github.panpf.sketch.extensions.view.test.ability.ProgressIndicatorAbilityTest.testShowProgressIndicator
 */
fun ViewAbilityContainer.showMaskProgressIndicator(
    @ColorInt maskColor: Int = PROGRESS_INDICATOR_MASK_COLOR,
    hiddenWhenIndeterminate: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_INDETERMINATE,
    hiddenWhenCompleted: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_COMPLETED,
    stepAnimationDuration: Int = PROGRESS_INDICATOR_STEP_ANIMATION_DURATION,
) = showProgressIndicator(
    MaskProgressDrawable(
        maskColor = maskColor,
        hiddenWhenIndeterminate = hiddenWhenIndeterminate,
        hiddenWhenCompleted = hiddenWhenCompleted,
        stepAnimationDuration = stepAnimationDuration,
    )
)

/**
 * Display a ring progress indicator
 *
 * @see com.github.panpf.sketch.extensions.view.test.ability.ProgressIndicatorAbilityTest.testShowProgressIndicator
 */
fun ViewAbilityContainer.showRingProgressIndicator(
    size: Int = PROGRESS_INDICATOR_RING_SIZE.dp2Px(),
    ringWidth: Float = size * PROGRESS_INDICATOR_RING_WIDTH_PERCENT,
    @ColorInt ringColor: Int = PROGRESS_INDICATOR_RING_COLOR,
    hiddenWhenIndeterminate: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_INDETERMINATE,
    hiddenWhenCompleted: Boolean = PROGRESS_INDICATOR_HIDDEN_WHEN_COMPLETED,
    stepAnimationDuration: Int = PROGRESS_INDICATOR_STEP_ANIMATION_DURATION,
) = showProgressIndicator(
    RingProgressDrawable(
        size = size,
        ringWidth = ringWidth,
        ringColor = ringColor,
        hiddenWhenIndeterminate = hiddenWhenIndeterminate,
        hiddenWhenCompleted = hiddenWhenCompleted,
        stepAnimationDuration = stepAnimationDuration,
    )
)

/**
 * Returns true if progress indicator feature is enabled
 *
 * @see com.github.panpf.sketch.extensions.view.test.ability.ProgressIndicatorAbilityTest.testShowProgressIndicator
 */
val ViewAbilityContainer.isShowProgressIndicator: Boolean
    get() = viewAbilityList.find { it is ProgressIndicatorAbility } != null

/**
 * A ViewAbility that displays [ImageRequest] progress indicator functionality on the View surface
 *
 * @see com.github.panpf.sketch.extensions.view.test.ability.ProgressIndicatorAbilityTest
 */
class ProgressIndicatorAbility(val progressDrawable: ProgressDrawable) : ViewAbility,
    LayoutObserver, RequestListenerObserver, RequestProgressListenerObserver,
    DrawObserver, VisibilityChangedObserver, AttachObserver, Callback {

    override var host: Host? = null

    override fun onAttachedToWindow() {
        progressDrawable.callback = this@ProgressIndicatorAbility
        progressDrawable.setVisible(true, true)
        if (progressDrawable is Animatable) progressDrawable.start()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        progressDrawable.setVisible(visibility == View.VISIBLE, false)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val view = host?.view ?: return
        val availableWidth = view.width - view.paddingLeft - view.paddingRight
        val availableHeight = view.height - view.paddingTop - view.paddingBottom
        val drawableWidth = progressDrawable.intrinsicWidth
        val drawableHeight = progressDrawable.intrinsicHeight
        val boundsLeft: Int
        val boundsRight: Int
        val boundsTop: Int
        val boundsBottom: Int
        if (drawableWidth > 0) {
            boundsLeft = view.paddingLeft + ((availableWidth - drawableWidth) / 2f).toInt()
            boundsRight = boundsLeft + drawableWidth
        } else {
            boundsLeft = view.paddingLeft
            boundsRight = view.width - view.paddingRight
        }
        if (drawableHeight > 0) {
            boundsTop = view.paddingTop + ((availableHeight - drawableHeight) / 2f).toInt()
            boundsBottom = boundsTop + drawableHeight
        } else {
            boundsTop = view.paddingTop
            boundsBottom = view.height - view.paddingBottom
        }
        progressDrawable.setBounds(boundsLeft, boundsTop, boundsRight, boundsBottom)
    }

    override fun onDrawBefore(canvas: Canvas) {

    }

    override fun onDraw(canvas: Canvas) {
        if (progressDrawable.isVisible) {
            progressDrawable.draw(canvas)
        }
    }

    override fun onDetachedFromWindow() {
        if (progressDrawable is Animatable) progressDrawable.stop()
        progressDrawable.setVisible(false, false)
        progressDrawable.callback = null
    }

    override fun onRequestStart(request: ImageRequest) {
        progressDrawable.progress = 0f
    }

    override fun onUpdateRequestProgress(
        request: ImageRequest, progress: Progress
    ) {
        progressDrawable.progress = progress.decimalProgress
    }

    override fun onRequestSuccess(request: ImageRequest, result: ImageResult.Success) {
        progressDrawable.progress = 1f
    }

    override fun onRequestError(request: ImageRequest, error: ImageResult.Error) {
        progressDrawable.progress = -1f
    }

    override fun invalidateDrawable(who: Drawable) {
        host?.view?.invalidate()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        val delay = `when` - SystemClock.uptimeMillis()
        host?.view?.postDelayed(what, delay)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        host?.view?.removeCallbacks(what)
    }
}