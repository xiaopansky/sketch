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

package com.github.panpf.sketch.core.android.test.state

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.DrawableImage
import com.github.panpf.sketch.drawable.ColorEquitableDrawable
import com.github.panpf.sketch.drawable.EquitableDrawable
import com.github.panpf.sketch.drawable.RealEquitableDrawable
import com.github.panpf.sketch.drawable.ResDrawable
import com.github.panpf.sketch.drawable.asEquitable
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.state.DrawableStateImage
import com.github.panpf.sketch.state.asStateImage
import com.github.panpf.sketch.test.singleton.getTestContextAndSketch
import com.github.panpf.sketch.test.utils.TestColor
import com.github.panpf.sketch.util.asOrNull
import com.github.panpf.sketch.util.asOrThrow
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DrawableStateImageTest {

    @Test
    fun testAsStateImage() {
        assertEquals(
            expected = DrawableStateImage(ColorDrawable(TestColor.RED).asEquitable()),
            actual = ColorDrawable(TestColor.RED).asEquitable().asStateImage()
        )
        assertEquals(
            expected = DrawableStateImage(
                RealEquitableDrawable(
                    ColorDrawable(TestColor.RED).asEquitable(101)
                )
            ),
            actual = ColorDrawable(TestColor.RED).asStateImage(101)
        )
        assertEquals(
            expected = DrawableStateImage(
                RealEquitableDrawable(
                    ColorDrawable(TestColor.RED).asEquitable()
                )
            ),
            actual = ColorDrawable(TestColor.RED).asStateImage()
        )
    }

    @Test
    fun testDrawableStateImage() {
        assertEquals(
            expected = DrawableStateImage(
                RealEquitableDrawable(ColorDrawable(TestColor.RED).asEquitable())
            ),
            actual = DrawableStateImage(ColorDrawable(TestColor.RED).asEquitable())
        )

        assertEquals(
            expected = DrawableStateImage(
                ResDrawable(com.github.panpf.sketch.test.utils.core.R.drawable.ic_animated)
            ),
            actual = DrawableStateImage(com.github.panpf.sketch.test.utils.core.R.drawable.ic_animated)
        )
    }

    @Test
    fun testGetImage() {
        val (context, sketch) = getTestContextAndSketch()
        val request = ImageRequest(context, ResourceImages.jpeg.uri)

        DrawableStateImage(ColorEquitableDrawable(Color.BLUE)).apply {
            assertEquals(
                Color.BLUE,
                getImage(sketch, request, null)!!
                    .asOrThrow<DrawableImage>().drawable
                    .asOrThrow<EquitableDrawable>().drawable
                    .asOrNull<ColorDrawable>()!!.color
            )
        }

        DrawableStateImage(ColorEquitableDrawable(Color.GREEN)).apply {
            assertEquals(
                Color.GREEN,
                getImage(sketch, request, null)!!
                    .asOrThrow<DrawableImage>().drawable
                    .asOrThrow<EquitableDrawable>().drawable
                    .asOrNull<ColorDrawable>()!!.color
            )
        }

        DrawableStateImage(android.R.drawable.btn_radio).apply {
            assertTrue(
                getImage(sketch, request, null)
                    ?.asOrThrow<DrawableImage>()?.drawable is StateListDrawable
            )
        }
    }

    @Test
    fun testEquals() {
        val stateImage1 = DrawableStateImage(android.R.drawable.btn_radio)
        val stateImage11 = DrawableStateImage(android.R.drawable.btn_radio)

        val stateImage2 = DrawableStateImage(android.R.drawable.btn_dialog)
        val stateImage21 = DrawableStateImage(android.R.drawable.btn_dialog)

        val stateImage3 = DrawableStateImage(android.R.drawable.btn_plus)
        val stateImage31 = DrawableStateImage(android.R.drawable.btn_plus)

        assertNotSame(stateImage1, stateImage11)
        assertNotSame(stateImage2, stateImage21)
        assertNotSame(stateImage3, stateImage31)

        assertEquals(stateImage1, stateImage11)
        assertEquals(stateImage2, stateImage21)
        assertEquals(stateImage3, stateImage31)

        assertNotEquals(stateImage1, stateImage2)
        assertNotEquals(stateImage1, stateImage3)
        assertNotEquals(stateImage2, stateImage3)
    }

    @Test
    fun testHashCode() {
        val stateImage1 = DrawableStateImage(android.R.drawable.btn_radio)
        val stateImage11 = DrawableStateImage(android.R.drawable.btn_radio)

        val stateImage2 = DrawableStateImage(android.R.drawable.btn_dialog)
        val stateImage21 = DrawableStateImage(android.R.drawable.btn_dialog)

        val stateImage3 = DrawableStateImage(android.R.drawable.btn_plus)
        val stateImage31 = DrawableStateImage(android.R.drawable.btn_plus)

        assertEquals(stateImage1.hashCode(), stateImage11.hashCode())
        assertEquals(stateImage2.hashCode(), stateImage21.hashCode())
        assertEquals(stateImage3.hashCode(), stateImage31.hashCode())

        assertNotEquals(stateImage1.hashCode(), stateImage2.hashCode())
        assertNotEquals(stateImage1.hashCode(), stateImage3.hashCode())
        assertNotEquals(stateImage2.hashCode(), stateImage3.hashCode())
    }

    @Test
    fun testToString() {
        DrawableStateImage(android.R.drawable.btn_radio).apply {
            assertEquals(
                "DrawableStateImage(ResDrawable(${android.R.drawable.btn_radio}))",
                toString()
            )
        }
        DrawableStateImage(android.R.drawable.btn_dialog).apply {
            assertEquals(
                "DrawableStateImage(ResDrawable(${android.R.drawable.btn_dialog}))",
                toString()
            )
        }
    }
}