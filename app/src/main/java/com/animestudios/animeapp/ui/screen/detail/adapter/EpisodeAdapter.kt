package com.animestudios.animeapp.ui.screen.detail.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.anilist.response.Episode
import com.animestudios.animeapp.databinding.ItemEpisodeCompatBinding
import com.animestudios.animeapp.logger
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.ui.screen.detail.pages.episodes.EpisodeScreen
import com.animestudios.animeapp.updateAnilistProgress
import com.animestudios.animeapp.widget.handleProgress
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl

class EpisodeAdapter(
    private val media: Media,
    private val fragment: EpisodeScreen,
    var arr: List<Episode> = arrayListOf()
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {
    fun onEpisodeClick(media: Media, i: String, manager: FragmentManager, launch: Boolean = true, prevEp: String? = null) {
        Handler(Looper.getMainLooper()).post {
            if (manager.findFragmentByTag("dialog") == null && !manager.isDestroyed) {
                if (media.anime?.episodes?.get(i) != null) {
                    media.anime.selectedEpisode = i
                } else {
                    snackString("Couldn't find episode : $i")
                    return@post
                }
            }
        }
    }

    inner class EpisodeViewHolder(val binding: ItemEpisodeCompatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(ep: Episode) {
            binding.apply {
                val title =
                    "${if (!ep.title.isNullOrEmpty() && ep.title != "null") "" else "Episode "}${if (!ep.title.isNullOrEmpty() && ep.title != "null") ep.title else ep.number}"
                setAnimation(fragment.requireContext(), binding.root, fragment.uiSettings)

                val thumb = ep.thumb?.let { if(it.url.isNotEmpty()) GlideUrl(it.url) { it.headers } else null }
                Glide.with(binding.itemEpisodeImage).load(thumb?:media.cover).override(400,0).into(binding.itemEpisodeImage)
                binding.itemEpisodeNumber.text = (absoluteAdapterPosition).toString()
                binding.itemEpisodeTitle.text = title
                logger(ep.maxLength.toString(),true)
                if (ep.filler) {
                    binding.itemEpisodeFiller.visibility = View.VISIBLE
                    binding.itemEpisodeFillerView.visibility = View.VISIBLE
                } else {
                    binding.itemEpisodeFiller.visibility = View.GONE
                    binding.itemEpisodeFillerView.visibility = View.GONE
                }
                println(ep.desc)
                binding.itemEpisodeDesc.visibility = if (ep.desc != null && ep.desc?.trim(' ') != "") View.VISIBLE else View.GONE
                binding.itemEpisodeDesc.text = ep.desc ?: ""

                if (media.userProgress != null) {
                    if ((ep.number.toFloatOrNull() ?: 9999f) <= media.userProgress!!.toFloat()) {
                        binding.itemEpisodeViewedCover.visibility = View.VISIBLE
                        binding.itemEpisodeViewed.visibility = View.VISIBLE
                    } else {
                        binding.itemEpisodeViewedCover.visibility = View.GONE
                        binding.itemEpisodeViewed.visibility = View.GONE
                        binding.itemEpisodeCont.setOnLongClickListener {
                            updateAnilistProgress(media, ep.number)
                            true
                        }
                    }
                } else {
                    binding.itemEpisodeViewedCover.visibility = View.GONE
                    binding.itemEpisodeViewed.visibility = View.GONE
                }

                handleProgress(
                    binding.itemEpisodeProgressCont,
                    binding.itemEpisodeProgress,
                    binding.itemEpisodeProgressEmpty,
                    media.id,
                    ep.number
                )
            }
        }
    }


    override fun getItemCount(): Int {
        return arr.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            ItemEpisodeCompatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.onBind(arr.get(position))
    }
}