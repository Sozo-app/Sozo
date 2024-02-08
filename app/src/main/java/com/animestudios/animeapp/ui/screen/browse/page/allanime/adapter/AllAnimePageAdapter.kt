package com.animestudios.animeapp.ui.screen.browse.page.allanime.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.AnimeHdItemBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.ui.activity.DetailActivity
import com.animestudios.animeapp.ui.screen.list.bottomsheet.MediaListDialogSmallFragment
import java.io.Serializable

class AllAnimePageAdapter(
    private val list: MutableList<Media>?,
    private val matchParent: Boolean = false,
    private val activity: FragmentActivity
) :
    RecyclerView.Adapter<AllAnimePageAdapter.AllAnimeVh>() {
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    inner class AllAnimeVh(private val binding: AnimeHdItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
            itemView.setSafeOnClickListener { clicked(bindingAdapterPosition) }
            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }

        fun onBind(media: Media) {
            binding.apply {
                titleItem.isSelected = true
                setAnimation(activity, binding.root, uiSettings)
                if (media.isFav) binding.isFav.visible() else binding.isFav.gone()
                itemImg.loadImage(media!!.cover)
//                binding.itemCompactScore.text = ((if (media.userScore == 0) (media.meanScore
//                    ?: 0) else media.userScore) / 10.0).toString()
                titleItem.text = media.userPreferredName
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAnimeVh {

        val binding =
            AnimeHdItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllAnimeVh(
            binding
        )
    }

    override fun onBindViewHolder(holder: AllAnimeVh, position: Int) {

        holder.onBind(list!!.get(position))
    }


    fun longClicked(position: Int): Boolean {
        if (list!!.size > position && position != -1) {
            val media = list.get(position)
            if (activity.supportFragmentManager.findFragmentByTag("list") == null) {
                MediaListDialogSmallFragment.newInstance(media)
                    .show(activity.supportFragmentManager, "list")
                return true
            }
        }
        return false
    }

    fun clicked(position: Int) {
        if ((list?.size ?: 0) > position && position != -1) {
            val media = list?.get(position)
            ContextCompat.startActivity(
                activity,
                Intent(activity, DetailActivity::class.java).putExtra(
                    "media",
                    media as Serializable
                ), null
            )
        }
    }

    override fun getItemCount(): Int {
        return list!!.size
    }
}