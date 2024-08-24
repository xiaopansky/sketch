package com.github.panpf.sketch.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.cacheDecodeTimeoutFrame
import com.github.panpf.sketch.sample.AppSettings
import com.github.panpf.sketch.state.StateImage

@Composable
actual fun rememberAnimatedPlaceholderStateImage(context: PlatformContext): StateImage? {
    // Animated svg is not yet supported on non-Android platforms
    return null
}

@Composable
actual inline fun PlatformListImageSettings(
    appSettings: AppSettings,
    builder: ImageRequest.Builder
) {
    val cache by appSettings.cacheDecodeTimeoutFrame.collectAsState()
    builder.cacheDecodeTimeoutFrame(cache)
}