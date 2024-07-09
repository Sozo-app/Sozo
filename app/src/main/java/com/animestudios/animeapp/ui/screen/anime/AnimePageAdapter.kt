package com.animestudios.animeapp.ui.screen.anime

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.AnimePageItemBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.*
import com.animestudios.animeapp.type.ReviewSort
import com.animestudios.animeapp.ui.activity.DetailActivity
import com.animestudios.animeapp.ui.screen.home.banner.BannerAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class AnimePageAdapter(private val fragmentAdapter: Fragment) :
    RecyclerView.Adapter<AnimePageAdapter.AnimePageVh>() {
    private var currentPage = 0
    private var trendHandler: Handler? = null
    private lateinit var trendRun: Runnable
    private val adapter: AnimeTitleWithScoreAdapter by lazy {
        AnimeTitleWithScoreAdapter(
            fragmentAdapter.requireActivity()
        )
    }
    private lateinit var selectedChipListener: (ReviewSort) -> Unit

    fun setSelectedChipListener(listener: (ReviewSort) -> Unit) {
        selectedChipListener = listener
    }

    var height = statusBarHeight
    val ready = MutableLiveData(false)
    lateinit var binding: AnimePageItemBinding
    var trendingViewPager: ViewPager2? = null


    inner class AnimePageVh(val bin: AnimePageItemBinding) :
        RecyclerView.ViewHolder(bin.root) {
        @SuppressLint("ResourceAsColor")
        fun onBind() {
            binding = bin
            trendingViewPager = binding.viewPager2

            binding.search.setOnClickListener {
                fragmentAdapter.findNavController()
                    .navigate(com.animestudios.animeapp.R.id.action_mainScreen_to_searchScreen)
            }
            binding.notification.setOnClickListener {
                fragmentAdapter.findNavController().navigate(
                    R.id.action_mainScreen_to_notificationScreen,
                    null,
                    fragmentAdapter.animationTransaction().build()
                )
            }

            if (ready.value == false)
                ready.postValue(true)
        }
    }


    fun updateRecently(list: List<Media>) {
        val uiSettings =
            readData<UISettings>("ui_settings") ?: UISettings()


        adapter.submitLit(list.toMutableList())
        adapter.setItemClickListener {

            val intent = Intent(
                fragmentAdapter.requireActivity(), DetailActivity::class.java
            )
            intent.putExtra("media", it)
            fragmentAdapter.requireActivity().startActivity(intent)
        }
        binding.animePageRecyclerView.setHasFixedSize(true)
        binding.animePageRecyclerView.adapter = adapter
        binding.recentReleased.visibility = View.VISIBLE

        if (uiSettings!!.layoutAnimations) {
            binding.animePageRecyclerView.layoutAnimation =
                LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
            binding.recentReleased.slideStart(700, 0)
        }


    }

    fun updateTrendingBanner(adapterBannerAdapter: BannerAdapter) {
        val uiSettings =
            readData<UISettings>("ui_settings") ?: UISettings()
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
        adapterBannerAdapter.setViewInfoListener {
            val intent = Intent(
                fragmentAdapter.requireActivity(), DetailActivity::class.java
            )
            intent.putExtra("media", it)
            intent.putExtra("currentPage", "info")
            fragmentAdapter.requireActivity().startActivity(intent)
        }
        adapterBannerAdapter.setItemClickListener {
            val intent = Intent(
                fragmentAdapter.requireActivity(), DetailActivity::class.java
            )
            intent.putExtra("media", it)
            fragmentAdapter.requireActivity().startActivity(intent)
        }
        adapterBannerAdapter.setPlayItemListener {
            val intent = Intent(
                fragmentAdapter.requireActivity(), DetailActivity::class.java
            )
            intent.putExtra("media", it)
            intent.putExtra("currentPage", "first")
            fragmentAdapter.requireActivity().startActivity(intent)
        }

        binding.viewPager2.setPageTransformer(MediaPageTransformer())
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.viewPager2.offscreenPageLimit = 3
        binding.animeToolbarContainer.visible()
        if (uiSettings.layoutAnimations) {
            binding.viewPager2.layoutAnimation =
                LayoutAnimationController(setSlideIn(uiSettings), 0.50f)
            binding.animeToolbarContainer.slideUp(700, 0)
        }

    }

    @SuppressLint("NewApi")
    fun updateTrending(list: MutableList<Media>) {
        val uiSettings =
            readData<UISettings>("ui_settings") ?: UISettings()
        println("Tushdiiiiii ${list.size}")
        val animeTitleWithScoreAdapter =
            AnimeTitleWithScoreAdapter(fragmentAdapter.requireActivity())
        animeTitleWithScoreAdapter.submitLit(list)
        animeTitleWithScoreAdapter.setItemClickListener {

            val intent = Intent(fragmentAdapter.requireActivity(), DetailActivity::class.java)
            intent.putExtra("media", it)
            fragmentAdapter.requireActivity().startActivity(intent)
        }
        binding.apply {

            binding.forYouTxt.visible()
            onlOnYouRecyclerView.adapter = animeTitleWithScoreAdapter
            binding.onlOnYouRecyclerView.setHasFixedSize(true)
            if (uiSettings!!.layoutAnimations) {
                binding.onlOnYouRecyclerView.layoutAnimation =
                    LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
                binding.forYouTxt.slideStart(700, 0)
            }
        }
    }

    fun updateReview(it: Resource<List<Review>>) {

        when (it) {
            is Resource.Loading -> {
                binding.reviewRecyclerview.invisible()
                binding.reviewProgressBar.visible()
                println("Loading")
            }
            is Resource.Success -> {
                val chipData = listOf(
                    EnumItem("Top", ReviewSort.SCORE),
                    EnumItem("Created", ReviewSort.CREATED_AT),
                    EnumItem("Updated", ReviewSort.UPDATED_AT),
                    EnumItem("Rating", ReviewSort.RATING_DESC),
                )
                binding.reviewRecyclerview.visible()
                binding.reviewProgressBar.gone()
                val uiSettings =
                    readData<UISettings>("ui_settings") ?: UISettings()
                val reviewAdapter =
                    ReviewAdapter(it.data!!, activity = fragmentAdapter)
                binding.reviewTxt.visible()
                binding.reviewRecyclerview.adapter = reviewAdapter
                if (uiSettings!!.layoutAnimations) {
                    binding.reviewRecyclerview.layoutAnimation =
                        LayoutAnimationController(setSlideIn(uiSettings), 0.25f)
                    binding.reviewTxt.slideStart(700, 0)
                }
                val chipGroup: ChipGroup = binding.chipGroup
                if ((fragmentAdapter as AnimeScreen).isLoaded) {

                    for (data in chipData) {
                        val chip = Chip(binding.root.context)
                        chip.text = data.title
                        chip.chipCornerRadius = 50f
                        chip.isCheckable = true
                        chipGroup.addView(chip)
                    }
                    fragmentAdapter.isLoaded = false
                }

                chipGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedChip = group.findViewById<Chip>(checkedId)
                    val reviewSort =
                        chipData[group.indexOfChild(selectedChip)].value // is this correct? /** thanks this is correct **/
                    selectedChipListener.invoke(reviewSort)

                }



                println("Success")
            }
            is Resource.Error -> {
                binding.reviewRecyclerview.visible()
                binding.reviewProgressBar.gone()
            }
        }
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