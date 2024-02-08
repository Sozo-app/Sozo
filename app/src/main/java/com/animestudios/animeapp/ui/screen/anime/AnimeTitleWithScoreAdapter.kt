package com.animestudios.animeapp.ui.screen.anime

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.AnimeItemBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.ui.screen.list.bottomsheet.MediaListDialogSmallFragment

class
AnimeTitleWithScoreAdapter(
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false,
) : RecyclerView.Adapter<AnimeTitleWithScoreAdapter.AnimeNoTitleVh>() {
    private val list: MutableList<Media> = ArrayList()

    lateinit var clickListener: ((Media) -> Unit)

    fun setItemClickListener(listener: ((Media) -> Unit)) {
        clickListener = listener
    }

    inner class AnimeNoTitleVh(val binding: AnimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
            itemView.setSafeOnClickListener { clicked(bindingAdapterPosition) }
            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }

        fun onBind(datA: Media) {
            val uiSettings =
                readData<UISettings>("ui_settings") ?: UISettings()
            binding.apply {
                if (uiSettings.layoutAnimations) {
                    setAnimation(activity, binding.root, uiSettings)
                    binding.titleItem.animation = setSlideIn(uiSettings)
                }
                if (datA.isFav) binding.isFav.visible() else binding.isFav.gone()
                itemImg.loadImage(datA.cover ?: datA.banner)
                binding.itemCompactScore.text = ((if (datA.userScore == 0) (datA.meanScore
                    ?: 0) else datA.userScore) / 10.0).toString()
                binding.titleItem.text = datA.userPreferredName
            }
        }
    }

    fun clicked(position: Int) {
        clickListener.invoke(list[position])
    }

    fun longClicked(position: Int): Boolean {
        if (list.size > position && position != -1) {
            val media = list.get(position)
            if (activity.supportFragmentManager.findFragmentByTag("list") == null) {
                MediaListDialogSmallFragment.newInstance(media)
                    .show(activity.supportFragmentManager, "list")
                return true
            }
        }
        return false
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeNoTitleVh {
        return AnimeNoTitleVh(
            binding = AnimeItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    fun submitLit(newList: MutableList<Media>) {
        list.clear()
        list.addAll(newList)
    }

    override fun onBindViewHolder(holder: AnimeNoTitleVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}