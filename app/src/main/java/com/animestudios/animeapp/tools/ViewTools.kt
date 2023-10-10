package com.animestudios.animeapp.tools

import android.content.Context
import android.os.Vibrator
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.NavOptions
import com.animestudios.animeapp.R
import com.animestudios.animeapp.app.App

fun View.slideTop(animTime: Long, startOffset: Long) {
    val slideUp = AnimationUtils.loadAnimation(App.currentContext(), R.anim.slide_top).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideUp)
}
fun View.slideUp(animTime: Long, startOffset: Long) {
    val slideUp = AnimationUtils.loadAnimation(App.currentContext(), R.anim.slide_up).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideUp)
}

fun View.slideStart(animTime: Long, startOffset: Long) {
    val slideUp = AnimationUtils.loadAnimation(App.currentContext(), R.anim.slide_start).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideUp)
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