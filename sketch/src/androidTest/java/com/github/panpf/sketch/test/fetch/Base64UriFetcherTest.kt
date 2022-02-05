package com.github.panpf.sketch.test.fetch

import android.util.Base64
import android.widget.ImageView
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.ByteArrayDataSource
import com.github.panpf.sketch.fetch.Base64UriFetcher
import com.github.panpf.sketch.fetch.newBase64Uri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DownloadRequest
import com.github.panpf.sketch.request.LoadRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Base64UriFetcherTest {

    @Test
    fun testNewUri() {
        Assert.assertEquals(
            "data:image/png;base64,4y2u1412421089084901240129",
            newBase64Uri("image/png", "4y2u1412421089084901240129")
        )
        Assert.assertEquals(
            "data:image/jpeg;base64,4y2u1412421089084901240128",
            newBase64Uri("image/jpeg", "4y2u1412421089084901240128")
        )
    }

    @Test
    fun testFactory() {
        val context = InstrumentationRegistry.getContext()
        val sketch = Sketch.new(context)
        val fetcherFactory = Base64UriFetcher.Factory()
        val base64Uri = "data:image/png;base64,4y2u1412421089084901240129"
        val contentUri = "content://sample_app/sample"
        val imageView = ImageView(context)

        fetcherFactory.create(sketch, LoadRequest(base64Uri))!!.apply {
            Assert.assertEquals("image/png", mimeType)
            Assert.assertEquals("4y2u1412421089084901240129", imageDataBase64StringLazy.value)
        }
        fetcherFactory.create(sketch, DisplayRequest(base64Uri, imageView))!!.apply {
            Assert.assertEquals("image/png", mimeType)
            Assert.assertEquals("4y2u1412421089084901240129", imageDataBase64StringLazy.value)
        }
        Assert.assertNull(fetcherFactory.create(sketch, DownloadRequest(base64Uri)))
        Assert.assertNull(fetcherFactory.create(sketch, LoadRequest(contentUri)))
    }

    @Test
    fun testFetch() {
        val context = InstrumentationRegistry.getContext()
        val sketch = Sketch.new(context)
        val fetcherFactory = Base64UriFetcher.Factory()
        val imageData = "4y2u1412421089084901240129".toByteArray()
        val base64Uri = "data:image/png;base64,${Base64.encodeToString(imageData, Base64.DEFAULT)}"

        val fetcher = fetcherFactory.create(sketch, LoadRequest(base64Uri))!!
        val source = runBlocking {
            fetcher.fetch().dataSource
        }
        Assert.assertEquals(imageData.size.toLong(), source.length())
        Assert.assertTrue(source is ByteArrayDataSource)
    }
}