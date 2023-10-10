package com.animestudios.animeapp.ui.screen.home.youtube

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.AnimeHomeItemBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.settings.UISettings

class HomeAnimeAdapter(
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false
) : RecyclerView.Adapter<HomeAnimeAdapter.HomeAnimeVh>() {
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()
    private val list = ArrayList<Media>()
    private lateinit var itemClickListener: ((Media) -> Unit)

    fun setItemClickListener(listener: (Media) -> Unit) {
        itemClickListener = listener
    }

    inner class HomeAnimeVh(private val binding: AnimeHomeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
        }

        fun onBind(data: Media) {
            binding.apply {
                setAnimation(context = binding.root.context, binding.root, uiSettings)
                val media = list.getOrNull(bindingAdapterPosition)
                if (media != null) {
                    if (media.anime != null) {
                        val episode = data.anime?.totalEpisodes ?: 0
                        if (episode != 0) {
                            itemCompactScore.text = episode.toString()
                            itemCompactScoreBG.visible()
                        } else {
                            itemCompactScoreBG.gone()
                        }
                        profilePhoto.loadImage(media.banner ?: media.cover)

                    }
                    animeImage.loadImage(media.banner ?: media.cover, 400)
                    titleItem.text = data.userPreferredName
                    root.setOnClickListener {
                        itemClickListener.invoke(media)
                    }
                }


//                time.text = data.anime.episodeDuration!!.formatDurationForItem()
//                descriptionItem.text =
//                    data.nameMAL + " • Duration ${data.anime.episodeDuration!!.formatDuration()} • Episodes ${data.anime.totalEpisodes}"
            }
        }
    }

    object HomeItemCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeAnimeAdapter.HomeAnimeVh {
        return HomeAnimeVh(
            AnimeHomeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    fun submitList(newList: List<Media>) {
        list.clear()
        list.addAll(newList)
    }

    override fun onBindViewHolder(holder: HomeAnimeVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
