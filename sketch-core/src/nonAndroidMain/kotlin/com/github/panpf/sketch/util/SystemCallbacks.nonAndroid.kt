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

import com.github.panpf.sketch.Sketch
import kotlinx.atomicfu.atomic

/**
 * Create an instance of [SystemCallbacks] for non-Android platforms
 *
 * @see com.github.panpf.sketch.core.nonandroid.test.util.SystemCallbacksNonAndroidTest.testSystemCallbacks
 */
internal actual fun SystemCallbacks(sketch: Sketch): SystemCallbacks = NoopSystemCallbacks()

/**
 * Noop implementation of [SystemCallbacks]
 *
 * @see com.github.panpf.sketch.core.nonandroid.test.util.SystemCallbacksNonAndroidTest.testNoopSystemCallbacks
 */
private class NoopSystemCallbacks : SystemCallbacks {

    // TODO Implement network type detection for non-Android platforms. https://github.com/jordond/connectivity/blob/main/connectivity-apple/src/appleMain/kotlin/dev/jordond/connectivity/internal/AppleConnectivityProvider.kt
    override val isCellularNetworkConnected get() = false

    private val _isShutdown = atomic(false)
    override var isShutdown: Boolean by _isShutdown

    // TODO Listen for memory-pressure events to trim the memory cache on non-Android platforms.
    override fun register() {

    }

    override fun shutdown() {
        if (_isShutdown.getAndSet(true)) return
    }
}
