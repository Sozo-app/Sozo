package com.animestudios.animeapp.tools

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ViewPagerExtensions {
    fun ViewPager2.autoScroll(lifecycleOwner: LifecycleOwner, interval: Long) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                scrollIndefinitely(interval)
            }
        }
    }

    private suspend fun ViewPager2.scrollIndefinitely(interval: Long) {
        delay(interval)
        val numberOfItems = adapter?.itemCount ?: 0
        val lastIndex = if (numberOfItems > 0) numberOfItems - 1 else 0
        val nextItem = if (currentItem == lastIndex) 0 else currentItem + 1

        setCurrentItem(nextItem, true)

        scrollIndefinitely(interval)
    }
}