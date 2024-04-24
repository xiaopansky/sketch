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
package com.github.panpf.sketch.core.test.state

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.state.IconAnimatableStateImage
import com.github.panpf.sketch.util.IntColor
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.asEquality
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IconAnimatableStateImageTest {

    @Test
    fun createFunctionTest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val drawableIcon = androidx.core.R.drawable.ic_call_decline.let { context.getDrawable(it)!!.asEquality(it)}
        val resIcon = androidx.core.R.drawable.ic_call_answer
        val drawableBackground = androidx.core.R.drawable.notification_bg.let { context.getDrawable(it)!!.asEquality(it)}
        val resBackground = androidx.core.R.drawable.notification_template_icon_bg
        val intColorBackground = IntColor(Color.BLUE)
        val iconSize = Size(100, 100)
        val intIconTint = IntColor(Color.GREEN)
        val resIconTint = android.R.color.black

        // drawable icon
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = drawableBackground,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = resBackground,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = intColorBackground,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = drawableBackground,
            iconSize = iconSize,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = resBackground,
            iconSize = iconSize,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = intColorBackground,
            iconSize = iconSize,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
            background = drawableBackground,
            iconSize = iconSize,
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = resBackground,
            iconSize = iconSize,
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = intColorBackground,
            iconSize = iconSize,
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
            background = drawableBackground,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = resBackground,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = intColorBackground,
            iconTint = resIconTint
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
            background = drawableBackground,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = resBackground,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = intColorBackground,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            iconSize = iconSize,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
            background = drawableBackground,
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = resBackground,
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            background = intColorBackground,
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
            iconSize = iconSize,
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = drawableIcon,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = drawableIcon,
        )

        // res icon
        IconAnimatableStateImage(
            icon = resIcon,
            background = drawableBackground,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = resBackground,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = intColorBackground,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = drawableBackground,
            iconSize = iconSize,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = resBackground,
            iconSize = iconSize,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = intColorBackground,
            iconSize = iconSize,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = resIcon,
            background = drawableBackground,
            iconSize = iconSize,
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = resBackground,
            iconSize = iconSize,
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = intColorBackground,
            iconSize = iconSize,
        )

        IconAnimatableStateImage(
            icon = resIcon,
            background = drawableBackground,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = resBackground,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = intColorBackground,
            iconTint = resIconTint
        )

        IconAnimatableStateImage(
            icon = resIcon,
            background = drawableBackground,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = resBackground,
            iconTint = intIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = intColorBackground,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = resIcon,
            iconSize = iconSize,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            iconSize = iconSize,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = resIcon,
            background = drawableBackground,
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = resBackground,
        )
        IconAnimatableStateImage(
            icon = resIcon,
            background = intColorBackground,
        )

        IconAnimatableStateImage(
            icon = resIcon,
            iconSize = iconSize,
        )

        IconAnimatableStateImage(
            icon = resIcon,
            iconTint = resIconTint
        )
        IconAnimatableStateImage(
            icon = resIcon,
            iconTint = intIconTint
        )

        IconAnimatableStateImage(
            icon = resIcon,
        )
    }

    @Test
    fun testGetDrawable() {
        val (context, sketch) = getTestContextAndSketch()
        val request = ImageRequest(context, MyImages.jpeg.uri)
        val iconDrawable =
            context.getDrawableCompat(com.github.panpf.sketch.test.utils.R.drawable.ic_animated)
        val greenBgDrawable = ColorDrawable(Color.GREEN)

        IconAnimatableStateImage(iconDrawable) {
            background(greenBgDrawable)
        }.apply {
            getImage(sketch, request, null)
                ?.asOrThrow<DrawableImage>()?.drawable
                .asOrNull<IconAnimatableDrawable>()!!.apply {
                    Assert.assertEquals(iconDrawable, icon)
                    Assert.assertEquals(greenBgDrawable, background)
                    Assert.assertNull(iconSize)
                }
        }

        IconAnimatableStateImage(iconDrawable) {
            iconSize(40)
            resBackground(android.R.drawable.bottom_bar)
        }.apply {
            getImage(sketch, request, null)
                ?.asOrThrow<DrawableImage>()?.drawable
                .asOrNull<IconAnimatableDrawable>()!!.apply {
                    Assert.assertEquals(iconDrawable, icon)
                    Assert.assertTrue(background is BitmapDrawable)
                    Assert.assertEquals(Size(40, 40), iconSize)
                }
        }

        IconAnimatableStateImage(iconDrawable) {
            colorBackground(Color.BLUE)
        }.apply {
            getImage(sketch, request, null)
                ?.asOrThrow<DrawableImage>()?.drawable
                .asOrNull<IconAnimatableDrawable>()!!.apply {
                    Assert.assertEquals(iconDrawable, icon)
                    Assert.assertEquals(Color.BLUE, (background as ColorDrawable).color)
                    Assert.assertNull(iconSize)
                }
        }

        IconAnimatableStateImage(iconDrawable).apply {
            getImage(sketch, request, null)
                ?.asOrThrow<DrawableImage>()?.drawable
                .asOrNull<IconAnimatableDrawable>()!!.apply {
                    Assert.assertEquals(iconDrawable, icon)
                    Assert.assertNull(background)
                    Assert.assertNull(iconSize)
                }
        }


        IconAnimatableStateImage(android.R.drawable.ic_delete) {
            background(greenBgDrawable)
        }.apply {
            Assert.assertNull(getImage(sketch, request, null))
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = IconAnimatableStateImage(android.R.drawable.ic_delete) {
            resBackground(android.R.drawable.bottom_bar)
        }
        val element11 = IconAnimatableStateImage(android.R.drawable.ic_delete) {
            resBackground(android.R.drawable.bottom_bar)
        }
        val element2 = IconAnimatableStateImage(android.R.drawable.ic_delete) {
            resBackground(android.R.drawable.btn_default)
        }
        val element3 = IconAnimatableStateImage(android.R.drawable.btn_star) {
            resBackground(android.R.drawable.bottom_bar)
        }
        val element4 = IconAnimatableStateImage(android.R.drawable.btn_star)

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element1, element3)
        Assert.assertNotSame(element1, element4)
        Assert.assertNotSame(element2, element11)
        Assert.assertNotSame(element2, element3)
        Assert.assertNotSame(element2, element4)
        Assert.assertNotSame(element3, element4)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertNotEquals(element1, element2)
        Assert.assertNotEquals(element1, element3)
        Assert.assertNotEquals(element1, element4)
        Assert.assertNotEquals(element2, element11)
        Assert.assertNotEquals(element2, element3)
        Assert.assertNotEquals(element2, element4)
        Assert.assertNotEquals(element3, element4)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element2.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element3.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element4.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element3.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element4.hashCode())
        Assert.assertNotEquals(element3.hashCode(), element4.hashCode())
    }

    @Test
    fun testToString() {
        IconAnimatableStateImage(android.R.drawable.ic_delete).apply {
            Assert.assertEquals(
                "IconAnimatableStateImage(icon=ResDrawable(${android.R.drawable.ic_delete}), background=null, iconSize=null)",
                toString()
            )
        }
        IconAnimatableStateImage(android.R.drawable.ic_delete) {
            resBackground(android.R.drawable.bottom_bar)
        }.apply {
            Assert.assertEquals(
                "IconAnimatableStateImage(icon=ResDrawable(${android.R.drawable.ic_delete}), background=ResDrawable(${android.R.drawable.bottom_bar}), iconSize=null)",
                toString()
            )
        }
        IconAnimatableStateImage(android.R.drawable.ic_delete) {
            iconSize(50)
        }.apply {
            Assert.assertEquals(
                "IconAnimatableStateImage(icon=ResDrawable(${android.R.drawable.ic_delete}), background=null, iconSize=50x50)",
                toString()
            )
        }
        IconAnimatableStateImage(android.R.drawable.ic_delete) {
            iconSize(50, 30)
        }.apply {
            Assert.assertEquals(
                "IconAnimatableStateImage(icon=ResDrawable(${android.R.drawable.ic_delete}), background=null, iconSize=50x30)",
                toString()
            )
        }
        IconAnimatableStateImage(android.R.drawable.ic_delete) {
            iconSize(Size(44, 67))
            resBackground(android.R.drawable.btn_default)
        }.apply {
            Assert.assertEquals(
                "IconAnimatableStateImage(icon=ResDrawable(${android.R.drawable.ic_delete}), background=ResDrawable(${android.R.drawable.btn_default}), iconSize=44x67)",
                toString()
            )
        }
    }
}