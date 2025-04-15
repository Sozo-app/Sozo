package com.animestudios.animeapp.ui.screen.detail.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.countDown
import com.animestudios.animeapp.databinding.ItemAnimeWatchBinding
import com.animestudios.animeapp.databinding.ItemChipBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.model.shikimori.Screenshot
import com.animestudios.animeapp.px
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.sourcers.WatchSources
import com.animestudios.animeapp.ui.screen.detail.pages.episodes.EpisodeScreen
import com.animestudios.animeapp.visible
import com.google.android.material.chip.Chip
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AnimeWatchAdapter(
    private val media: Media,
    private val fragment: EpisodeScreen,
    private val watchSources: WatchSources
) : RecyclerView.Adapter<AnimeWatchAdapter.ViewHolder>() {

    private var _binding: ItemAnimeWatchBinding? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val bind = ItemAnimeWatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(bind)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        _binding = binding

        binding.animeSourceTitle.isSelected = true
        val source = media.selected!!.source.let { if (it >= watchSources.names.size) 0 else it }

        watchSources[source].apply {
            this.selectDub = media.selected!!.preferDub
            binding.animeSourceTitle.text = showUserText
            binding.animeSourceTitle.visible()
            showUserTextListener = { MainScope().launch { binding.animeSourceTitle.text = it } }
        }


        handleEpisodes()
    }

    fun updateScreenshotView(arrayList: List<Screenshot>) {
    }

    @SuppressLint("SetTextI18n")
    fun handleEpisodes() {
        val binding = _binding
        if (binding != null) {
            if (media.anime?.episodes != null) {
                val episodes = media.anime.episodes!!.keys.toTypedArray()

                val anilistEp = (media.userProgress ?: 0).plus(1)
                val appEp = readData<String>("${media.id}_current_ep")?.toIntOrNull() ?: 1

                val continueEp = (if (anilistEp > appEp) anilistEp else appEp).toString()
                if (episodes.contains(continueEp)) {

                    if (media.anime.episodes!!.isNotEmpty())
                        binding.animeSourceNotFound.visibility = View.GONE
                    else
                        binding.animeSourceNotFound.visibility = View.VISIBLE
                } else {
                    binding.animeSourceNotFound.visibility = View.GONE
                }
            }
        }
    }

    //Chips
    @SuppressLint("SetTextI18n")
    fun updateChips(limit: Int, names: Array<String>, arr: Array<Int>, selected: Int = 0) {
        val binding = _binding
        if (binding != null) {
            val screenWidth = fragment.screenWidth.px
            var select: Chip? = null
            for (position in arr.indices) {
                val last = if (position + 1 == arr.size) names.size else (limit * (position + 1))
                val chip =
                    ItemChipBinding.inflate(
                        LayoutInflater.from(fragment.context),
                        binding.animeSourceChipGroup,
                        false
                    ).root
                chip.isCheckable = true
                fun selected() {
                    chip.isChecked = true
                    binding.animeWatchChipScroll.smoothScrollTo(
                        (chip.left - screenWidth / 2) + (chip.width / 2),
                        0
                    )
                }
                chip.text = "${names[limit * (position)]} - ${names[last - 1]}"

                chip.setOnClickListener {
                    selected()
                    fragment.onChipClicked(position, limit * (position), last - 1)
                }
                binding.animeSourceChipGroup.addView(chip)
                if (selected == position) {
                    selected()
                    select = chip
                }
            }
            if (select != null)
                binding.animeWatchChipScroll.apply {
                    post {
                        scrollTo(
                            (select.left - screenWidth / 2) + (select.width / 2),
                            0
                        )
                    }
                }
        }
    }

    fun clearChips() {
        _binding?.animeSourceChipGroup?.removeAllViews()
    }

    override fun getItemCount(): Int = 1

    inner class ViewHolder(val binding: ItemAnimeWatchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            countDown(media, binding.animeSourceContainer)
        }
    }
}