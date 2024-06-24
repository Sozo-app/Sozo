package com.animestudios.animeapp.ui.screen.detail.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.animestudios.animeapp.ui.screen.detail.pages.cast.CastScreen
import com.animestudios.animeapp.ui.screen.detail.pages.detail.DetailPage
import com.animestudios.animeapp.ui.screen.detail.pages.episodes.EpisodeScreen
import com.animestudios.animeapp.ui.screen.detail.pages.relation.RelationScreen
import com.animestudios.animeapp.ui.screen.detail.pages.statistics.StatisticsScreen

class TabAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val screen = EpisodeScreen()


                screen
            }
            1 -> {
                val screen = CastScreen()
                screen
            }
            2 -> {
                RelationScreen()

            }
            3->{
                DetailPage()
            }
            else -> {
                StatisticsScreen()

            }
        }
    }
}