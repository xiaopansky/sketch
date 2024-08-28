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

package com.github.panpf.sketch.source

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.annotation.WorkerThread
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.source.DataFrom.LOCAL
import okio.IOException
import okio.Path
import okio.Source

/**
 * Provides access to local file image data
 */
class FileDataSource constructor(
    override val sketch: Sketch,
    override val request: ImageRequest,
    val path: Path,
    override val dataFrom: DataFrom = LOCAL,
) : DataSource {

    @WorkerThread
    @Throws(IOException::class)
    override fun openSourceOrNull(): Source = sketch.fileSystem.source(path)

    @WorkerThread
    @Throws(IOException::class)
    override fun getFileOrNull(): Path = path

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as FileDataSource
        if (sketch != other.sketch) return false
        if (request != other.request) return false
        if (path != other.path) return false
        if (dataFrom != other.dataFrom) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sketch.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + dataFrom.hashCode()
        return result
    }

    override fun toString(): String = "FileDataSource('${path}')"
}