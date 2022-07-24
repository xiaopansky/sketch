package com.github.panpf.sketch.test.request

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.datasource.DataFrom.DOWNLOAD_CACHE
import com.github.panpf.sketch.datasource.DataFrom.MEMORY
import com.github.panpf.sketch.request.DownloadData
import com.github.panpf.sketch.test.utils.getTestContextAndNewSketch
import com.github.panpf.sketch.util.asOrThrow
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.FileInputStream

@RunWith(AndroidJUnit4::class)
class DownloadDataTest {

    @Test
    fun test() {
        val (_, sketch) = getTestContextAndNewSketch()
        val diskCacheKey = "testDiskCacheKey"
        sketch.downloadCache.edit(diskCacheKey)!!.apply {
            newOutputStream().buffered().use {
                it.write(diskCacheKey.toByteArray())
            }
            commit()
        }
        val snapshot = sketch.downloadCache[diskCacheKey]!!

        val bytes = snapshot.newInputStream().use { it.readBytes() }
        DownloadData(bytes, MEMORY).apply {
            Assert.assertSame(bytes, data.asOrThrow<DownloadData.ByteArrayData>().bytes)
            Assert.assertEquals(MEMORY, dataFrom)
            Assert.assertTrue(data.newInputStream().apply { close() } is ByteArrayInputStream)
        }

        DownloadData(snapshot, DOWNLOAD_CACHE).apply {
            Assert.assertSame(snapshot, data.asOrThrow<DownloadData.DiskCacheData>().snapshot)
            Assert.assertEquals(DOWNLOAD_CACHE, dataFrom)
            Assert.assertTrue(data.newInputStream().apply { close() } is FileInputStream)
        }
    }
}