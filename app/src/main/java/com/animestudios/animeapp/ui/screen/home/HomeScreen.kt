package com.animestudios.animeapp.ui.screen.home

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.animestudios.animeapp.*
import com.animestudios.animeapp.base.BaseSwipeableFragment
import com.animestudios.animeapp.databinding.HomeScreenBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.ui.screen.home.category.CategoryAdapter
import com.animestudios.animeapp.ui.screen.home.youtube.HomeAnimeAdapter
import com.animestudios.animeapp.viewmodel.imp.AniListViewModelImp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class HomeScreen : BaseSwipeableFragment<HomeScreenBinding>(HomeScreenBinding::inflate) {
    private val model by viewModels<AniListViewModelImp>()
    private val categoryAdapter by lazy { CategoryAdapter(requireActivity()) }
    private val homeAdapter by lazy { HomeAnimeAdapter(requireActivity()) }

    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()
    var height = statusBarHeight


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initCategoryRv()
        initHomeAnimeRv()
    }

    private fun initView() {
        if (uiSettings.smallView) binding.parentHome.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = (-108f).px

        }
        binding.container.visible()

        binding.search.setOnClickListener {
            findNavController().navigate(R.id.searchScreen)
        }
        binding.homeRefresh.setSlingshotDistance(height + 200)
        binding.homeRefresh.setProgressViewEndTarget(false, height + 200)
        binding.homeRefresh.setOnRefreshListener {
            Refresh.activity[this.hashCode()]!!.postValue(true)
        }
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
        val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(false) }
        live.observe(viewLifecycleOwner) {
            binding.homeRefresh.isRefreshing = false
            lifecycleScope.launchWhenResumed {
                model.loadHome()
            }

        }


    }

    private fun initCategoryRv() {
        binding.apply {
            lifecycleScope.launch {
                model.genreCollection.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Resource.Error -> {
                            snackString(
                                response.throwable.message,
                                requireActivity(),
                                response.throwable.message
                            )

                        }
                        Resource.Loading -> {
                            frameLayout.invisible()
                            recyclerViewGenres.invisible()

                        }
                        is Resource.Success -> {
                            binding.recyclerViewGenres.layoutManager = LinearLayoutManager(
                                recyclerViewGenres.context,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            recyclerViewGenres.visible()
                            frameLayout.visible()
                            recyclerViewGenres.startAnimation(setSlideUp(uiSettings))
                            recyclerViewGenres.layoutAnimation =
                                LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
                            frameLayout.startAnimation(setSlideUp(uiSettings))
                            recyclerViewGenres.adapter = categoryAdapter
                            categoryAdapter.submitList(response.data.data.genreCollection!!)
                            categoryAdapter.setItemClickListener {
                                model.getHomeAnimeListByGenre(mutableListOf(it))
                            }

                        }
                    }
                }
            }
        }

    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun initHomeAnimeRv() {
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
            lifecycleScope.launch {
                delay(700)
                model.getHomeAnimeList.observe(this@HomeScreen) { response ->
                    when (response) {
                        is Resource.Error -> TODO()
                        Resource.Loading -> {
                            animeHomeRv.invisible()
                        }
                        is Resource.Success -> {
                            val result = response.data
                            val newList = ArrayList<Media>()
                            result.onEach {
                                if (it.userPreferredName.isNotEmpty() && it.banner != null && it.anime?.totalEpisodes != null && it.cover != null) {
                                    newList.add(it)
                                }
                            }
                            animeHomeRv.visible()
                            animeHomeRv.layoutManager = LinearLayoutManager(currContext())
                            homeAdapter.submitList(newList)
                            animeHomeRv.adapter = homeAdapter
                            homeAdapter.setItemClickListener {
                                findNavController().navigate(R.id.detailScreen)
                            }
                            animeHomeRv.startAnimation(setSlideIn(uiSettings))
                            animeHomeRv.layoutAnimation =
                                LayoutAnimationController(setSlideUp(uiSettings), 0.25f)
                            binding.animeHomeRv.updatePaddingRelative(bottom = navBarHeight + 180f.px)
                        }
                    }

                }
            }
        }
    }

}