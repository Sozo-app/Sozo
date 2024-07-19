package com.animestudios.animeapp

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.CircleCropTransformation
import com.animestudios.animeapp.others.GlideApp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.noties.markwon.Markwon

/**
 * A binding adapter that loads an image from a url into an ImageView.
 *
 * @param image ImageView - The ImageView that we want to bind the image to.
 * @param url The URL of the image to load.
 */
fun ImageView.provideImageBinding(url: String?) {
    if (!url.isNullOrEmpty()) {
        GlideApp.with(context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(this)
    }
}

fun ImageView.setAvatarImage(url: String?) {
    if (!url.isNullOrEmpty()) {
        load(url) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }
}

/**
 * If the value is true, show the view, otherwise hide it.
 *
 * @param view View - The view you want to change the visibility of
 * @param value The value that will be passed to the binding adapter.
 */
@BindingAdapter("android:visibility")
fun View.setVisibility(value: Boolean) {
    isVisible = value
}

/**
 * "If the isFiller boolean is true, set the background color of the CardView to #2B2C30, otherwise
 * set it to #17293F."
 *
 * The @BindingAdapter annotation is what tells the compiler that this function is a binding
 * adapter. The first parameter is the name of the attribute that we'll be using in our XML layout.
 * The second parameter is the type of the attribute. In this case, it's a boolean
 *
 * @param view CardView - The view that we're binding to.
 * @param isFiller Boolean - This is the parameter that we will pass to the binding adapter.
 */
@BindingAdapter("isFiller")
fun CardView.changeFillerBg(isFiller: Boolean) {
    setCardBackgroundColor(Color.parseColor(if (isFiller) "#2B2C30" else "#17293F"))
}

@BindingAdapter("htmlText")
fun TextView.setHtml(htmlString: String) {
    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(htmlString)
    }
}

@BindingAdapter("animatedProgress")
fun ContentLoadingProgressBar.setAnimatedProgress(progress: Int) {
    ObjectAnimator.ofInt(this, "progress", progress)
        .setDuration(400)
        .start()
}

@BindingAdapter("markdown")
fun TextView.setMarkdownText(string: String) {
    text = Markwon.create(this.context)
        .toMarkdown(string)
}




@SuppressLint("ClickableViewAccessibility")
class SpinnerNoSwipe : androidx.appcompat.widget.AppCompatSpinner {
    private var mGestureDetector: GestureDetector? = null

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup()
    }

    private fun setup() {
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return performClick()
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mGestureDetector!!.onTouchEvent(event)
        return true
    }
}