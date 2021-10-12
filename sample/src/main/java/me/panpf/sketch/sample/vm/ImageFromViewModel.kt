package me.panpf.sketch.sample.vm

import android.app.Application
import android.content.ContentUris
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.panpf.sketch.sample.AssetImage
import me.panpf.sketch.sample.BuildConfig
import me.panpf.sketch.sample.R
import me.panpf.sketch.sample.base.LifecycleAndroidViewModel
import me.panpf.sketch.uri.AndroidResUriModel
import me.panpf.sketch.uri.ApkIconUriModel
import me.panpf.sketch.uri.AppIconUriModel
import me.panpf.sketch.uri.DrawableUriModel

class ImageFromViewModel(application1: Application) : LifecycleAndroidViewModel(application1) {

    val data = MutableLiveData<ImageFromData>()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val localFirstPhotoPath = loadLocalFirstPhotoPath()
            val localSecondPhotoUri = loadLocalSecondPhotoUri()
            val localFirstVideoPath = loadLocalFirstVideoPath()
            val headerUserPackageInfo = loadUserAppPackageInfo(true)
            val footerUserPackageInfo = loadUserAppPackageInfo(false)
            val datas = ArrayList<Pair<String, String>>().apply {
                add("HTTP" to "http://b.zol-img.com.cn/desk/bizhi/image/4/1366x768/1387347695254.jpg")
                add("HTTPS" to "https://images.unsplash.com/photo-1431440869543-efaf3388c585?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=1080&fit=max&s=8b00971a3e4a84fb43403797126d1991%22")
                if (localSecondPhotoUri != null) {
                    add("CONTENT" to localSecondPhotoUri.toString())
                }
                if (localFirstPhotoPath != null) {
                    add("FILE" to localFirstPhotoPath)
                }
                add("ASSET" to AssetImage.MEI_NV)
                add("DRAWABLE" to DrawableUriModel.makeUri(R.drawable.image_loading))
                add(
                    "ANDROID_RES" to AndroidResUriModel.makeUriByName(
                        BuildConfig.APPLICATION_ID,
                        "mipmap",
                        "ic_launcher"
                    )
                )
                add(
                    "APP" to AppIconUriModel.makeUri(
                        headerUserPackageInfo.packageName,
                        headerUserPackageInfo.versionCode
                    )
                )
                add("APK" to ApkIconUriModel.makeUri(footerUserPackageInfo.applicationInfo.publicSourceDir))
                if (localFirstVideoPath != null) {
                    add("VIDEO THUMBNAIL" to localFirstVideoPath)
                }
                add("BASE64" to BASE64_IMAGE)
            }
            val uris = datas.map { it.second }.toTypedArray()
            val titles = datas.map { it.first }.toTypedArray()
            data.postValue(ImageFromData(titles, uris))
        }
    }

    private suspend fun loadUserAppPackageInfo(fromHeader: Boolean): PackageInfo {
        return withContext(Dispatchers.IO) {
            val packageList =
                application1.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            (if (fromHeader) {
                packageList.find {
                    it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }
            } else {
                packageList.findLast {
                    it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }
            } ?: application1.packageManager.getPackageInfo(application1.packageName, 0))
        }
    }

    private suspend fun loadLocalFirstPhotoPath(): String? {
        return withContext(Dispatchers.IO) {
            val cursor = application1.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC" + " limit " + 0 + "," + 1
            )
            var imagePath: String? = null
            cursor?.use {
                if (cursor.moveToNext()) {
                    imagePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                }
            }
            imagePath
        }
    }

    private suspend fun loadLocalSecondPhotoUri(): Uri? {
        return withContext(Dispatchers.IO) {
            val cursor = application1.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                null,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC" + " limit " + 1 + "," + 1
            )
            var imageId: Long? = null
            cursor?.use {
                if (cursor.moveToNext()) {
                    imageId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                }
            }
            if (imageId != null) {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId!!)
            } else {
                null
            }
        }
    }

    private suspend fun loadLocalFirstVideoPath(): String? {
        return withContext(Dispatchers.IO) {
            val cursor = application1.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Video.Media.DATA,
                ),
                null,
                null,
                MediaStore.Video.Media.DATE_TAKEN + " DESC" + " limit " + 0 + "," + 1
            )
            var imagePath: String? = null
            cursor?.use {
                if (cursor.moveToNext()) {
                    imagePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                }
            }
            imagePath
        }
    }

    class ImageFromData(val titles: Array<String>, val uris: Array<String>)

    companion object {
        const val BASE64_IMAGE =
            "data:image/jpeg;base64,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AAEAbGZmAAYAAAAAAAEAL2ZmAAEAoZmaAAYAAAAAAAEAMgAAAAEAWgAAAAYAAAAAAAEANQAAAAEALQAAAAYAAAAAAAE4QklNA/gAAAAAAHAAAP////////////////////////////8D6AAAAAD/////////////////////////////A+gAAAAA/////////////////////////////wPoAAAAAP////////////////////////////8D6AAAOEJJTQQAAAAAAAACAAA4QklNBAIAAAAAAAIAADhCSU0EMAAAAAAAAQEAOEJJTQQtAAAAAAAGAAEAAAACOEJJTQQIAAAAAAAQAAAAAQAAAkAAAAJAAAAAADhCSU0EHgAAAAAABAAAAAA4QklNBBoAAAAAAz0AAAAGAAAAAAAAAAAAAAApAAAAMgAAAAQAMQAwADMAOAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAMgAAACkAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAQAAAAAAAG51bGwAAAACAAAABmJvdW5kc09iamMAAAABAAAAAAAAUmN0MQAAAAQAAAAAVG9wIGxvbmcAAAAAAAAAAExlZnRsb25nAAAAAAAAAABCdG9tbG9uZwAAACkAAAAAUmdodGxvbmcAAAAyAAAABnNsaWNlc1ZsTHMAAAABT2JqYwAAAAEAAAAAAAVzbGljZQAAABIAAAAHc2xpY2VJRGxvbmcAAAAAAAAAB2dyb3VwSURsb25nAAAAAAAAAAZvcmlnaW5lbnVtAAAADEVTbGljZU9yaWdpbgAAAA1hdXRvR2VuZXJhdGVkAAAAAFR5cGVlbnVtAAAACkVTbGljZVR5cGUAAAAASW1nIAAAAAZib3VuZHNPYmpjAAAAAQAAAAAAAFJjdDEAAAAEAAAAAFRvcCBsb25nAAAAAAAAAABMZWZ0bG9uZwAAAAAAAAAAQnRvbWxvbmcAAAApAAAAAFJnaHRsb25nAAAAMgAAAAN1cmxURVhUAAAAAQAAAAAAAG51bGxURVhUAAAAAQAAAAAAAE1zZ2VURVhUAAAAAQAAAAAABmFsdFRhZ1RFWFQAAAABAAAAAAAOY2VsbFRleHRJc0hUTUxib29sAQAAAAhjZWxsVGV4dFRFWFQA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ME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0RXZ0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOkRvY3VtZW50SUQ9IjhGQzVFMTUzOUIzMDE0MTYzRkI2NjQzQ0FBRTgxOTk5IiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjAyODAxMTc0MDcyMDY4MTE4MDgzQkMxNDRGODAzODZBIiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9IjhGQzVFMTUzOUIzMDE0MTYzRkI2NjQzQ0FBRTgxOTk5IiBkYzpmb3JtYXQ9ImltYWdlL2pwZWciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHhtcDpDcmVhdGVEYXRlPSIyMDE2LTA0LTIzVDEzOjE5OjA0KzA4OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAxNy0wNS0yM1QxODo1Nzo1NiswODowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAxNy0wNS0yM1QxODo1Nzo1NiswODowMCI+IDx4bXBNTTpIaXN0b3J5PiA8cmRmOlNlcT4gPHJkZjpsaSBzdEV2dDphY3Rpb249InNhdmVkIiBzdEV2dDppbnN0YW5jZUlEPSJ4bXAuaWlkOjAxODAxMTc0MDcyMDY4MTE4MDgzQkMxNDRGODAzODZBIiBzdEV2dDp3aGVuPSIyMDE3LTA1LTIzVDE4OjU3OjU2KzA4OjAwIiBzdEV2dDpzb2Z0d2FyZUFnZW50PSJBZG9iZSBQaG90b3Nob3AgQ1M2IChNYWNpbnRvc2gpIiBzdEV2dDpjaGFuZ2VkPSIvIi8+IDxyZGY6bGkgc3RFdnQ6YWN0aW9uPSJzYXZlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDowMjgwMTE3NDA3MjA2ODExODA4M0JDMTQ0RjgwMzg2QSIgc3RFdnQ6d2hlbj0iMjAxNy0wNS0yM1QxODo1Nzo1NiswODowMCIgc3RFdnQ6c29mdHdhcmVBZ2VudD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoTWFjaW50b3NoKSIgc3RFdnQ6Y2hhbmdlZD0iLyIvPiA8L3JkZjpTZXE+IDwveG1wTU06SGlzdG9yeT4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPD94cGFja2V0IGVuZD0idyI/Pv/uAA5BZG9iZQBkAAAAAAH/2wCEAAYEBAQFBAYFBQYJBgUGCQsIBgYICwwKCgsKCgwQDAwMDAwMEAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwBBwcHDQwNGBAQGBQODg4UFA4ODg4UEQwMDAwMEREMDAwMDAwRDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDP/AABEIACkAMgMBEQACEQEDEQH/3QAEAAf/xAGiAAAABwEBAQEBAAAAAAAAAAAEBQMCBgEABwgJCgsBAAICAwEBAQEBAAAAAAAAAAEAAgMEBQYHCAkKCxAAAgEDAwIEAgYHAwQCBgJzAQIDEQQABSESMUFRBhNhInGBFDKRoQcVsUIjwVLR4TMWYvAkcoLxJUM0U5KismNzwjVEJ5OjszYXVGR0w9LiCCaDCQoYGYSURUaktFbTVSga8uPzxNTk9GV1hZWltcXV5fVmdoaWprbG1ub2N0dXZ3eHl6e3x9fn9zhIWGh4iJiouMjY6PgpOUlZaXmJmam5ydnp+So6SlpqeoqaqrrK2ur6EQACAgECAwUFBAUGBAgDA20BAAIRAwQhEjFBBVETYSIGcYGRMqGx8BTB0eEjQhVSYnLxMyQ0Q4IWklMlomOywgdz0jXiRIMXVJMICQoYGSY2RRonZHRVN/Kjs8MoKdPj84SUpLTE1OT0ZXWFlaW1xdXl9UZWZnaGlqa2xtbm9kdXZ3eHl6e3x9fn9zhIWGh4iJiouMjY6Pg5SVlpeYmZqbnJ2en5KjpKWmp6ipqqusra6vr/2gAMAwEAAhEDEQA/AO875kNKUebPM+n+V9Audavw0kUACw2yGjzTN9iJT25ftN+wnxZGUgA2YsZnKg+WvNf5oedfMENzf6nqU0FvMStppFq5htI0P2fhUgyt/lScsIDmcEY8kl8q3fmSznS6sNRu7G7Qc4ri3lZa06jiTwbj3jccXXASL2bhGxu+nvyr893fmfSprbVGRtb08KZ5YxwWeF9kmCfstyHCT/KwguDqMIibHIs1Nck4y3fCr//Q71mQ0vDf+ci9We913y15PhdlWcm6vePXjK3BR/yLRshLv/mudpdok/zjw/5rev8A5MafqvlaNdHhZb2yBkVDVmkUDdfn/LmNCcgbcviHV5raRemhhWMoUBIUijK8fwyIf15eFO6feTPM6+XfNmm6uGP1B5Pq9+o/5Zp6JJX/AIxvwlywNOWPFGn03IvFiKg06EdD7jJOrWYq/wD/0e+qpZgoFSTQDL2l8q+ep7jzj+el5BZSRhYJGsrWSQ/D6dogTkOnxc+ZxkaiS5+KN0A9Zs/LHma+/LyGYXML6gl3JBHOTIsTwRngJFQMrOeYPwM/2cxK69G4S3rq8r83eX9R8t668FzDCjXKC6SS2DLCzAfGAjlmTmtfg5fayyJtsiKY9ePFVoQAYpwWjFdjUbp/slJy+IYSNPpL8qfNKeY/JVnK787/AE0Cwv69S0Sj0pD/AMZIuP8AwLYXXZocMvIstphaX//S79G5R1cdVII+jL2l8efmJb3HkT83Lye1rMLO6S+h57GaC6HN1+kO65Mx4o05cJkUXuX5f+YdU1zy3JaQzLd2bK0mju5ZEVSxdHk9NeXONvhdT/LmCRWxc2o1xAsH/PLW0tms4byRJtaKiKOKPalSOUhHVU/l5ZLCCSspUHktpe+rAsZJ7mOvUMm34r/xHM2mnieg/kV5sk0r8wIbCR+On68v1KePss+7W7/ISDh/qyYJBpzC4+59OcW8D1p9ORcJ/9PvgIy9peIf85Hfl5favJa+ZdLt3uZorc2t/HEKsBFVopCP5AvJWyQNN2KXR5Z5U8+eafLulGz8ragqWstrBcXa+ksrQTzAiUqX3T4lq1Ph+LJeDGRssp5pRCC86WWvvfR3l+FnvLlRMxgk+sbqhPxMtTuPjxhg4QkaoTQf5f8AlDX/ADTe/UbGNfTLMfrEh4qhAqd/py4Qtry6kYxuyTVPy6816Jp1rr13bPFZ+oDDqFtIpkjZZCEZlH2QzJyR/wDVyuQptx5YzNDm9U/x1+cn+/8ARf8Ajm+v/fx/Y/6uXX+//wCKfs/5GR4Gr0d0uf8AN/2L/9TvYy9pQWt/8cbU/wDmBuv+TD4pHN8i6T/cL/2wbL9RzKwcvgw1HIf1mTaJ/wApBon+vJ/1DPl/UOJ/DL8fxLvyG/3j1T/jHL+pchi5N+u+qL37zD/5LfUv+2W3/JgZWebDH9Uf6z53/wC8bg/U7T/in//Z"
    }
}