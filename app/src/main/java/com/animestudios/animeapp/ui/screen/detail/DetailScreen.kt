package com.animestudios.animeapp.ui.screen.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.animestudios.animeapp.databinding.DetailScreenBinding
import com.animestudios.animeapp.loadDetailTabs
import com.animestudios.animeapp.loadIcons
import com.animestudios.animeapp.ui.screen.detail.adapter.IconAdapter
import com.animestudios.animeapp.ui.screen.detail.adapter.TabAdapter
import com.google.android.material.tabs.TabLayoutMediator

class DetailScreen : Fragment() {
    private var _binding: DetailScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTab()
    }

    private fun setTab() {

        binding?.apply {

            val adapter = TabAdapter(requireActivity())
            viewPager.adapter = adapter
            viewPager.isUserInputEnabled = false
            TabLayoutMediator(tabLayout, viewPager) { _, _ ->

            }.attach()
            for (i in 0 until tabLayout.tabCount) {
                tabLayout.getTabAt(i)
                    ?.let { TooltipCompat.setTooltipText(it.view, null) }
            }
            val tabCount = tabLayout.tabCount
            for (i in 0 until tabCount) {
                val tab = tabLayout.getTabAt(i)
                tab!!.text = loadDetailTabs()[i]
            }

        }
    }

}