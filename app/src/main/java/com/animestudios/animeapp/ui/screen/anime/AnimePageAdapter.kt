package com.animestudios.animeapp.ui.screen.anime

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.AnimePageItemBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.slideStart
import com.animestudios.animeapp.tools.slideUp
import com.animestudios.animeapp.ui.screen.home.banner.BannerAdapter

class AnimePageAdapter(private val fragmentAdapter: Fragment) :
    RecyclerView.Adapter<AnimePageAdapter.AnimePageVh>() {
    private var currentPage = 0
    private var trendHandler: Handler? = null
    private lateinit var trendRun: Runnable
    private val adapter: AnimeTitleWithScoreAdapter by lazy {
        AnimeTitleWithScoreAdapter(
            fragmentAdapter as AnimeScreen
        )
    }
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()
    var height = statusBarHeight
    val ready = MutableLiveData(false)
    lateinit var binding: AnimePageItemBinding
    var trendingViewPager: ViewPager2? = null


    inner class AnimePageVh(val bin: AnimePageItemBinding) :
        RecyclerView.ViewHolder(bin.root) {
        fun onBind() {
            binding = bin
            trendingViewPager = binding.viewPager2

            binding.search.setOnClickListener {
                fragmentAdapter.findNavController().navigate(R.id.action_mainScreen_to_searchScreen)
            }

            if (ready.value == false)
                ready.postValue(true)
        }
    }


    fun updateRecently(list: List<Media>) {

        adapter.submitLit(list.toMutableList())
        binding.animePageRecyclerView.setHasFixedSize(true)
        binding.animePageRecyclerView.adapter = adapter
        binding.animePageRecyclerView.layoutAnimation =
            LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
        binding.animePageRecyclerView.layoutManager = LinearLayoutManager(
            binding.animePageRecyclerView.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recentReleased.visibility = View.VISIBLE
        binding.recentReleased.slideStart(700, 0)

    }

    fun updateTrendingBanner(adapterBannerAdapter: BannerAdapter) {
        binding.viewPager2.adapter = adapterBannerAdapter

        trendHandler = Handler(Looper.getMainLooper())
        trendRun = Runnable {
            binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
        }
        binding.viewPager2.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    trendHandler!!.removeCallbacks(trendRun)
                    trendHandler!!.postDelayed(trendRun, 4000)
                }
            }
        )

        binding.viewPager2.setPageTransformer(MediaPageTransformer())
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.layoutAnimation =
            LayoutAnimationController(setSlideIn(uiSettings), 0.50f)
        binding.animeToolbarContainer.visible()
        binding.animeToolbarContainer.slideUp(700, 0)
    }

    fun updateTrending(list: MutableList<Media>) {
        println("Tushdiiiiii ${list.size}")
        val animeTitleWithScoreAdapter =
            AnimeTitleWithScoreAdapter(fragmentAdapter)
        animeTitleWithScoreAdapter.submitLit(list)
        binding.apply {

            binding.forYouTxt.visible()
            onlOnYouRecyclerView.adapter = animeTitleWithScoreAdapter
            binding.onlOnYouRecyclerView.setHasFixedSize(true)
            binding.onlOnYouRecyclerView.layoutAnimation =
                LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
            binding.forYouTxt.slideStart(700, 0)
        }
    }

    fun updateHeight() {
        trendingViewPager!!.updateLayoutParams { height += statusBarHeight }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnimePageAdapter.AnimePageVh {

        val binding =
            AnimePageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimePageVh(binding)
    }

    override fun onBindViewHolder(holder: AnimePageVh, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int = 1

}