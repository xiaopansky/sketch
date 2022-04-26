/*
 * Copyright (C) 2019 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.sketch.zoom.internal

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.ImageView.ScaleType
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.zoom.Zoomer
import com.github.panpf.sketch.zoom.internal.ScaleDragGestureDetector.OnActionListener
import com.github.panpf.sketch.zoom.internal.ScaleDragGestureDetector.OnGestureListener
import kotlin.math.abs
import kotlin.math.roundToInt

internal class ScaleDragHelper constructor(
    context: Context,
    private val sketch: Sketch,
    private val zoomer: Zoomer,
    val onUpdateMatrix: () -> Unit,
    val onDragFling: (startX: Float, startY: Float, velocityX: Float, velocityY: Float) -> Unit,
    val onScaleChanged: (scaleFactor: Float, focusX: Float, focusY: Float) -> Unit,
) {

    private val view = zoomer.view
    private val logger = sketch.logger

    /* Stores default scale and translate information */
    private val baseMatrix = Matrix()

    /* Stores zoom, translate and externally set rotation information generated by the user through touch events */
    private val supportMatrix = Matrix()

    /* Store the fused information of baseMatrix and supportMatrix for drawing */
    private val drawMatrix = Matrix()
    private val drawRectF = RectF()

    /* Cache the coordinates of the last zoom gesture, used when restoring zoom */
    private var lastScaleFocusX: Float = 0f
    private var lastScaleFocusY: Float = 0f
    private val flingHelper: FlingHelper = FlingHelper(context, zoomer, this@ScaleDragHelper)
    private val locationHelper: LocationHelper =
        LocationHelper(context, zoomer, this@ScaleDragHelper)
    private val zoomHelper: ZoomHelper = ZoomHelper(zoomer, this@ScaleDragHelper)
    private val gestureDetector = ScaleDragGestureDetector(
        context, object : OnGestureListener {
            override fun onDrag(dx: Float, dy: Float) = drag(dx, dy)
            override fun onFling(
                startX: Float, startY: Float, velocityX: Float, velocityY: Float
            ) = fling(startX, startY, velocityX, velocityY)

            override fun onScaleBegin(): Boolean = scaleBegin()
            override fun onScale(
                scaleFactor: Float, focusX: Float, focusY: Float
            ) = scale(scaleFactor, focusX, focusY)

            override fun onScaleEnd() = scaleEnd()
        }
    ).apply {
        onActionListener = object : OnActionListener {
            override fun onActionDown(ev: MotionEvent) = actionDown()
            override fun onActionUp(ev: MotionEvent) = actionUp()
            override fun onActionCancel(ev: MotionEvent) = actionUp()
        }
    }
    private var _horScrollEdge: Edge = Edge.NONE
    private var _verScrollEdge: Edge = Edge.NONE
    private var blockParentIntercept: Boolean = false
    private var dragging = false

    var isZooming: Boolean = false
    val horScrollEdge: Edge
        get() = _horScrollEdge
    val verScrollEdge: Edge
        get() = _verScrollEdge
    val defaultZoomScale: Float
        get() = baseMatrix.getScale()
    val supportZoomScale: Float
        get() = supportMatrix.getScale()
    val zoomScale: Float
        get() = drawMatrix.apply { getDrawMatrix(this) }.getScale()

    fun reset() {
        resetBaseMatrix()
        resetSupportMatrix()
        checkAndApplyMatrix()
    }

    fun recycle() {
        cancelFling()
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        /* Location operations cannot be interrupted */
        if (this.locationHelper.isRunning) {
            logger.v(Zoomer.MODULE) {
                "onTouchEvent. requestDisallowInterceptTouchEvent true. locating"
            }
            requestDisallowInterceptTouchEvent(true)
            return true
        }

//        val wasScaling = gestureDetector.isScaling
//        val wasDragging = gestureDetector.isDragging
        val scaleDragHandled = gestureDetector.onTouchEvent(event)
//        val didntScale = !wasScaling && !gestureDetector.isScaling
//        val didntDrag = !wasDragging && !gestureDetector.isDragging
//        blockParentIntercept = didntScale && didntDrag
//        blockParentIntercept = didntScale
//        if (blockParentIntercept) {
//            logger.w(Zoomer.MODULE) {
//                "onTouchEvent. didntScale=${didntScale}, didntDrag=${didntDrag}"
//            }
//        } else {
//            logger.d(Zoomer.MODULE) {
//                "onTouchEvent. didntScale=${didntScale}, didntDrag=${didntDrag}"
//            }
//        }
        return scaleDragHandled
    }

    private fun resetBaseMatrix() {
        baseMatrix.reset()
        val viewSize = zoomer.viewSize.takeIf { !it.isEmpty } ?: return
        val (drawableWidth, drawableHeight) = zoomer.drawableSize.takeIf { !it.isEmpty }?.let {
            if (zoomer.rotateDegrees % 180 == 0) it else Size(it.height, it.width)
        } ?: return
        val drawableGreaterThanView =
            drawableWidth > viewSize.width || drawableHeight > viewSize.height
        val initZoomScale = zoomer.scales.init
        val scaleType = zoomer.scaleType
        when {
            zoomer.readModeDecider?.should(
                sketch, drawableWidth, drawableHeight, viewSize.width, viewSize.height
            ) == true -> {
                baseMatrix.postScale(initZoomScale, initZoomScale)
            }
            scaleType == ScaleType.CENTER
                    || (scaleType == ScaleType.CENTER_INSIDE && !drawableGreaterThanView) -> {
                baseMatrix.postScale(initZoomScale, initZoomScale)
                val dx = (viewSize.width - drawableWidth) / 2f
                val dy = (viewSize.height - drawableHeight) / 2f
                baseMatrix.postTranslate(dx, dy)
            }
            scaleType == ScaleType.CENTER_CROP -> {
                baseMatrix.postScale(initZoomScale, initZoomScale)
                val dx = (viewSize.width - drawableWidth * initZoomScale) / 2f
                val dy = (viewSize.height - drawableHeight * initZoomScale) / 2f
                baseMatrix.postTranslate(dx, dy)
            }
            scaleType == ScaleType.FIT_START -> {
                baseMatrix.postScale(initZoomScale, initZoomScale)
                baseMatrix.postTranslate(0f, 0f)
            }
            scaleType == ScaleType.FIT_END -> {
                baseMatrix.postScale(initZoomScale, initZoomScale)
                baseMatrix.postTranslate(0f, viewSize.height - drawableHeight * initZoomScale)
            }
            scaleType == ScaleType.FIT_CENTER
                    || (scaleType == ScaleType.CENTER_INSIDE && drawableGreaterThanView) -> {
                baseMatrix.postScale(initZoomScale, initZoomScale)
                val dy = (viewSize.height - drawableHeight * initZoomScale) / 2f
                baseMatrix.postTranslate(0f, dy)
            }
            scaleType == ScaleType.FIT_XY -> {
                val srcRectF = RectF(0f, 0f, drawableWidth.toFloat(), drawableHeight.toFloat())
                val dstRectF = RectF(0f, 0f, viewSize.width.toFloat(), viewSize.height.toFloat())
                baseMatrix.setRectToRect(srcRectF, dstRectF, Matrix.ScaleToFit.FILL)
            }
        }
    }

    private fun resetSupportMatrix() {
        supportMatrix.reset()
        supportMatrix.postRotate(zoomer.rotateDegrees.toFloat())
    }

    private fun checkAndApplyMatrix() {
        if (checkMatrixBounds()) {
            onUpdateMatrix()
        }
    }

    private fun checkMatrixBounds(): Boolean {
        val drawRectF = drawRectF.apply { getDrawRect(this) }
        if (drawRectF.isEmpty) {
            _horScrollEdge = Edge.NONE
            _verScrollEdge = Edge.NONE
            return false
        }

        var deltaX = 0f
        val viewWidth = zoomer.viewSize.width
        val displayWidth = drawRectF.width()
        when {
            displayWidth.toInt() <= viewWidth -> {
                deltaX = when (zoomer.scaleType) {
                    ScaleType.FIT_START -> -drawRectF.left
                    ScaleType.FIT_END -> viewWidth - displayWidth - drawRectF.left
                    else -> (viewWidth - displayWidth) / 2 - drawRectF.left
                }
            }
            drawRectF.left.toInt() > 0 -> {
                deltaX = -drawRectF.left
            }
            drawRectF.right.toInt() < viewWidth -> {
                deltaX = viewWidth - drawRectF.right
            }
        }

        var deltaY = 0f
        val viewHeight = zoomer.viewSize.height
        val displayHeight = drawRectF.height()
        when {
            displayHeight.toInt() <= viewHeight -> {
                deltaY = when (zoomer.scaleType) {
                    ScaleType.FIT_START -> -drawRectF.top
                    ScaleType.FIT_END -> viewHeight - displayHeight - drawRectF.top
                    else -> (viewHeight - displayHeight) / 2 - drawRectF.top
                }
            }
            drawRectF.top.toInt() > 0 -> {
                deltaY = -drawRectF.top
            }
            drawRectF.bottom.toInt() < viewHeight -> {
                deltaY = viewHeight - drawRectF.bottom
            }
        }

        // Finally actually translate the matrix
        supportMatrix.postTranslate(deltaX, deltaY)

        _verScrollEdge = when {
            displayHeight.toInt() <= viewHeight -> Edge.BOTH
            drawRectF.top.toInt() >= 0 -> Edge.START
            drawRectF.bottom.toInt() <= viewHeight -> Edge.END
            else -> Edge.NONE
        }
        _horScrollEdge = when {
            displayWidth.toInt() <= viewWidth -> Edge.BOTH
            drawRectF.left.toInt() >= 0 -> Edge.START
            drawRectF.right.toInt() <= viewWidth -> Edge.END
            else -> Edge.NONE
        }
        return true
    }

    fun translateBy(dx: Float, dy: Float) {
        supportMatrix.postTranslate(dx, dy)
        checkAndApplyMatrix()
    }

    fun location(xInDrawable: Float, yInDrawable: Float, animate: Boolean) {
        locationHelper.cancel()
        cancelFling()

        val (viewWidth, viewHeight) = zoomer.viewSize.takeIf { !it.isEmpty } ?: return
        val pointF = PointF(xInDrawable, yInDrawable).apply {
            rotatePoint(this, zoomer.rotateDegrees, zoomer.drawableSize)
        }
        val newX = pointF.x
        val newY = pointF.y

        val scale = zoomScale.format(2)
        val fullZoomScale = zoomer.fullZoomScale.format(2)
        if (scale == fullZoomScale) {
            zoom(zoomer.originZoomScale, newX, newY, false)
        }

        val drawRectF = drawRectF.apply { getDrawRect(this) }
        val currentScale = zoomScale
        val scaleLocationX = (newX * currentScale).toInt()
        val scaleLocationY = (newY * currentScale).toInt()
        val scaledLocationX =
            scaleLocationX.coerceAtLeast(0).coerceAtMost(drawRectF.width().toInt())
        val scaledLocationY =
            scaleLocationY.coerceAtLeast(0).coerceAtMost(drawRectF.height().toInt())
        val centerLocationX = (scaledLocationX - viewWidth / 2).coerceAtLeast(0)
        val centerLocationY = (scaledLocationY - viewHeight / 2).coerceAtLeast(0)

        val startX = abs(drawRectF.left.toInt())
        val startY = abs(drawRectF.top.toInt())
        logger.v(Zoomer.MODULE) {
            "location. start=%dx%d, end=%dx%d"
                .format(startX, startY, centerLocationX, centerLocationY)
        }
        if (animate) {
            locationHelper.start(startX, startY, centerLocationX, centerLocationY)
        } else {
            val dx = -(centerLocationX - startX).toFloat()
            val dy = -(centerLocationY - startY).toFloat()
            translateBy(dx, dy)
        }
    }

    fun zoom(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        zoomHelper.stop()
        if (animate) {
            zoomHelper.start(scale, focalX, focalY)
        } else {
            val baseScale = defaultZoomScale
            val supportZoomScale = supportZoomScale
            val finalScale = scale / baseScale
            val addScale = finalScale / supportZoomScale
            scaleBy(addScale, focalX, focalY)
        }
    }

    fun getDrawMatrix(matrix: Matrix) {
        matrix.set(baseMatrix)
        matrix.postConcat(supportMatrix)
    }

    fun getDrawRect(rectF: RectF) {
        val drawableSize = zoomer.drawableSize
        rectF[0f, 0f, drawableSize.width.toFloat()] = drawableSize.height.toFloat()
        drawMatrix.apply { getDrawMatrix(this) }.mapRect(rectF)
    }

    /**
     * Gets the area that the user can see on the drawable (not affected by rotation)
     */
    fun getVisibleRect(rect: Rect) {
        rect.setEmpty()
        val drawRectF = drawRectF.apply { getDrawRect(this) }.takeIf { !it.isEmpty } ?: return
        val viewSize = zoomer.viewSize.takeIf { !it.isEmpty } ?: return
        val drawableSize = zoomer.drawableSize.takeIf { !it.isEmpty } ?: return
        val (drawableWidth, drawableHeight) = drawableSize.let {
            if (zoomer.rotateDegrees % 180 == 0) it else Size(it.height, it.width)
        }
        val displayWidth = drawRectF.width()
        val displayHeight = drawRectF.height()
        val widthScale = displayWidth / drawableWidth
        val heightScale = displayHeight / drawableHeight
        var left: Float = if (drawRectF.left >= 0)
            0f else abs(drawRectF.left)
        var right: Float = if (displayWidth >= viewSize.width)
            viewSize.width + left else drawRectF.right - drawRectF.left
        var top: Float = if (drawRectF.top >= 0)
            0f else abs(drawRectF.top)
        var bottom: Float = if (displayHeight >= viewSize.height)
            viewSize.height + top else drawRectF.bottom - drawRectF.top
        left /= widthScale
        right /= widthScale
        top /= heightScale
        bottom /= heightScale
        rect.set(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
        reverseRotateRect(rect, zoomer.rotateDegrees, drawableSize)
    }

    /**
     * Whether you can scroll horizontally in the specified direction
     *
     * @param direction A negative value represents the left and a positive value represents the right
     */
    fun canScrollHorizontally(direction: Int): Boolean {
        return if (direction < 0) {
            horScrollEdge != Edge.START && horScrollEdge != Edge.NONE
        } else {
            horScrollEdge != Edge.END && horScrollEdge != Edge.NONE
        }
    }

    /**
     * Whether you can scroll vertically in the specified direction
     *
     * @param direction A negative value means up, and a positive value means down
     */
    fun canScrollVertically(direction: Int): Boolean {
        return if (direction < 0) {
            verScrollEdge != Edge.START && horScrollEdge != Edge.NONE
        } else {
            verScrollEdge != Edge.END && horScrollEdge != Edge.NONE
        }
    }

    private fun drag(dx: Float, dy: Float) {
        if (gestureDetector.isScaling) {
            logger.v(Zoomer.MODULE) { "onDrag. isScaling" }
            return
        }
//        logger.v(Zoomer.MODULE) { "onDrag. dx: $dx, dy: $dy" }
        supportMatrix.postTranslate(dx, dy)
        checkAndApplyMatrix()

        val scaling = gestureDetector.isScaling
        val disallowParentInterceptOnEdge = !zoomer.allowParentInterceptOnEdge
        val blockParent = blockParentIntercept
        val requestDisallowInterceptTouchEvent =
            if (dragging || scaling || blockParent || disallowParentInterceptOnEdge) {
                logger.i(Zoomer.MODULE) {
                    "onDrag. DisallowParentIntercept. dragging=$dragging, scaling=$scaling, blockParent=$blockParent, disallowParentInterceptOnEdge=$disallowParentInterceptOnEdge"
                }
                true
            } else {
                val slop = 1f
                val result = (horScrollEdge == Edge.NONE && (dx >= slop || dx <= -slop))
                        || (horScrollEdge == Edge.START && dx <= -slop)
                        || (horScrollEdge == Edge.END && dx >= slop)
                        || (verScrollEdge == Edge.NONE && (dy >= slop || dy <= -slop))
                        || (verScrollEdge == Edge.START && dy <= -slop)
                        || (verScrollEdge == Edge.END && dy >= slop)
                if (result) {
                    logger.i(Zoomer.MODULE) {
                        "onDrag. DisallowParentIntercept. scrollEdge=%s-%s, d=%sx%s"
                            .format(horScrollEdge, verScrollEdge, dx, dy)
                    }
                } else {
                    logger.w(Zoomer.MODULE) {
                        "onDrag. AllowParentIntercept. scrollEdge=%s-%s, d=%sx%s"
                            .format(horScrollEdge, verScrollEdge, dx, dy)
                    }
                }
                dragging = result
                result
            }
        requestDisallowInterceptTouchEvent(requestDisallowInterceptTouchEvent)
    }

    private fun fling(startX: Float, startY: Float, velocityX: Float, velocityY: Float) {
        logger.v(Zoomer.MODULE) {
            "fling. startX=$startX, startY=$startY, velocityX=$velocityX, velocityY=$velocityY"
        }
        flingHelper.start(velocityX.toInt(), velocityY.toInt())
        onDragFling(startX, startY, velocityX, velocityY)
    }

    private fun cancelFling() {
        flingHelper.cancel()
    }

    private fun scaleBegin(): Boolean {
        logger.v(Zoomer.MODULE) { "onScaleBegin" }
        isZooming = true
        return true
    }

    private fun scaleBy(addScale: Float, focalX: Float, focalY: Float) {
        supportMatrix.postScale(addScale, addScale, focalX, focalY)
        checkAndApplyMatrix()
    }

    internal fun scale(scaleFactor: Float, focusX: Float, focusY: Float) {
        var newScaleFactor = scaleFactor
        logger.v(Zoomer.MODULE) {
            "onScale. scaleFactor: $newScaleFactor, focusX: $focusX, focusY: $focusY"
        }
        lastScaleFocusX = focusX
        lastScaleFocusY = focusY
        val oldSupportScale = supportZoomScale
        var newSupportScale = oldSupportScale * newScaleFactor
        if (newScaleFactor > 1.0f) {
            // The maximum zoom has been reached. Simulate the effect of pulling a rubber band
            val maxSupportScale = zoomer.maxZoomScale / baseMatrix.getScale()
            if (oldSupportScale >= maxSupportScale) {
                var addScale = newSupportScale - oldSupportScale
                addScale *= 0.4f
                newSupportScale = oldSupportScale + addScale
                newScaleFactor = newSupportScale / oldSupportScale
            }
        } else if (newScaleFactor < 1.0f) {
            // The minimum zoom has been reached. Simulate the effect of pulling a rubber band
            val minSupportScale = zoomer.minZoomScale / baseMatrix.getScale()
            if (oldSupportScale <= minSupportScale) {
                var addScale = newSupportScale - oldSupportScale
                addScale *= 0.4f
                newSupportScale = oldSupportScale + addScale
                newScaleFactor = newSupportScale / oldSupportScale
            }
        }
        supportMatrix.postScale(newScaleFactor, newScaleFactor, focusX, focusY)
        checkAndApplyMatrix()
        onScaleChanged(newScaleFactor, focusX, focusY)
    }

    private fun scaleEnd() {
        logger.v(Zoomer.MODULE) { "onScaleEnd" }
        val currentScale = zoomScale.format(2)
        val overMinZoomScale = currentScale < zoomer.minZoomScale.format(2)
        val overMaxZoomScale = currentScale > zoomer.maxZoomScale.format(2)
        if (!overMinZoomScale && !overMaxZoomScale) {
            isZooming = false
            onUpdateMatrix()
        }
    }

    private fun actionDown() {
        lastScaleFocusX = 0f
        lastScaleFocusY = 0f
        dragging = false

        logger.v(Zoomer.MODULE) {
            "onActionDown. disallow parent intercept touch event"
        }
        requestDisallowInterceptTouchEvent(true)

        cancelFling()
    }

    private fun actionUp() {
        /* Roll back to minimum or maximum scaling */
        val currentScale = zoomScale.format(2)
        val minZoomScale = zoomer.minZoomScale.format(2)
        val maxZoomScale = zoomer.maxZoomScale.format(2)
        if (currentScale < minZoomScale) {
            val drawRectF = drawRectF.apply { getDrawRect(this) }
            if (!drawRectF.isEmpty) {
                zoom(minZoomScale, drawRectF.centerX(), drawRectF.centerY(), true)
            }
        } else if (currentScale > maxZoomScale) {
            val lastScaleFocusX = lastScaleFocusX
            val lastScaleFocusY = lastScaleFocusY
            if (lastScaleFocusX != 0f && lastScaleFocusY != 0f) {
                zoom(maxZoomScale, lastScaleFocusX, lastScaleFocusY, true)
            }
        }
    }

    private fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        view.parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }
}