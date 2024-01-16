package com.animestudios.animeapp.ui.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils.clamp
import androidx.core.text.HtmlCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.DetailScreenBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.ImageUtil
import com.animestudios.animeapp.ui.screen.detail.adapter.TabAdapter
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import com.flaviofaria.kenburnsview.RandomTransitionGenerator
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.abs

/**
 * Load Data By Anilist Id
 * @see media
 * @author Azamov X
 * @param [model] this DetailActivity ViewModel
 * @constructor Create an Empty
 **/
@AndroidEntryPoint
class DetailActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {
    private var _binding: DetailScreenBinding? = null
    lateinit var media: Media
    private val binding get() = _binding!!
    private val scope = lifecycleScope
    private val model: DetailsViewModelImpl by viewModels()
    private val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()


    /**
     * OnCreate
     * @see DetailActivity
     **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenWidth = resources.displayMetrics.widthPixels.toFloat()
        _binding = DetailScreenBinding.inflate(layoutInflater)
        binding.mediaTitle.isSelected = true;
        setContentView(binding.root)
        initActivity(this)
        media = intent.getSerializableExtra("media") as Media ?: return
        initUiLayoutParams()
        setTab()
        observerData()
        binding.mediaAppBar.addOnOffsetChangedListener(this)
        val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(true) }
        live.observe(this) {
            if (it) {
                scope.launch(Dispatchers.IO) {
                    model.loadMedia(media)
                    live.postValue(false)
                }
            }
        }
    }

    /**
     * Init Favorite Icon Clicks
     **/
    private fun initFabClick(): PopImageButton? {
        val favButton = if (Anilist.userid != null) {
            if (media.isFav) binding.mediaFav.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_round_favorite_24
                )
            )
            PopImageButton(
                scope,
                binding.mediaFav,
                R.drawable.ic_round_favorite_24,
                R.drawable.ic_heart,
                R.color.nav_tab,
                R.color.fav,
                media.isFav
            ) {
                media.isFav = it
                model.toggleFavorite(media.id)
                Refresh.all()
            }
        } else {
            binding.mediaFav.visibility = View.GONE
            null
        }
        return favButton
    }

    /**
     * @author Azamov X
     * @see model
     **/
    @SuppressLint("SetTextI18n")
    private fun observerData() {
        model.getMedia().observe(this) {
            if (it != null) {
                //Load Media Data By Id
                media = it
                loadImages(media)

                binding.title.text = it.mainName()
                binding.mediaTitle.text = it.userPreferredName


                if (it.anime!!.totalEpisodes != 1 || it.anime.nextAiringEpisode != 1) {
                    binding.itemCompactTotal.text =
                        "1 / ${if (it.anime!!.nextAiringEpisode != null) (it.anime.nextAiringEpisode.toString()) else it.anime.totalEpisodes}"
                } else {
                    binding.itemCompactTotal.text = "1"
                }

                if (it.genres.isNotEmpty()){
                    binding.mainData.text = "${it.anime!!.seasonYear ?: "??"} · ${it.genres.get(0)}  ${
                        if (it.genres.size != 1) "/" + it.genres.get(1) else ""
                    } · ${it.format}"

                }else{
                    binding.mainData.text = "${it.anime!!.seasonYear ?: "??"} · ${it.format}"

                }


                val desc = HtmlCompat.fromHtml(
                    (media.description ?: "null").replace("\\n", "<br>").replace("\\\"", "\""),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                binding.mediaInfoDescription.text =
                    "\t\t\t" + if (desc.toString() != "null") desc else "No Description Available"

                scope.launch {
                    if (it.isFav != initFabClick()?.clicked) initFabClick()?.clicked()
                }



                binding.mediaAppBar.visible()
                binding.fabBack.visible()
                binding.viewPager.visible()
                binding.animeDetailProgress.gone()
                binding.mediaNotify.setOnClickListener {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_TEXT, media.shareLink)
                    startActivity(Intent.createChooser(i, media.userPreferredName))
                }
                binding.mediaNotify.setOnLongClickListener {
                    openLinkInBrowser(media.shareLink)
                    true
                }

            }
        }

    }

    /**
     * Update Toolbar Margins,Sizes
     * @see updateLayoutParams
     * **/
    private fun initUiLayoutParams() {

        binding.itemCompactBannerNoKen.updateLayoutParams { height += statusBarHeight }
        binding.imageView.updateLayoutParams { height += statusBarHeight }
        binding.shapeableImageView2.updateLayoutParams { height += statusBarHeight }
        binding.fabBack.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }
        binding.mediaCollapsing.minimumHeight = statusBarHeight
    }

    /**
     * Load Anime Cover,Banner
     * @see loadImages
     * **/
    private fun loadImages(media: Media) {
        binding.coverImage.loadImage(media.cover)
        val banner = binding.itemCompactBannerNoKen
        if (uiSettings.bannerAnimations) {
            val adi = AccelerateDecelerateInterpolator()
            val generator = RandomTransitionGenerator(
                (10000 + 15000 * (uiSettings.animationSpeed)).toLong(),
                adi
            )
            binding.itemCompactBannerNoKen.setTransitionGenerator(generator)

        }
        media.selected = model.loadSelected(media)
        banner.loadImage(media.banner ?: media.cover, 400)

        binding.mediaStatus.text = media.status ?: ""
        binding.coverImage.setOnClickListener {
            ImageUtil.showFullScreenImage(binding.root.context, media.extraLarge.toString(), binding.coverImage)
        }
    }

    /**
     * @author Azamov X
     * @param  Tablayout Tablayout in the application is being populated with Data here
     * @see TabLayoutMediator
     * **/
    private fun setTab() {
        binding.apply {
            val adapter = TabAdapter(supportFragmentManager, lifecycle)
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

    /**
     * @author @saikou_absolute
     * @param  AnimationToolbar This will be displayed if you scroll the screen
     * @see onOffsetChanged
     * **/
    private var isCollapsed = false
    private var isCollapsedForAppbar = false
    private val percent = 70
    private var screenWidth = 0f
    private var mMaxScrollSize = 0

    @SuppressLint("ResourceType")
    override fun onOffsetChanged(appBar: AppBarLayout, i: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBar.totalScrollRange
        val percentage = abs(i) * 100 / mMaxScrollSize
        val cap = clamp((percent - percentage) / percent.toFloat(), 0f, 1f)
        val duration = (300 * uiSettings.animationSpeed).toLong()

        binding.animeCover.scaleX = 1f * cap
        binding.animeCover.scaleY = 1f * cap
        binding.animeCover.cardElevation = 32f * cap
        ObjectAnimator.ofFloat(binding.mediaTitleToolbar, "translationX", 0f)
            .setDuration(duration).start()


        binding.animeCover.visibility =
            if (binding.animeCover.scaleX == 0f) View.GONE else View.VISIBLE
        binding.animeCover.visibility =
            if (binding.animeCover.scaleX == 0f) View.GONE else View.VISIBLE


        if (percentage >= percent && !isCollapsed) {
            isCollapsed = true
            binding.itemCompactBannerNoKen.pause()
            window.statusBarColor = ContextCompat.getColor(this, R.color.detail_Bg)
            ObjectAnimator.ofFloat(binding.mediaTitleToolbar, "translationX", 0f)
                .setDuration(duration).start()
            ObjectAnimator.ofFloat(binding.mediaAccessContainer, "translationX", screenWidth)
                .setDuration(duration).start()
            ObjectAnimator.ofFloat(binding.mediaCollapseContainer, "translationX", screenWidth)
                .setDuration(duration).start()
            binding.mediaTitleToolbar.visible()
            binding.fabBack.gone()
            binding.mediaAccessContainer.gone()
        }
        if (percentage <= percent && isCollapsed) {
            isCollapsed = false
            window.statusBarColor = ContextCompat.getColor(this, R.color.nav_bg_inv)
            binding.itemCompactBannerNoKen.resume()
            ObjectAnimator.ofFloat(binding.mediaTitleToolbar, "translationX", -screenWidth)
                .setDuration(duration).start()
            ObjectAnimator.ofFloat(binding.mediaAccessContainer, "translationX", 0f)
                .setDuration(duration).start()
            ObjectAnimator.ofFloat(binding.mediaCollapseContainer, "translationX", 0f)
                .setDuration(duration).start()
            binding.fabBack.visible()
            binding.mediaTitleToolbar.gone()
            binding.mediaAccessContainer.visible()


        }

    }
}

