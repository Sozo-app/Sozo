package com.animestudios.animeapp.tools

import android.annotation.SuppressLint
import android.text.InputFilter
import android.text.Spanned
import android.widget.AutoCompleteTextView
import com.animestudios.animeapp.logger

class InputFilterMinMax(private val min: Double, private val max: Double, private val status: AutoCompleteTextView? = null) :
    InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toDouble()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
            logger(nfe.stackTraceToString())
        }
        return ""
    }

    @SuppressLint("SetTextI18n")
    private fun isInRange(a: Double, b: Double, c: Double): Boolean {
        if (c == b) {
            status?.setText("COMPLETED", false)
            status?.parent?.requestLayout()
        }
        return if (b > a) c in a..b else c in b..a
    }
}
