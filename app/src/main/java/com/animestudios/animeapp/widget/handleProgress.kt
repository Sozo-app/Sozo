package com.animestudios.animeapp.widget

import android.view.View
import android.widget.LinearLayout
import com.animestudios.animeapp.readData

fun handleProgress(cont: LinearLayout, bar: View, empty: View, mediaId: Int, ep: String) {
    val curr = readData<Long>("${mediaId}_${ep}")
    val max = readData<Long>("${mediaId}_${ep}_max")
    if (curr != null && max != null) {
        cont.visibility = View.VISIBLE
        val div = curr.toFloat() / max.toFloat()
        val barParams = bar.layoutParams as LinearLayout.LayoutParams
        barParams.weight = div
        bar.layoutParams = barParams
        val params = empty.layoutParams as LinearLayout.LayoutParams
        params.weight = 1 - div
        empty.layoutParams = params
    } else {
        cont.visibility = View.GONE
    }
}