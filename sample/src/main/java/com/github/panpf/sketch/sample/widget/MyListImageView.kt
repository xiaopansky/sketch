package com.github.panpf.sketch.sample.widget

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Observer
import com.github.panpf.sketch.SketchImageView
import com.github.panpf.sketch.internal.MimeTypeLogo
import com.github.panpf.sketch.sample.R
import com.github.panpf.sketch.sample.appSettingsService

class MyListImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SketchImageView(context, attrs) {

    private val progressIndicatorObserver = Observer<Boolean> {
        showMaskProgressIndicator(it == true)
    }
    private val mimeTypeLogoObserver = Observer<Boolean> {
        setMimeTypeLogo(
            if (it == true)
                mapOf("image/gif" to MimeTypeLogo(R.drawable.ic_gif, true))
            else null
        )
    }
    private val dataFromObserver = Observer<Boolean> {
        showDataFrom(it == true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.appSettingsService.showProgressIndicatorInList.observeForever(
            progressIndicatorObserver
        )
        context.appSettingsService.showMimeTypeLogoInLIst.observeForever(mimeTypeLogoObserver)
        context.appSettingsService.showDataFrom.observeForever(dataFromObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.appSettingsService.showProgressIndicatorInList.removeObserver(
            progressIndicatorObserver
        )
        context.appSettingsService.showMimeTypeLogoInLIst.removeObserver(mimeTypeLogoObserver)
        context.appSettingsService.showDataFrom.removeObserver(dataFromObserver)
    }
}