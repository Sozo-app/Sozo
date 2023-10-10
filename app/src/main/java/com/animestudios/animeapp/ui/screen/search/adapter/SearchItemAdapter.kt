package com.animestudios.animeapp.ui.screen.search.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.animestudios.animeapp.databinding.AnimeItemBinding
import com.animestudios.animeapp.databinding.SearchBigItemBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.model.Genre
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.setSafeOnClickListener
import com.animestudios.animeapp.setSlideIn
import com.animestudios.animeapp.setSlideUp
import com.animestudios.animeapp.settings.UISettings

class SearchItemAdapter(
    var type: Int,
    private val mediaList: MutableList<Media>?,
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false,
    private val viewPager: ViewPager2? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (type) {
            0 -> MediaViewHolder(
                AnimeItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            1 -> MediaLargeViewHolder(
                SearchBigItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException()
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (type) {
            0 -> {
                val b = (holder as MediaViewHolder).binding
                setAnimation(activity, b.root, uiSettings)

                val media = mediaList?.getOrNull(position)
                if (media != null) {
                    holder.binding.apply {
                        itemImg.loadImage(media.cover ?: media.cover)
                        b.itemCompactScore.text =
                            ((if (media.userScore == 0) (media.meanScore
                                ?: 0) else media.userScore) / 10.0).toString()
                        titleItem.text = media.userPreferredName
                    }
                }
            }
            1 -> {
                val b = (holder as MediaLargeViewHolder).binding
                setAnimation(activity, b.root, uiSettings)
                val media = mediaList?.get(position)
                if (media != null) {
                    b.apply {
                        if (media.anime != null) {
                            b.itemTotal.text = " " + if ((media.anime.totalEpisodes
                                    ?: 0) != 1
                            ) "Episodes"
                            else "Episode"
                            b.itemCompactTotal.text =
                                if (media.anime.nextAiringEpisode != null) (media.anime.nextAiringEpisode.toString() + " / " + (media.anime.totalEpisodes
                                    ?: "??").toString()) else (media.anime.totalEpisodes
                                    ?: "??").toString()

                            b.title.text = media.userPreferredName
                            b.itemCompactScore.text =
                                ((if (media.userScore == 0) (media.meanScore
                                    ?: 0) else media.userScore) / 10.0).toString()

                            b.itemImg.loadImage(media.cover ?: media.banner)
                            val list = ArrayList<Genre>()

                            var count = 0
                            media.genres.onEach {
                                if (count <= 2) {
                                    count++
                                    list.add(Genre(it))
                                }
                            }
                            b.genreList.adapter = GenreRvAdapter(root.context, list)
                        }

                    }

                }
            }
        }
    }

    override fun getItemCount() = mediaList!!.size

    override fun getItemViewType(position: Int): Int {
        return type
    }

    inner class MediaViewHolder(val binding: AnimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
            itemView.setSafeOnClickListener { clicked(bindingAdapterPosition) }
            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }
    }

    inner class MediaLargeViewHolder(val binding: SearchBigItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setSafeOnClickListener { clicked(bindingAdapterPosition) }
            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }
    }

    fun clicked(position: Int) {
        if ((mediaList?.size ?: 0) > position && position != -1) {
            val media = mediaList?.get(position)
//            ContextCompat.startActivity(
//                activity,
//                Intent(activity, MediaDetailsActivity::class.java).putExtra(
//                    "media",
//                    media as Serializable
//                ), null
//            )
        }
    }

    fun longClicked(position: Int): Boolean {
        if ((mediaList?.size ?: 0) > position && position != -1) {
            val media = mediaList?.get(position) ?: return false
//            if (activity.supportFragmentManager.findFragmentByTag("list") == null) {
//                MediaListDialogSmallFragment.newInstance(media)
//                    .show(activity.supportFragmentManager, "list")
//                return true
//            }
        }
        return false
    }
}