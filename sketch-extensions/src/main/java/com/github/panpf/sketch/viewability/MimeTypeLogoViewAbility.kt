package com.github.panpf.sketch.viewability

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.github.panpf.sketch.drawable.SketchDrawable
import com.github.panpf.sketch.util.getLastDrawable
import com.github.panpf.sketch.viewability.ViewAbility.AttachObserver
import com.github.panpf.sketch.viewability.ViewAbility.DrawObserver
import com.github.panpf.sketch.viewability.ViewAbility.DrawableObserver
import com.github.panpf.sketch.viewability.ViewAbility.LayoutObserver

class MimeTypeLogoViewAbility(
    private val mimeTypeIconMap: Map<String, MimeTypeLogo>,
    private val margin: Int = 0
) : ViewAbility, AttachObserver, DrawObserver, LayoutObserver, DrawableObserver {

    override var host: Host? = null
    private var logoDrawable: Drawable? = null

    override fun onAttachedToWindow() {
        reset()
        host?.invalidate()
    }

    override fun onDetachedFromWindow() {

    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        reset()
        host?.invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        reset()
        host?.invalidate()
    }

    override fun onDrawBefore(canvas: Canvas) {

    }

    override fun onDraw(canvas: Canvas) {
        logoDrawable?.draw(canvas)
    }

    override fun onDrawForegroundBefore(canvas: Canvas) {

    }

    override fun onDrawForeground(canvas: Canvas) {

    }

    private fun reset() {
        logoDrawable = null
        val host = host ?: return
        val view = host.view
        val lastDrawable = host.drawable?.getLastDrawable() ?: return
        if (lastDrawable !is SketchDrawable) return
        val mimeType = lastDrawable.imageMimeType ?: return
        val mimeTypeLogo = mimeTypeIconMap[mimeType] ?: return
        if (mimeTypeLogo.hiddenWhenAnimatable && lastDrawable is Animatable) return
        val logoDrawable = mimeTypeLogo.getDrawable(host.context)
        logoDrawable.setBounds(
            view.right - view.paddingRight - margin - logoDrawable.intrinsicWidth,
            view.bottom - view.paddingBottom - margin - logoDrawable.intrinsicHeight,
            view.right - view.paddingRight - margin,
            view.bottom - view.paddingBottom - margin
        )
        this.logoDrawable = logoDrawable
    }
}


fun ViewAbilityOwner.setMimeTypeLogo(mimeTypeLogoViewAbility: MimeTypeLogoViewAbility?) {
    viewAbilityList
        .find { it is MimeTypeLogoViewAbility }
        ?.let { removeViewAbility(it) }
    if (mimeTypeLogoViewAbility != null) {
        addViewAbility(mimeTypeLogoViewAbility)
    }
}

fun ViewAbilityOwner.setMimeTypeLogoWith(
    mimeTypeIconMap: Map<String, MimeTypeLogo>?,
    margin: Int = 0
) {
    val mimeTypeLogoViewAbility = if (mimeTypeIconMap?.isNotEmpty() == true) {
        MimeTypeLogoViewAbility(mimeTypeIconMap, margin)
    } else {
        null
    }
    setMimeTypeLogo(mimeTypeLogoViewAbility)
}

fun ViewAbilityOwner.setMimeTypeLogoWithDrawable(
    mimeTypeIconMap: Map<String, Drawable>?,
    margin: Int = 0
) {
    val mimeTypeLogoViewAbility = if (mimeTypeIconMap?.isNotEmpty() == true) {
        val newMap = mimeTypeIconMap.mapValues {
            MimeTypeLogo(it.value)
        }
        MimeTypeLogoViewAbility(newMap, margin)
    } else {
        null
    }
    setMimeTypeLogo(mimeTypeLogoViewAbility)
}

fun ViewAbilityOwner.setMimeTypeLogoWithResId(
    mimeTypeIconMap: Map<String, Int>?,
    margin: Int = 0
) {
    val mimeTypeLogoViewAbility = if (mimeTypeIconMap?.isNotEmpty() == true) {
        val newMap = mimeTypeIconMap.mapValues {
            MimeTypeLogo(it.value)
        }
        MimeTypeLogoViewAbility(newMap, margin)
    } else {
        null
    }
    setMimeTypeLogo(mimeTypeLogoViewAbility)
}

class MimeTypeLogo {

    private val data: Any
    private var _drawable: Drawable? = null

    val hiddenWhenAnimatable: Boolean

    constructor(drawable: Drawable, hiddenWhenAnimatable: Boolean = false) {
        this.data = drawable
        this.hiddenWhenAnimatable = hiddenWhenAnimatable
    }

    constructor(drawableResId: Int, hiddenWhenAnimatable: Boolean = false) {
        this.data = drawableResId
        this.hiddenWhenAnimatable = hiddenWhenAnimatable
    }

    fun getDrawable(context: Context): Drawable {
        return _drawable ?: if (data is Drawable) {
            _drawable = data
            data
        } else {
            val drawableResId = data as Int
            val newDrawable = ResourcesCompat.getDrawable(context.resources, drawableResId, null)!!
            _drawable = newDrawable
            newDrawable
        }
    }
}