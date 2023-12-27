package com.animestudios.animeapp.ui.screen.detail.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.method.Touch.scrollTo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat.getChannelId
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.ItemAnimeWatchBinding
import com.animestudios.animeapp.databinding.ItemChipBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.sourcers.WatchSources
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.post
import com.animestudios.animeapp.ui.screen.detail.pages.episodes.EpisodeScreen
import com.animestudios.animeapp.widget.handleProgress
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

        //Youtube
//        if (media.anime!!.youtube != null && fragment.uiSettings.showYtButton) {
//            binding.animeSourceYT.visibility = View.VISIBLE
//            binding.animeSourceYT.setOnClickListener {
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(media.anime.youtube))
//                fragment.requireContext().startActivity(intent)
//            }
//        }

        binding.animeSourceTitle.isSelected = true
        val source = media.selected!!.source.let { if (it >= watchSources.names.size) 0 else it }

        watchSources[source].apply {
            this.selectDub = media.selected!!.preferDub
            binding.animeSourceTitle.text = showUserText
            showUserTextListener = { MainScope().launch { binding.animeSourceTitle.text = it } }
        }


    }


    override fun getItemCount(): Int = 1

    inner class ViewHolder(val binding: ItemAnimeWatchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            //Timer
            countDown(media, binding.animeSourceContainer)
        }
    }
}