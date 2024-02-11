package com.animestudios.animeapp.ui.screen.notification

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.NotificationScreenBinding
import com.animestudios.animeapp.databinding.TabItemBinding
import com.animestudios.animeapp.ui.screen.browse.adapter.TabAdapter
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class NotificationScreen : Fragment() {
    private var _binding: NotificationScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = NotificationScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notificationPager.adapter= TabAdapter(tabList(),requireActivity())
        TabLayoutMediator(binding.notificationTab, binding.notificationPager) { _, _ ->
        }.attach()
        binding.notificationTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val customView = tab?.customView
                val customTab = TabItemBinding.bind(customView!!)
                customTab?.polisContainer?.setBackgroundResource(R.drawable.tab_selected)
                customTab?.titleCat?.setTextColor(requireActivity().getColor(R.color.bg_black))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val customView = tab?.customView
                val customTab = TabItemBinding.inflate(LayoutInflater.from(customView!!.context))
                customTab?.polisContainer?.setBackgroundResource(R.drawable.tab_item_unselected)
                val color = MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorSurfaceInverse,
                    Color.BLACK
                )
                customTab?.titleCat?.setTextColor(color)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

    }


    private fun setTab() {
        binding.apply {
            val tabCount = notificationTab.tabCount
            for (i in 0 until tabCount) {


                val tabView = TabItemBinding.inflate(LayoutInflater.from(requireActivity()))
                val tab = notificationTab.getTabAt(i)


                tab?.customView = tabView.root
                tabView.titleCat.text = tabList()[i]
                if (i == 0) {
                    val color = MaterialColors.getColor(
                        requireContext(),
                        com.google.android.material.R.attr.colorSurfaceInverse,
                        Color.BLACK
                    )

                    tabView?.polisContainer?.setBackgroundResource(R.drawable.tab_selected)

                    tabView?.titleCat?.setTextColor(color)
                } else {
                    tabView?.polisContainer?.setBackgroundResource(R.drawable.tab_item_unselected)
                    tabView?.titleCat?.setTextColor(requireActivity().getColor(R.color.bg_black))
                }
            }

        }
    }

    fun tabList(): ArrayList<String> {
        return arrayListOf(
            "All",
            "Unread",
            "Airing",
            "Completed",
            "Forum",
            "On Hold",
            "Media",
            ""
        )
    }

}