package com.animestudios.animeapp.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.core.content.withStyledAttributes
import com.animestudios.animeapp.R
import com.google.android.material.imageview.ShapeableImageView
import kotlin.math.roundToInt

private const val ASPECT_RATIO_HEIGHT = 18f
private const val ASPECT_RATIO_WIDTH = 13f

class FeedItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    private var orientation: Int = LinearLayout.HORIZONTAL

    init {
        context.withStyledAttributes(attrs, R.styleable.FeedItem, defStyleAttr) {
//            orientation = getInt(R.styleable.FeedItem_android_orientation, orientation)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredWidth: Int
        val desiredHeight: Int
        if (orientation == LinearLayout.VERTICAL) {
            desiredHeight = measuredHeight
            desiredWidth = (desiredHeight * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT).roundToInt()
        } else {
            desiredWidth = measuredWidth
            desiredHeight = (desiredWidth * ASPECT_RATIO_HEIGHT / ASPECT_RATIO_WIDTH).roundToInt()
        }
        setMeasuredDimension(desiredWidth, desiredHeight)
    }
}