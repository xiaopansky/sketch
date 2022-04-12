/*
 * Copyright (C) 2019 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.sketch.http

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HurlStack(
    val readTimeout: Int,
    val connectTimeout: Int,
    val userAgent: String?,
    val extraHeaders: Map<String, String>?,
    val addExtraHeaders: Map<String, String>?,
    val processRequest: ((url: String, connection: HttpURLConnection) -> Unit)?
) : HttpStack {

    override fun toString(): String =
        "HurlStack(connectTimeout=${connectTimeout},readTimeout=${readTimeout},userAgent=${userAgent})"

    @Throws(IOException::class)
    override fun getResponse(
        sketch: Sketch,
        request: ImageRequest,
        url: String
    ): HttpStack.Response {
        var newUri = url
        while (newUri.isNotEmpty()) {
            val connection = (URL(newUri).openConnection() as HttpURLConnection).apply {
                this@apply.connectTimeout = this@HurlStack.connectTimeout
                this@apply.readTimeout = this@HurlStack.readTimeout
                doInput = true
                if (this@HurlStack.userAgent != null) {
                    setRequestProperty("User-Agent", this@HurlStack.userAgent)
                }
                if (addExtraHeaders != null && addExtraHeaders.isNotEmpty()) {
                    for ((key, value) in addExtraHeaders) {
                        addRequestProperty(key, value)
                    }
                }
                if (extraHeaders != null && extraHeaders.isNotEmpty()) {
                    for ((key, value) in extraHeaders) {
                        setRequestProperty(key, value)
                    }
                }
                request.httpHeaders?.apply {
                    addList.forEach {
                        addRequestProperty(it.first, it.second)
                    }
                    setList.forEach {
                        setRequestProperty(it.first, it.second)
                    }
                }
                processRequest?.invoke(url, this)
            }
            connection.connect()
            val code = connection.responseCode
            if (code == 301 || code == 302 || code == 307) {
                newUri = connection.getHeaderField("Location")
            } else {
                return HurlResponse(connection)
            }
        }
        throw IOException("Unable to get response")
    }

    private class HurlResponse(private val connection: HttpURLConnection) : HttpStack.Response {

        @get:Throws(IOException::class)
        override val code: Int by lazy { connection.responseCode }

        @get:Throws(IOException::class)
        override val message: String? by lazy { connection.responseMessage }

        override val contentLength: Long by lazy {
            connection.getHeaderField("content-length").toLongOrNull() ?: -1
        }

        override val contentType: String? by lazy {
            connection.contentType
        }

        @get:Throws(IOException::class)
        override val content: InputStream
            get() = connection.inputStream

        override fun getHeaderField(name: String): String? {
            return connection.getHeaderField(name)
        }
    }

    class Builder {
        private var readTimeout: Int = HttpStack.DEFAULT_TIMEOUT
        private var connectTimeout: Int = HttpStack.DEFAULT_TIMEOUT
        private var userAgent: String? = null
        private var extraHeaders: MutableMap<String, String>? = null
        private var addExtraHeaders: MutableMap<String, String>? = null
        private var processRequest: ((url: String, connection: HttpURLConnection) -> Unit)? = null

        fun connectTimeout(connectTimeout: Int) = apply {
            this.connectTimeout = connectTimeout
        }

        fun readTimeout(readTimeout: Int): Builder = apply {
            this.readTimeout = readTimeout
        }

        fun userAgent(userAgent: String?): Builder = apply {
            this.userAgent = userAgent
        }

        fun extraHeaders(headers: Map<String, String>): Builder = apply {
            this.extraHeaders = (this.extraHeaders ?: HashMap<String, String>()).apply {
                putAll(headers)
            }
        }

        fun extraHeaders(vararg headers: Pair<String, String>): Builder = apply {
            this.extraHeaders = (this.extraHeaders ?: HashMap<String, String>()).apply {
                putAll(headers.toMap())
            }
        }

        fun addExtraHeaders(headers: Map<String, String>): Builder = apply {
            this.addExtraHeaders = (this.addExtraHeaders ?: HashMap<String, String>()).apply {
                putAll(headers)
            }
        }

        fun addExtraHeaders(vararg headers: Pair<String, String>): Builder = apply {
            this.addExtraHeaders = (this.addExtraHeaders ?: HashMap<String, String>()).apply {
                putAll(headers.toMap())
            }
        }

        fun processRequest(block: (url: String, connection: HttpURLConnection) -> Unit): Builder =
            apply {
                this.processRequest = block
            }

        fun build(): HurlStack = HurlStack(
            readTimeout = readTimeout,
            connectTimeout = connectTimeout,
            userAgent = userAgent,
            extraHeaders = extraHeaders,
            addExtraHeaders = addExtraHeaders,
            processRequest = processRequest,
        )
    }
}