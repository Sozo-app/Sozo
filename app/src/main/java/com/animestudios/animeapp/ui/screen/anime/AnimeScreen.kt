package com.animestudios.animeapp.ui.screen.anime

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.animestudios.animeapp.Refresh
import com.animestudios.animeapp.databinding.AnimeScreenBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.statusBarHeight
import com.animestudios.animeapp.ui.screen.home.banner.BannerAdapter
import com.animestudios.animeapp.viewmodel.imp.AniListViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class AnimeScreen : Fragment() {
    private var _binding: AnimeScreenBinding? = null
    private val binding get() = _binding!!
    private val model by activityViewModels<AniListViewModelImp>()
    private val animePageAdapter = AnimePageAdapter(this@AnimeScreen)
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
        animePageAdapter.ready.observe(this) { i ->
            binding.mainScrollView.isNestedScrollingEnabled = false
            val adapter = ConcatAdapter(animePageAdapter)
            binding.animePageRecyclerView.adapter = adapter
            val layout = LinearLayoutManager(requireContext())
            binding.animePageRecyclerView.layoutManager = layout
            if (i) {
                if (animePageAdapter.trendingViewPager != null) {
                    model.loadPopular.observe(this) {
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
                model.recentlyTrendList.observe(this) {
                    if (it != null) {
                        animePageAdapter.updateTrending(it)
                    }
                }
                model.recentlyUpdatedList.observe(this) {
                    if (it != null) {
                        animePageAdapter.updateRecently(it)
                    }
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.isMotionEventSplittingEnabled = false

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
            initView()
        }
    }


    @SuppressLint("NewApi")
    private fun initView() {
        binding.apply {
            lifecycleScope.launch(Dispatchers.Main)
            {

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
                        withContext(Dispatchers.IO) {
                            model.loaded = true
                            model.loadAnimeSection(1)
                        }
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
        if (!model.loaded) Refresh.activity[1]!!.postValue(true)

        super.onResume()
    }
}