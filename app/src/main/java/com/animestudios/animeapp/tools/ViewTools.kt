package com.animestudios.animeapp.tools

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.NavOptions
import com.animestudios.animeapp.R
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.settings.UISettings

fun View.slideTop(animTime: Long, startOffset: Long) {
    val slideUp = AnimationUtils.loadAnimation(App.instance, R.anim.slide_top).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideUp)
}

fun View.slideUp(animTime: Long, startOffset: Long) {
    val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()
    if (uiSettings.layoutAnimations) {
        val slideUp = AnimationUtils.loadAnimation(App.instance, R.anim.slide_up).apply {
            duration = animTime
            interpolator = FastOutSlowInInterpolator()
            this.startOffset = startOffset
        }
        startAnimation(slideUp)
    }
}

fun highlightCharacter(text: String, numberToFind: Int): Spannable {
    val spannableText = SpannableString(text)

    // Regex orqali matndan raqamni izlash
    val regex = "(?<=\\d)[a-zA-Z](?=\\d)".toRegex()
    val matches = regex.findAll(text)

    for (match in matches) {
        val matchText = match.value
        val startIndex = match.range.first + 1 // Raqamdan keyin kelgan harfning indeksi
        val endIndex = match.range.first + 2 // Raqam bilan harfning oldindagi indeksi

        // Raqamni izlayotganimizdan emas, izlanayotgan harfni qizil rangda qo'yamiz
        val colorRed = ForegroundColorSpan(Color.RED)
        spannableText.setSpan(colorRed, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    return spannableText
}

fun View.slideStart(animTime: Long, startOffset: Long) {
    val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()
    if (uiSettings.layoutAnimations) {
        val slideUp = AnimationUtils.loadAnimation(App.instance, R.anim.slide_start).apply {
            duration = animTime
            interpolator = FastOutSlowInInterpolator()
            this.startOffset = startOffset
        }
        startAnimation(slideUp)
    }

}

fun Fragment.animationTransactionClearStack(clearFragmentID: Int): NavOptions.Builder {
    val navBuilder = NavOptions.Builder()
    navBuilder.setEnterAnim(R.anim.from_right).setExitAnim(R.anim.to_left)
        .setPopEnterAnim(R.anim.from_left).setPopExitAnim(R.anim.to_right)
        .setPopUpTo(clearFragmentID, true)
    return navBuilder
}

fun Fragment.animationTransaction(): NavOptions.Builder {
    val navBuilder = NavOptions.Builder()
    navBuilder.setEnterAnim(R.anim.from_right).setExitAnim(R.anim.to_left)
        .setPopEnterAnim(R.anim.from_left).setPopExitAnim(R.anim.to_right)
    return navBuilder
}

//
//fun Fragment.vibrate(time: Long) {
//    val vibrate = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//
//    vibrate.vibrate(time)
//}
//
//fun View.vibrationAnimation() {
//    val vibrationAnim =
//        AnimationUtils.loadAnimation(App.instance, R.anim.vibiration_anim)
//    startAnimation(vibrationAnim)
//
//}