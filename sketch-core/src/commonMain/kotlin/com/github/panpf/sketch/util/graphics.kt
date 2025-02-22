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

package com.github.panpf.sketch.util

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Fast Gaussian blur, pixels format is ARGB_8888
 *
 * @see com.github.panpf.sketch.core.android.test.util.BitmapsAndroidTest.testBlur
 * @see com.github.panpf.sketch.core.nonandroid.test.util.BitmapsNonAndroidTest.testBlur
 */
fun fastGaussianBlur(pixels: IntArray, width: Int, height: Int, radius: Int) {
    val wm = width - 1
    val hm = height - 1
    val wh = width * height
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(max(width, height))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = yi
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int
    y = 0
    while (y < height) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * width
        i = -radius
        while (i <= radius) {
            p = pixels[yi + min(wm, max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += width
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < width) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = min(x + radius + 1, wm)
            }
            p = pixels[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi++
            x++
        }
        yw += width
        y++
    }
    x = 0
    while (x < width) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * width
        i = -radius
        while (i <= radius) {
            yi = max(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += width
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < height) {
            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pixels[yi] =
                -0x1000000 and pixels[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = min(y + r1, hm) * width
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi += width
            y++
        }
        x++
    }
}

/**
 * Calculate the size of the rotated image
 *
 * @see com.github.panpf.sketch.core.nonandroid.test.util.BitmapsNonAndroidTest.testRotate
 */
fun calculateRotatedSize(size: Size, angle: Double): Size {
    val radians = angle * 0.017453292519943295
    val affineTransform: (Pair<Double, Double>) -> Pair<Double, Double> = { corner ->
        val x = corner.first * kotlin.math.cos(radians) - corner.second * kotlin.math.sin(radians)
        val y = corner.first * kotlin.math.sin(radians) + corner.second * kotlin.math.cos(radians)
        Pair(x, y)
    }

    var minX = Double.MAX_VALUE
    var minY = Double.MAX_VALUE
    var maxX = Double.MIN_VALUE
    var maxY = Double.MIN_VALUE
    val corners = arrayOf(
        Pair(0.0, 0.0),
        Pair(size.width.toDouble(), 0.0),
        Pair(0.0, size.height.toDouble()),
        Pair(size.width.toDouble(), size.height.toDouble())
    )
    for (corner in corners) {
        val result = affineTransform(corner)
        minX = min(minX, result.first)
        minY = min(minY, result.second)
        maxX = max(maxX, result.first)
        maxY = max(maxY, result.second)
    }

    val newWidth = abs(maxX - minX).toInt()
    val newHeight = abs(maxY - minY).toInt()
    return Size(width = newWidth, height = newHeight)
}