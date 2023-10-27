package com.animestudios.animeapp.ui.screen.home.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.animestudios.animeapp.databinding.BannerItemBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.settings.UISettings
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestOptions
import com.flaviofaria.kenburnsview.RandomTransitionGenerator
import jp.wasabeef.glide.transformations.BlurTransformation

class BannerAdapter(
    var type: Int,
    private val mediaList: MutableList<Media>?,
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false,
    private val viewPager: ViewPager2? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()


    @SuppressLint("ClickableViewAccessibility")
    inner class MediaPageSmallViewHolder(val binding: BannerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnTouchListener { _, _ -> true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MediaPageSmallViewHolder(
            BannerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val b = (holder as MediaPageSmallViewHolder).binding
        val media = mediaList?.get(position)
        if (media != null) {
            if (uiSettings.layoutAnimations)
                b.itemCompactBanner.setTransitionGenerator(
                    RandomTransitionGenerator(
                        (10000 + 15000 * (uiSettings.animationSpeed)).toLong(),
                        AccelerateDecelerateInterpolator()
                    )
                )
            val banner =
                if (uiSettings.layoutAnimations) b.itemCompactBanner else b.itemCompactBannerNoKen
            val context = b.itemCompactBanner.context
            if (!(context as Activity).isDestroyed)
                Glide.with(context as Context)
                    .load(GlideUrl(media.banner ?: media.cover))
                    .diskCacheStrategy(DiskCacheStrategy.ALL).override(400)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(2, 3)))
                    .into(banner)

            b.itemCompactImage.loadImage(media.cover)
            b.title.text = media.userPreferredName
            b.addToListBtn.setOnClickListener {
                if ((mediaList?.size ?: 0) > position && position != -1) {
                    val media = mediaList?.get(position)
                    if (activity.supportFragmentManager.findFragmentByTag("list") == null) {
//                        MediaListDialogSmallFragment.newInstance(media).show(activity.supportFragmentManager, "list")
                    }
                }
            }
            var genresL = ""
            media.genres.apply {
                var count = 0
                if (isNotEmpty()) {
                    forEach {
                        if (count <= 2) {


                            count++
                            genresL += "$it • "
                        }
                    }
                    genresL = genresL.removeSuffix(" • ")
                }

                val genres =
                    "${media.anime?.totalEpisodes} Episodes\n${genresL}"
                b.itemDescription.text = genres
            }
            @SuppressLint("NotifyDataSetChanged")
            if (position == mediaList!!.size - 2 && viewPager != null) viewPager.post {
                val size = mediaList.size
                mediaList.addAll(mediaList)
                notifyItemRangeInserted(size - 1, mediaList.size)
            }
        }

    }

    override fun getItemCount(): Int {
        return mediaList!!.size
    }

}
