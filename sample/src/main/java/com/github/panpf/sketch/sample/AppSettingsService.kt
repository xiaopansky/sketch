package com.github.panpf.sketch.sample

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.panpf.sketch.resize.Scale
import com.github.panpf.sketch.sample.model.LayoutMode
import com.github.panpf.sketch.sample.model.LayoutMode.GRID
import com.github.panpf.sketch.sample.util.prefsdata.BooleanPrefsData
import com.github.panpf.sketch.sample.util.prefsdata.EnumPrefsData
import com.github.panpf.sketch.sample.util.prefsdata.StringPrefsData
import com.github.panpf.sketch.util.Logger

class AppSettingsService(val context: Context) {

    val photoListLayoutMode by lazy {
        EnumPrefsData(
            context,
            "photoListLayoutMode",
            GRID,
            { LayoutMode.valueOf(it) },
            { it.name }
        )
    }
    val disabledAnimatedImageInList by lazy {
        BooleanPrefsData(context, "disabledAnimatedImageInList", false)
    }

    val showMimeTypeLogoInLIst by lazy {
        BooleanPrefsData(context, "showMimeTypeLogoInLIst", true)
    }
    val showProgressIndicatorInList by lazy {
        BooleanPrefsData(context, "showProgressIndicatorInList", true)
    }
    val saveCellularTrafficInList by lazy {
        BooleanPrefsData(context, "saveCellularTrafficInList", false)
    }
    val pauseLoadWhenScrollInList by lazy {
        BooleanPrefsData(context, "pauseLoadWhenScrollInList", false)
    }
    val resizePrecision by lazy {
        StringPrefsData(context, "resizePrecision", "LESS_PIXELS")
    }
    val otherResizeScale by lazy {
        StringPrefsData(context, "otherResizeScale", Scale.CENTER_CROP.name)
    }
    val longImageResizeScale by lazy {
        StringPrefsData(context, "longImageResizeScale", Scale.START_CROP.name)
    }

    val inPreferQualityOverSpeed by lazy {
        BooleanPrefsData(context, "inPreferQualityOverSpeed", false)
    }
    val bitmapQuality by lazy {
        StringPrefsData(context, "bitmapQuality", "MIDDEN")
    }
    @get:RequiresApi(Build.VERSION_CODES.O)
    val colorSpace by lazy {
        StringPrefsData(context, "colorSpace", "Default")
    }
    val ignoreExifOrientation by lazy {
        BooleanPrefsData(context, "ignoreExifOrientation", false)
    }

    val disabledBitmapMemoryCache by lazy {
        BooleanPrefsData(context, "disabledBitmapMemoryCache", false)
    }
    val disabledBitmapResultDiskCache by lazy {
        BooleanPrefsData(context, "disabledBitmapResultDiskCache", false)
    }
    val disabledDownloadDiskCache by lazy {
        BooleanPrefsData(context, "disabledDownloadDiskCache", false)
    }
    val disabledReuseBitmap by lazy {
        BooleanPrefsData(context, "disabledReuseBitmap", false)
    }

    val showDataFromLogo by lazy {
        BooleanPrefsData(context, "showDataFrom", true)
    }
    val showTileBoundsInHugeImagePage by lazy {
        BooleanPrefsData(context, "showTileBoundsInHugeImagePage", true)
    }

    val logLevel by lazy {
        StringPrefsData(context, "logLevel", Logger.Level.DEBUG.name)
    }

//    val readModeEnabled by lazy {
//        BooleanPrefsData(context, "readModeEnabled", true)
//    }
//    val pauseBlockDisplayWhenPageNoVisibleEnabled by lazy {
//        BooleanPrefsData(context, "pauseBlockDisplayWhenPageNoVisibleEnabled", true)
//    }
//    val threeLevelZoomModeEnabled by lazy {
//        BooleanPrefsData(context, "threeLevelZoomModeEnabled", false)
//    }
//    val smallMapLocationAnimateEnabled by lazy {
//        BooleanPrefsData(context, "smallMapLocationAnimateEnabled", true)
//    }
//
//    val clickPlayGifEnabled by lazy {
//        BooleanPrefsData(context, "clickPlayGifEnabled", false)
//    }
//
//    val showRoundedInPhotoListEnabled by lazy {
//        BooleanPrefsData(context, "showRoundedInPhotoListEnabled", false)
//    }
//    val showRawImageInDetailEnabled by lazy {
//        BooleanPrefsData(context, "showRawImageInDetailEnabled", false)
//    }
}