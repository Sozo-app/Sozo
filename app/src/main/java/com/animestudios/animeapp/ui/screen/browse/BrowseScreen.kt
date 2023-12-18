package com.animestudios.animeapp.ui.screen.browse

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.animestudios.animeapp.databinding.BorwseScreenBinding
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.localLoadTabTxt
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.slideStart
import com.animestudios.animeapp.ui.screen.browse.adapter.TabAdapter
import com.google.android.material.tabs.TabLayoutMediator


class BrowseScreen : Fragment() {
    private var _binding: BorwseScreenBinding? = null
    private val binding get() = _binding!!
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()
    private lateinit var adapter: TabAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = com.animestudios.animeapp.databinding.BorwseScreenBinding.inflate(
            inflater,
            container,
            false
        )
        return _binding?.root

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbarBrowse.slideStart(700, 0)
            browseType.slideStart(700, 0)
            viewPager.slideStart(700, 0)
            adapter = TabAdapter(localLoadTabTxt(), requireActivity())
            binding.viewPager.adapter = adapter

//            binding.genreContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight;bottomMargin += navBarHeight }

            TabLayoutMediator(browseType, viewPager) { _, _ ->
            }.attach()
            setTab()

            for (i in 0 until binding.browseType.tabCount) {
                binding.browseType.getTabAt(i)?.let { TooltipCompat.setTooltipText(it.view, null) }
            }
//            binding.genreContainer.updatePaddingRelative(bottom = navBarHeight + 36f.px)


        }
    }


    private fun setTab() {
        binding.apply {
            val tabCount = browseType.tabCount
            for (i in 0 until tabCount) {
                val tab = browseType.getTabAt(i)
                tab!!.text = localLoadTabTxt()[i]
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.toolbarBrowse.slideStart(700, 0)
        binding.browseType.slideStart(700, 0)
        binding.viewPager.slideStart(700, 0)

    }
}