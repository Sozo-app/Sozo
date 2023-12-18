package com.animestudios.animeapp.ui.screen.detail.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.animestudios.animeapp.model.AniListMedia
import com.animestudios.animeapp.ui.screen.detail.pages.cast.CastScreen
import com.animestudios.animeapp.ui.screen.detail.pages.episodes.EpisodeScreen
import com.animestudios.animeapp.ui.screen.profile.ProfileScreen

class TabAdapter( fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                EpisodeScreen()
            }
            1-> {
                CastScreen()
            }
            2->{
                CastScreen()

            }
            else ->{
                CastScreen()

            }
        }
    }
}