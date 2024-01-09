package com.animestudios.animeapp.tools

import android.content.Context
import android.util.TypedValue
import android.view.View
import coil.request.Disposable
import java.util.concurrent.TimeUnit

fun Context.getAttrValue(attrResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return typedValue.data
}

fun Int?.or1() = this ?: 1
