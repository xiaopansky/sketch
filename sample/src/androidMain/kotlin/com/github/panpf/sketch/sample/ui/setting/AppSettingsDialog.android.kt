package com.github.panpf.sketch.sample.ui.setting

import android.graphics.ColorSpace.Named
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.github.panpf.sketch.sample.AppSettings
import com.github.panpf.sketch.sample.ui.MyEvents

@Composable
actual fun getSettingsDialogHeight(): Dp {
    return with(LocalDensity.current) {
        (LocalContext.current.resources.displayMetrics.heightPixels * 0.8f).toInt().toDp()
    }
}

actual fun platformMakeDecodeMenuList(appSettings: AppSettings): List<SettingItem> = buildList {
    add(
        DropdownSettingItem(
            title = "Bitmap Quality",
            desc = null,
            values = listOf("Default", "LOW", "HIGH"),
            state = appSettings.bitmapQuality,
        )
    )
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
        // Cannot use Named.entries, crashes on versions lower than O
        val items = listOf("Default").plus(Named.values().map { it.name })
        add(
            DropdownSettingItem(
                title = "Color Space",
                desc = null,
                values = items,
                state = appSettings.colorSpace,
            )
        )
    }
    if (VERSION.SDK_INT <= VERSION_CODES.M) {
        add(
            SwitchSettingItem(
                title = "inPreferQualityOverSpeed",
                desc = null,
                state = appSettings.inPreferQualityOverSpeed
            )
        )
    }
}

actual fun platformMakeOtherMenuList(appSettings: AppSettings): List<SettingItem> = buildList {
    add(
        DropdownSettingItem(
            title = "Http Engine",
            desc = null,
            values = listOf("Ktor", "OkHttp", "HttpURLConnection"),
            state = appSettings.httpEngine,
            onItemClick = {
                MyEvents.toastFlow.emit("Restart the app to take effect")
            }
        )
    )
    add(
        DropdownSettingItem(
            title = "Video Frame Decoder",
            desc = null,
            values = listOf("FFmpeg", "AndroidBuiltIn"),
            state = appSettings.videoFrameDecoder,
            onItemClick = {
                MyEvents.toastFlow.emit("Restart the app to take effect")
            }
        )
    )
    add(
        DropdownSettingItem(
            title = "Gif Decoder",
            desc = null,
            values = listOf("KoralGif", "ImageDecoder+Movie"),
            state = appSettings.gifDecoder,
            onItemClick = {
                MyEvents.toastFlow.emit("Restart the app to take effect")
            }
        )
    )
}