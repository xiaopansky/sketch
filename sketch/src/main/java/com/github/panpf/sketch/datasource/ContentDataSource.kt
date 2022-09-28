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
package com.github.panpf.sketch.datasource

import android.net.Uri
import androidx.annotation.WorkerThread
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Provides access to image data in content resources
 */
class ContentDataSource constructor(
    override val sketch: Sketch,
    override val request: ImageRequest,
    val contentUri: Uri
) : DataSource {

    override val dataFrom: DataFrom = DataFrom.LOCAL

    private var _length = -1L

    @WorkerThread
    @Throws(IOException::class)
    override fun length(): Long =
        _length.takeIf { it != -1L }
            ?: (request.context.contentResolver.openFileDescriptor(contentUri, "r")
                ?.use {
                    it.statSize
                } ?: throw IOException("Invalid content uri: $contentUri")).apply {
                this@ContentDataSource._length = this
            }

    @WorkerThread
    @Throws(IOException::class)
    override fun newInputStream(): InputStream =
        request.context.contentResolver.openInputStream(contentUri)
            ?: throw IOException("Invalid content uri: $contentUri")

    @WorkerThread
    @Throws(IOException::class)
    override fun file(): File =
        if (contentUri.scheme.equals("file", ignoreCase = true)) {
            File(contentUri.toString().substring("file://".length))
        } else {
            super.file()
        }

    override fun toString(): String = "ContentDataSource('$contentUri')"
}