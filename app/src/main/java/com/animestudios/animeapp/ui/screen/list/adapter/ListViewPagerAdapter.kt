package com.animestudios.animeapp.ui.screen.list.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.animestudios.animeapp.ui.screen.list.page.ListFragment
import com.animestudios.animeapp.viewmodel.imp.ListViewModelImp

class ListViewPagerAdapter(
    private val model: ListViewModelImp,
    private val size: Int,
    private val calendar: Boolean,
    fragment: FragmentActivity
) :
    FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int = size
    override fun createFragment(position: Int): Fragment {
        var fragment =  ListFragment.newInstance(
            position,
            calendar,
            model
        )


        return fragment
    }

}