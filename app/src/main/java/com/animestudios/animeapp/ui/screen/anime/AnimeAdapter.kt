package com.animestudios.animeapp.ui.screen.anime

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.AnimeHdItemBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.setSafeOnClickListener
import com.animestudios.animeapp.settings.UISettings

class
AnimeAdapter(
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false
) : ListAdapter<Media, AnimeAdapter.AnimeVh>(AnimeItemCallback) {
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    inner class AnimeVh(val binding: AnimeHdItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
            itemView.setSafeOnClickListener { clicked(bindingAdapterPosition) }
            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }

        fun onBind(datA: Media) {
            binding.apply {
                setAnimation(activity, binding.root, uiSettings)
                val media = datA
                itemImg.loadImage(media!!.cover)
                titleItem.text = media.nameMAL
                binding.itemCompactScore.text = ((if (datA.userScore == 0) (datA.meanScore
                    ?: 0) else datA.userScore) / 10.0).toString()


            }
        }
    }

    fun clicked(position: Int) {
    }

    fun longClicked(position: Int): Boolean {
//        if ((mediaList?.size ?: 0) > position && position != -1) {
//            val media = mediaList?.get(position) ?: return false
//            if (activity.supportFragmentManager.findFragmentByTag("list") == null) {
//                MediaListDialogSmallFragment.newInstance(media).show(activity.supportFragmentManager, "list")
//                return true
//            }
//        }
        return false
    }

    object AnimeItemCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeVh {
        return AnimeVh(
            binding = AnimeHdItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: AnimeVh, position: Int) {
        holder.onBind(getItem(position))
    }
}