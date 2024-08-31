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

package com.github.panpf.sketch.fetch

import android.annotation.SuppressLint
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.WorkerThread
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.drawable.ResDrawable
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.sketch.source.DrawableDataSource
import com.github.panpf.sketch.source.ResourceDataSource
import com.github.panpf.sketch.util.MimeTypeMap
import com.github.panpf.sketch.util.Uri


/**
 * Get image resources from [ImageRequest].context.resources using resource type and resource name
 * <br>
 * Sample: 'android.resource:///drawable/ic_launcher'
 *
 * @see com.github.panpf.sketch.core.android.test.fetch.ResourceUriFetcherTest.testNewResourceUri
 */
fun newResourceUri(resType: String, resName: String): String =
    "${ResourceUriFetcher.SCHEME}:///$resType/$resName"

/**
 * Get image resources from [ImageRequest].context.resources using resource id
 * <br>
 * Sample: 'android.resource:///1031232'
 *
 * @see com.github.panpf.sketch.core.android.test.fetch.ResourceUriFetcherTest.testNewResourceUri
 */
fun newResourceUri(resId: Int): String = "${ResourceUriFetcher.SCHEME}:///$resId"

/**
 * Get the image resource from the specified package using the resource type and resource name
 * <br>
 * Sample: 'android.resource://com.github.panpf.sketch.sample/drawable/ic_launcher'
 *
 * @see com.github.panpf.sketch.core.android.test.fetch.ResourceUriFetcherTest.testNewResourceUri
 */
fun newResourceUri(packageName: String, resType: String, resName: String): String =
    "${ResourceUriFetcher.SCHEME}://$packageName/$resType/$resName"

/**
 * Get the image resource from the specified package using the resource id
 * <br>
 * Sample: 'android.resource://com.github.panpf.sketch.sample/1031232'
 *
 * @see com.github.panpf.sketch.core.android.test.fetch.ResourceUriFetcherTest.testNewResourceUri
 */
fun newResourceUri(packageName: String, resId: Int): String =
    "${ResourceUriFetcher.SCHEME}://$packageName/$resId"

/**
 * Check if the uri is a resource uri
 *
 * Support the following uri:
 * * 'android.resource:///drawable/ic_launcher'
 * * 'android.resource:///1031232'
 * * 'android.resource://com.github.panpf.sketch.sample/drawable/ic_launcher'
 * * 'android.resource://com.github.panpf.sketch.sample/1031232'
 *
 * @see com.github.panpf.sketch.core.android.test.fetch.ResourceUriFetcherTest.testIsResourceUri
 */
fun isResourceUri(uri: Uri): Boolean =
    ResourceUriFetcher.SCHEME.equals(uri.scheme, ignoreCase = true)

/**
 * Support the following uri:
 * * 'android.resource:///drawable/ic_launcher'
 * * 'android.resource:///1031232'
 * * 'android.resource://com.github.panpf.sketch.sample/drawable/ic_launcher'
 * * 'android.resource://com.github.panpf.sketch.sample/1031232'
 *
 * @see com.github.panpf.sketch.core.android.test.fetch.ResourceUriFetcherTest
 */
class ResourceUriFetcher(
    val sketch: Sketch, // TODO remove
    val request: ImageRequest,  // TODO change to context
    val resourceUri: Uri,
) : Fetcher {

    companion object {
        const val SCHEME = "android.resource"
    }

    @WorkerThread
    @SuppressLint("DiscouragedApi")
    override suspend fun fetch(): Result<FetchResult> = kotlin.runCatching {
        val packageName = resourceUri.authority
            ?.takeIf { it.isNotEmpty() }
            ?: request.context.packageName

        val resources: Resources = try {
            request.context.packageManager.getResourcesForApplication(packageName)
        } catch (ex: NameNotFoundException) {
            throw Resources.NotFoundException("Not found Resources by packageName: $resourceUri")
        }

        val paths = resourceUri.pathSegments
        val resId = when (paths.size) {
            1 -> paths.first().toInt()
            2 -> resources.getIdentifier(
                /* name= */ paths.last(),
                /* defType= */ paths.first(),
                /* defPackage= */ packageName
            ).takeIf { it != 0 }
                ?: throw Resources.NotFoundException("No found resource identifier by resType, resName: $resourceUri")

            else -> throw Resources.NotFoundException("Invalid resource uri: $resourceUri")
        }

        val path =
            TypedValue().apply { resources.getValue(resId, this, true) }.string ?: ""
        val entryName = path.lastIndexOf('/').takeIf { it != -1 }
            ?.let { path.substring(it + 1) }
            ?: path.toString()
        val mimeType = MimeTypeMap.getMimeTypeFromUrl(entryName)
        val dataSource = if (resources.getResourceTypeName(resId) == "raw") {
            ResourceDataSource(
                resources = resources,
                packageName = packageName,
                resId = resId,
            )
        } else {
            DrawableDataSource(
                context = request.context,
                dataFrom = DataFrom.LOCAL,
                drawableFetcher = ResDrawable(packageName, resources, resId)
            )
        }
        FetchResult(dataSource, mimeType)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ResourceUriFetcher
        if (sketch != other.sketch) return false
        if (request != other.request) return false
        if (resourceUri != other.resourceUri) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sketch.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + resourceUri.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResourceUriFetcher('$resourceUri')"
    }

    class Factory : Fetcher.Factory {

        override fun create(sketch: Sketch, request: ImageRequest): ResourceUriFetcher? {
            val uri = request.uri
            if (!isResourceUri(uri)) return null
            return ResourceUriFetcher(sketch, request, uri)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String = "ResourceUriFetcher"
    }
}