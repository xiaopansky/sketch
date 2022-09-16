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
package com.github.panpf.sketch.test.stateimage

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.resize.Precision.EXACTLY
import com.github.panpf.sketch.resize.Precision.LESS_PIXELS
import com.github.panpf.sketch.stateimage.ColorStateImage
import com.github.panpf.sketch.stateimage.InexactlyMemoryCacheStateImage
import com.github.panpf.sketch.stateimage.IntColor
import com.github.panpf.sketch.test.utils.TestAssets
import com.github.panpf.sketch.test.utils.getTestContextAndNewSketch
import com.github.panpf.sketch.transform.CircleCropTransformation
import com.github.panpf.sketch.transform.RotateTransformation
import com.github.panpf.sketch.transform.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InexactlyMemoryCacheStateImageTest {

    @Test
    fun testGetDrawable() {
        val (context, sketch) = getTestContextAndNewSketch()

        val memoryCache = sketch.memoryCache
        Assert.assertEquals(0, memoryCache.keys().size)

        val requests1 = arrayOf(
            DisplayRequest(context, TestAssets.SAMPLE_JPEG_URI) {
                resize(100, 100, LESS_PIXELS)
            },
            DisplayRequest(context, TestAssets.SAMPLE_JPEG_URI) {
                resize(100, 100, EXACTLY)
            },
            DisplayRequest(context, TestAssets.SAMPLE_JPEG_URI) {
                resize(100, 100, LESS_PIXELS)
                transformations(CircleCropTransformation())
            },
        )
        val requests2 = arrayOf(
            DisplayRequest(context, TestAssets.SAMPLE_PNG_URI) {
                resize(100, 100, LESS_PIXELS)
            },
            DisplayRequest(context, TestAssets.SAMPLE_PNG_URI) {
                resize(100, 100, EXACTLY)
            },
            DisplayRequest(context, TestAssets.SAMPLE_PNG_URI) {
                resize(100, 100, LESS_PIXELS)
                transformations(CircleCropTransformation())
            },
        )

        Assert.assertEquals(
            6,
            requests1.map { it.key }.plus(requests2.map { it.key }).distinct().size
        )

        runBlocking(Dispatchers.Main) {
            val inexactlyStateImage = InexactlyMemoryCacheStateImage()
            val inexactlyStateImage1 = InexactlyMemoryCacheStateImage(requests1[0].uriString)
            val inexactlyStateImage2 = InexactlyMemoryCacheStateImage(requests2[0].uriString)

            Assert.assertEquals(0, memoryCache.keys().size)
            requests1.plus(requests2).forEach { request ->
                Assert.assertNull(request.key, memoryCache[request.key])
            }
            requests1.plus(requests2).forEach { request ->
                Assert.assertNull(
                    request.key,
                    inexactlyStateImage.getDrawable(sketch, request, null)
                )
                Assert.assertNull(
                    request.key,
                    inexactlyStateImage1.getDrawable(sketch, request, null)
                )
                Assert.assertNull(
                    request.key,
                    inexactlyStateImage2.getDrawable(sketch, request, null)
                )
            }

            val testRequests1: suspend (Int) -> Unit = { loadIndex ->
                memoryCache.clear()
                Assert.assertEquals(0, memoryCache.keys().size)
                sketch.enqueue(requests1[loadIndex]).job.await()
                Assert.assertEquals(1, memoryCache.keys().size)
                requests1.forEachIndexed { index, request ->
                    if (index == loadIndex) {
                        Assert.assertNotNull(request.key, memoryCache[request.key])
                    } else {
                        Assert.assertNull(request.key, memoryCache[request.key])
                    }
                }
                requests2.forEach { request ->
                    Assert.assertNull(request.key, memoryCache[request.key])
                }
                requests1.forEach { request ->
                    if (loadIndex == 0) {
                        Assert.assertNotNull(
                            request.key,
                            inexactlyStateImage.getDrawable(sketch, request, null)
                        )
                        Assert.assertNotNull(
                            request.key,
                            inexactlyStateImage1.getDrawable(sketch, request, null)
                        )
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage2.getDrawable(sketch, request, null)
                        )
                    } else {
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage.getDrawable(sketch, request, null)
                        )
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage1.getDrawable(sketch, request, null)
                        )
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage2.getDrawable(sketch, request, null)
                        )
                    }
                }
                requests2.forEach { request ->
                    Assert.assertNull(inexactlyStateImage.getDrawable(sketch, request, null))
                    if (loadIndex == 0) {
                        Assert.assertNotNull(
                            request.key,
                            inexactlyStateImage1.getDrawable(sketch, request, null)
                        )
                    } else {
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage1.getDrawable(sketch, request, null)
                        )
                    }
                    Assert.assertNull(
                        request.key,
                        inexactlyStateImage2.getDrawable(sketch, request, null)
                    )
                }
            }
            testRequests1(0)
            testRequests1(1)
            testRequests1(2)

            val testRequests2: suspend (Int) -> Unit = { loadIndex ->
                memoryCache.clear()
                Assert.assertEquals(0, memoryCache.keys().size)
                sketch.enqueue(requests2[loadIndex]).job.await()
                Assert.assertEquals(1, memoryCache.keys().size)
                requests1.forEach { request ->
                    Assert.assertNull(request.key, memoryCache[request.key])
                }
                requests2.forEachIndexed { index, request ->
                    if (index == loadIndex) {
                        Assert.assertNotNull(request.key, memoryCache[request.key])
                    } else {
                        Assert.assertNull(request.key, memoryCache[request.key])
                    }
                }
                requests1.forEach { request ->
                    Assert.assertNull(inexactlyStateImage.getDrawable(sketch, request, null))
                    Assert.assertNull(
                        request.key,
                        inexactlyStateImage1.getDrawable(sketch, request, null)
                    )
                    if (loadIndex == 0) {
                        Assert.assertNotNull(
                            request.key,
                            inexactlyStateImage2.getDrawable(sketch, request, null)
                        )
                    } else {
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage2.getDrawable(sketch, request, null)
                        )
                    }
                }
                requests2.forEach { request ->
                    if (loadIndex == 0) {
                        Assert.assertNotNull(
                            request.key,
                            inexactlyStateImage.getDrawable(sketch, request, null)
                        )
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage1.getDrawable(sketch, request, null)
                        )
                        Assert.assertNotNull(
                            request.key,
                            inexactlyStateImage2.getDrawable(sketch, request, null)
                        )
                    } else {
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage.getDrawable(sketch, request, null)
                        )
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage1.getDrawable(sketch, request, null)
                        )
                        Assert.assertNull(
                            request.key,
                            inexactlyStateImage2.getDrawable(sketch, request, null)
                        )
                    }
                }
            }
            testRequests2(0)
            testRequests2(1)
            testRequests2(2)

            memoryCache.clear()
            Assert.assertEquals(0, memoryCache.keys().size)
            sketch.enqueue(requests1[0]).job.await()
            sketch.enqueue(requests2[0]).job.await()
            Assert.assertEquals(2, memoryCache.keys().size)
            requests1.forEachIndexed { index, request ->
                if (index == 0) {
                    Assert.assertNotNull(memoryCache[request.key])
                } else {
                    Assert.assertNull(memoryCache[request.key])
                }
            }
            requests2.forEachIndexed { index, request ->
                if (index == 0) {
                    Assert.assertNotNull(memoryCache[request.key])
                } else {
                    Assert.assertNull(memoryCache[request.key])
                }
            }
            requests1.forEach { request ->
                Assert.assertNotNull(inexactlyStateImage.getDrawable(sketch, request, null))
                Assert.assertNotNull(inexactlyStateImage1.getDrawable(sketch, request, null))
                Assert.assertNotNull(inexactlyStateImage2.getDrawable(sketch, request, null))
            }
            requests2.forEach { request ->
                Assert.assertNotNull(inexactlyStateImage.getDrawable(sketch, request, null))
                Assert.assertNotNull(inexactlyStateImage1.getDrawable(sketch, request, null))
                Assert.assertNotNull(inexactlyStateImage2.getDrawable(sketch, request, null))
            }

            memoryCache.clear()
            Assert.assertEquals(0, memoryCache.keys().size)
            sketch.enqueue(requests1[1]).job.await()
            sketch.enqueue(requests1[2]).job.await()
            sketch.enqueue(requests1[2].newDisplayRequest {
                transformations(listOf(RoundedCornersTransformation()))
            }).job.await()
            sketch.enqueue(requests1[2].newDisplayRequest {
                transformations(listOf(RotateTransformation(90)))
            }).job.await()
            Assert.assertNull(inexactlyStateImage.getDrawable(sketch, requests1[0], null))
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = InexactlyMemoryCacheStateImage("uri1", ColorStateImage(IntColor(Color.BLUE)))
        val element11 =
            InexactlyMemoryCacheStateImage("uri1", ColorStateImage(IntColor(Color.BLUE)))
        val element2 =
            InexactlyMemoryCacheStateImage("uri1", ColorStateImage(IntColor(Color.GREEN)))
        val element3 = InexactlyMemoryCacheStateImage("uri2", ColorStateImage(IntColor(Color.BLUE)))
        val element4 = InexactlyMemoryCacheStateImage(null, ColorStateImage(IntColor(Color.BLUE)))
        val element5 = InexactlyMemoryCacheStateImage("uri1", null)

        Assert.assertNotSame(element1, element11)
        Assert.assertNotSame(element1, element2)
        Assert.assertNotSame(element1, element3)
        Assert.assertNotSame(element1, element4)
        Assert.assertNotSame(element1, element5)
        Assert.assertNotSame(element2, element11)
        Assert.assertNotSame(element2, element3)
        Assert.assertNotSame(element2, element4)
        Assert.assertNotSame(element2, element5)
        Assert.assertNotSame(element3, element4)
        Assert.assertNotSame(element3, element5)
        Assert.assertNotSame(element4, element5)

        Assert.assertEquals(element1, element1)
        Assert.assertEquals(element1, element11)
        Assert.assertNotEquals(element1, element2)
        Assert.assertNotEquals(element1, element3)
        Assert.assertNotEquals(element1, element4)
        Assert.assertNotEquals(element1, element5)
        Assert.assertNotEquals(element2, element11)
        Assert.assertNotEquals(element2, element3)
        Assert.assertNotEquals(element2, element4)
        Assert.assertNotEquals(element2, element5)
        Assert.assertNotEquals(element3, element4)
        Assert.assertNotEquals(element3, element5)
        Assert.assertNotEquals(element4, element5)
        Assert.assertNotEquals(element1, null)
        Assert.assertNotEquals(element1, Any())

        Assert.assertEquals(element1.hashCode(), element1.hashCode())
        Assert.assertEquals(element1.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element2.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element3.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element4.hashCode())
        Assert.assertNotEquals(element1.hashCode(), element5.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element11.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element3.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element4.hashCode())
        Assert.assertNotEquals(element2.hashCode(), element5.hashCode())
        Assert.assertNotEquals(element3.hashCode(), element4.hashCode())
        Assert.assertNotEquals(element3.hashCode(), element5.hashCode())
        Assert.assertNotEquals(element4.hashCode(), element5.hashCode())
    }

    @Test
    fun testToString() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val request = DisplayRequest(context, newAssetUri("sample.jpeg"))
        val uriString = request.uriString

        InexactlyMemoryCacheStateImage(uriString, ColorStateImage(IntColor(Color.BLUE))).apply {
            Assert.assertEquals(
                "InexactlyMemoryCacheStateImage(uri=$uriString, defaultImage=ColorStateImage(IntColor(${Color.BLUE})))",
                toString()
            )
        }
        InexactlyMemoryCacheStateImage(uriString, ColorStateImage(IntColor(Color.GREEN))).apply {
            Assert.assertEquals(
                "InexactlyMemoryCacheStateImage(uri=$uriString, defaultImage=ColorStateImage(IntColor(${Color.GREEN})))",
                toString()
            )
        }
        InexactlyMemoryCacheStateImage(null, null).apply {
            Assert.assertEquals(
                "InexactlyMemoryCacheStateImage(uri=null, defaultImage=null)",
                toString()
            )
        }
    }
}