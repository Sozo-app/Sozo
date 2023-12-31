package com.animestudios.animeapp.ui.screen.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.ui.screen.anime.AnimeScreen
import com.animestudios.animeapp.ui.screen.browse.BrowseScreen
import com.animestudios.animeapp.ui.screen.list.ListScreen
import com.animestudios.animeapp.ui.screen.profile.ProfileScreen

class BottomNavigationAdapter(fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AnimeScreen()
            1 -> BrowseScreen()
            2 -> if (Anilist.token != null) ListScreen() else ListScreen()
            3 -> ProfileScreen()
            4 -> ProfileScreen()
            else -> {
                ListScreen()
            }
        }
    }
}