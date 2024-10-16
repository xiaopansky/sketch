package com.github.panpf.sketch.sample.ui.test

import android.graphics.Color
import android.graphics.Paint
import com.github.panpf.sketch.transform.AnimatedTransformation
import com.github.panpf.sketch.transform.PixelOpacity
import com.github.panpf.sketch.util.Rect

actual data object TestAnimatedTransformation : AnimatedTransformation {

    override val key: String = "TestAnimatedTransformation"

    private val paint = Paint().apply {
        color = Color.RED
    }

    override fun transform(canvas: Any, bounds: Rect): PixelOpacity {
        if (canvas is android.graphics.Canvas) {
            val radius = bounds.width() / 4f
            canvas.drawCircle(
                /* cx = */ radius,
                /* cy = */ radius,
                /* radius = */ radius,
                /* paint = */ paint
            )
        }
        return PixelOpacity.TRANSLUCENT
    }
}