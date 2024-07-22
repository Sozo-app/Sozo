package com.animestudios.animeapp.ui.screen.anime

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.AnimeScreenBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.others.CustomBottomDialog
import com.animestudios.animeapp.ui.screen.home.banner.BannerAdapter
import com.animestudios.animeapp.viewmodel.imp.AniListViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class AnimeScreen : Fragment() {
    private var _binding: AnimeScreenBinding? = null
    private val binding get() = _binding!!
    var isLoaded = true
    private val model by viewModels<AniListViewModelImp>()
    var height = statusBarHeight
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AnimeScreenBinding.inflate(
            inflater,
            container,
            false
        )
        return _binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var height = statusBarHeight
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val displayCutout = activity?.window?.decorView?.rootWindowInsets?.displayCutout
            if (displayCutout != null) {
                if (displayCutout.boundingRects.size > 0) {
                    height = max(
                        statusBarHeight,
                        min(
                            displayCutout.boundingRects[0].width(),
                            displayCutout.boundingRects[0].height()
                        )
                    )
                }
            }
        }
        binding.apply {
            initRv(height)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (readData<Boolean>("allow_opening_links", binding.root.context) != true) {
                CustomBottomDialog.newInstance().apply {
                    title = "Allow Sozo to automatically open Anilist  Links?"
                    val md = "Open settings & click +Add Links & select Anilist & Mal urls"
                    addView(TextView(binding.root.context).apply {
                        val markWon =
                            Markwon.builder(binding.root.context)
                                .usePlugin(SoftBreakAddsNewLinePlugin.create()).build()
                        markWon.setMarkdown(this, md)
                    })

                    setNegativeButton("No") {
                        saveData("allow_opening_links", true, binding.root.context)
                        dismiss()
                    }

                    setPositiveButton("Yes") {
                        saveData("allow_opening_links", true, binding.root.context)
                        tryWith(true) {
                            startActivity(
                                Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS)
                                    .setData(Uri.parse("package:com.animestudios.animeapp"))
                            )
                        }
                    }
                }.show(parentFragmentManager, "dialog")
            }
        }
        val animePageAdapter = AnimePageAdapter(this@AnimeScreen)
        val adapter = ConcatAdapter(animePageAdapter)
        binding.animePageRecyclerView.adapter = adapter
        val layout = LinearLayoutManager(requireContext())
        binding.animePageRecyclerView.layoutManager = layout
        animePageAdapter.ready.observe(viewLifecycleOwner) { i ->
            binding.mainScrollView.isNestedScrollingEnabled = false
            if (i) {
                if (animePageAdapter.trendingViewPager != null) {
                    model.loadPopular.observe(viewLifecycleOwner) {
                        binding.animeRefresh.isRefreshing = false
                        binding.mainScrollView.isNestedScrollingEnabled = true
                        if (it != null) {
                            binding.animeTrendingProgressBar.gone()
                            val newList = filterList(it.results)
                            animePageAdapter.updateTrendingBanner(
                                BannerAdapter(
                                    3,
                                    newList,
                                    requireActivity(),
                                    viewPager = animePageAdapter.trendingViewPager,
                                )
                            )
                        }
                    }
                }
                model.recentlyTrendList.observe(viewLifecycleOwner) {
                    if (it != null) {
                        animePageAdapter.updateTrending(it)
                    }
                }
                model.recentlyUpdatedList.observe(viewLifecycleOwner) {
                    if (it != null) {
                        animePageAdapter.updateRecently(it)
                    }
                }
                model.getReview.observe(viewLifecycleOwner) {
                    if (it != null) {
                        animePageAdapter.updateReview(it)
                    }
                }

                animePageAdapter.setSelectedChipListener {
                    model.loadReview(it)
                }
            }
        }
    }


    @SuppressLint("NewApi")
    private fun initRv(height: Int) {
        binding.apply {
            binding.animeRefresh.setSlingshotDistance(height + 120)
            binding.animeRefresh.setProgressViewEndTarget(false, height + 120)
            binding.animeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    Refresh.activity[1]!!.postValue(true)
                }
            }

            val live = Refresh.activity.getOrPut(
                1
            ) { MutableLiveData(false) }
            live.observe(viewLifecycleOwner) {
                if (it) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        loaded = true
                        model.loadAnimeSection(1)
                        live.postValue(false)
                        _binding?.animeRefresh?.isRefreshing = false
                    }
                }
            }
        }
    }


    private fun filterList(it: List<Media>): ArrayList<Media> {
        val newList = ArrayList<Media>()

        it.onEach {
            if (it.userPreferredName.isNotEmpty() && it.banner != null && it.anime?.totalEpisodes != null) {
                newList.add(it)
            }
        }
        return newList
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        if (!loaded) Refresh.activity[1]!!.postValue(true)
        super.onResume()
    }
}