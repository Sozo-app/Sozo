package com.animestudios.animeapp.ui.screen.anime

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.AnimeScreenBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.ui.screen.home.banner.BannerAdapter
import com.animestudios.animeapp.viewmodel.imp.AniListViewModelImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


class AnimeScreen : Fragment() {
    private var _binding: AnimeScreenBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<AniListViewModelImp>()
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
                mainScrollView.isNestedScrollingEnabled = false
                val adapter = ConcatAdapter(animePageAdapter)
                binding.animePageRecyclerView.adapter = adapter
                val layout = LinearLayoutManager(requireContext())
                binding.animePageRecyclerView.layoutManager = layout
                animePageAdapter.ready.observe(viewLifecycleOwner) { i ->
                    if (i) {
                        if (animePageAdapter.trendingViewPager != null) {
                            model.loadPopular.observe(viewLifecycleOwner) {
                                mainScrollView.isNestedScrollingEnabled = true
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
                    }
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
                Refresh.activity[this.hashCode()]!!.postValue(true)
            }
            val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(false) }
            live.observe(viewLifecycleOwner) {

                loadedBrowse = true
                model.loadAnimeSection(1)
                binding.animeRefresh.isRefreshing = false
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
        super.onResume()
        if (!loadedBrowse) Refresh.activity[1]!!.postValue(true)
    }
}