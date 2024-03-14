package com.github.panpf.sketch.compose

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.BitmapImage
import com.github.panpf.sketch.DrawableImage
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.compose.painter.asPainter


actual fun Image.asPainter(): Painter = when (this) {
    is PainterImage -> painter
    is BitmapImage -> bitmap.asImageBitmap().asPainter()
    is DrawableImage -> drawable.asPainter()
    else -> throw IllegalArgumentException("Not supported conversion to Painter from Image '$this'")
}