package com.animestudios.animeapp.ui.screen.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.animestudios.animeapp.R
import com.animestudios.animeapp.applyColorByAttr
import com.animestudios.animeapp.databinding.NotificationScreenBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.ui.screen.notification.adapter.NotificationTabAdapter
import com.animestudios.animeapp.visible
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
        binding.notificationPager.adapter = NotificationTabAdapter(tabList(), requireActivity())
        TabLayoutMediator(binding.notificationTab, binding.notificationPager) { _, _ ->
        }.attach()

        binding.materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.notificationTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                val customView = tab?.customView
                val img = customView?.findViewById<ImageView>(R.id.icTab)
                customView?.findViewById<LinearLayout>(R.id.polisContainer)
                    ?.setBackgroundResource(R.drawable.tab_selected)
                customView?.findViewById<TextView>(R.id.titleCat)
                    ?.setTextColor(applyColorByAttr(com.google.android.material.R.attr.colorControlNormal))
                img!!.setColorFilter(applyColorByAttr(com.google.android.material.R.attr.colorControlNormal))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val customView = tab?.customView
                val img = customView?.findViewById<ImageView>(R.id.icTab)
                customView?.findViewById<LinearLayout>(R.id.polisContainer)
                    ?.setBackgroundResource(R.drawable.tab_item_unselected)
                customView?.findViewById<TextView>(R.id.titleCat)
                    ?.setTextColor(applyColorByAttr(com.google.android.material.R.attr.colorOnSurfaceVariant))
                img!!.setColorFilter(applyColorByAttr(com.google.android.material.R.attr.colorOnSurfaceVariant))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        setTab()
    }


    private fun setTab() {
        binding.apply {
            val tabCount = notificationTab.tabCount
            for (i in 0 until tabCount) {

                val tabView = LayoutInflater.from(requireActivity())
                    .inflate(R.layout.tab_item, null, false)
                val tab = notificationTab.getTabAt(i)

                tab?.customView = tabView
                tabView.findViewById<TextView>(R.id.titleCat).text = tabList()[i]
                val img = tabView.findViewById<ImageView>(R.id.icTab)

                if (tabIconList()[i].second) {
                    img.setImageResource(tabIconList()[i].first)
                    img.visible()
                } else {
                    img.gone()
                }



                if (i == 0) {

                    img.setColorFilter(applyColorByAttr(com.google.android.material.R.attr.colorControlNormal))

                    tabView?.findViewById<LinearLayout>(R.id.polisContainer)
                        ?.setBackgroundResource(R.drawable.tab_selected)
                    tabView?.findViewById<TextView>(R.id.titleCat)
                        ?.setTextColor(applyColorByAttr(com.google.android.material.R.attr.colorControlNormal))

                } else {

                    tabView?.findViewById<LinearLayout>(R.id.polisContainer)
                        ?.setBackgroundResource(R.drawable.tab_item_unselected)
                    tabView?.findViewById<TextView>(R.id.titleCat)
                        ?.setTextColor(applyColorByAttr(com.google.android.material.R.attr.colorOnSurfaceVariant))
                    img.setColorFilter(applyColorByAttr(com.google.android.material.R.attr.colorOnSurfaceVariant))
                }
            }

        }
    }

    fun tabList(): ArrayList<String> {
        return arrayListOf(
            "All",
            "Airing",
            "Forum",
            "Follows",
            "Media",
        )
    }

    fun tabIconList(): ArrayList<Pair<Int, Boolean>> {
        return arrayListOf(
            Pair(0, false),
            Pair(
                R.drawable.ic_unread,
                true
            ),
            Pair(R.drawable.ic_airing, true),
            Pair(R.drawable.ic_forum, true),
            Pair(R.drawable.ic_follow, true),
            Pair(R.drawable.ic_pic, true),

            )
    }

}