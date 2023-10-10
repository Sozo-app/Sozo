package com.animestudios.animeapp.ui.screen.browse.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.animestudios.animeapp.ui.screen.browse.page.allanime.AllAnimeFragment
import com.animestudios.animeapp.ui.screen.browse.page.genre.GenreFragment

class TabAdapter(var arrayList: ArrayList<String>, fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllAnimeFragment()
            else -> GenreFragment()
        }
    }
}