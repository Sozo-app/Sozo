package com.animestudios.animeapp.ui.screen.detail.pages.episodes

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.animestudios.animeapp.anilist.response.Episode
import com.animestudios.animeapp.databinding.EpisodeScreenBinding
import com.animestudios.animeapp.dp
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.navBarHeight
import com.animestudios.animeapp.parsers.AnimeParser
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.sourcers.AnimeSources
import com.animestudios.animeapp.sourcers.HAnimeSources
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.ui.screen.detail.adapter.AnimeWatchAdapter
import com.animestudios.animeapp.ui.screen.detail.adapter.EpisodeAdapter
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@AndroidEntryPoint
class EpisodeScreen : Fragment() {
    private lateinit var headerAdapter: AnimeWatchAdapter

    private var _binding: EpisodeScreenBinding? = null
    private lateinit var media: Media
    private lateinit var episodeAdapter: EpisodeAdapter
    var screenWidth = 0f
    private var progress = View.VISIBLE
    var continueEp: Boolean = false
    private var start = 0
    private var end: Int? = null
    private var reverse = false
    var loaded = false
    private var style: Int? = null

    private val binding get() = _binding!!
    lateinit var uiSettings: UISettings

    private val model by activityViewModels<DetailsViewModelImpl>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = EpisodeScreenBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.animeSourceRecycler.updatePadding(bottom = binding.animeSourceRecycler.paddingBottom + navBarHeight)
        screenWidth = resources.displayMetrics.widthPixels.dp

        var maxGridSize = (screenWidth / 100f).roundToInt()
        maxGridSize = max(4, maxGridSize - (maxGridSize % 2))
        uiSettings = readData("ui_settings", toast = false) ?: UISettings().apply {
            saveData(
                "ui_settings",
                this
            )
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), maxGridSize)

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val style = episodeAdapter.getItemViewType(position)

                return when (position) {
                    0 -> maxGridSize
                    else -> when (style) {
                        0 -> maxGridSize
                        1 -> 2
                        2 -> 1
                        else -> maxGridSize
                    }
                }
            }
        }

        binding.animeSourceRecycler.layoutManager = gridLayoutManager

        model.scrolledToTop.observe(viewLifecycleOwner) {
            if (it) binding.animeSourceRecycler.scrollToPosition(0)
        }

        continueEp = model.continueMedia ?: false
        model.getMedia().observe(viewLifecycleOwner) {
            if (it != null) {
                media = it
                media.selected = model.loadSelected(media)


                style = media.selected!!.recyclerStyle
                reverse = media.selected!!.recyclerReversed

                progress = View.GONE
                binding.mediaInfoProgressBar.visibility = progress

                if (!loaded) {
                    model.watchSources = if (media.isAdult) HAnimeSources else AnimeSources

                    headerAdapter = AnimeWatchAdapter(it, this, model.watchSources!!)
                    episodeAdapter = EpisodeAdapter(media, this)

                    binding.animeSourceRecycler.adapter =
                        ConcatAdapter(headerAdapter, episodeAdapter)

                    lifecycleScope.launch(Dispatchers.IO) {
                        awaitAll(
                            async { model.loadKitsuEpisodes(media) },
                            async { model.loadFillerEpisodes(media) }
                        )
                        model.loadEpisodes(media, media.selected!!.source)
                    }
                    loaded = true
                } else {
                    reload()
                }
            }
        }
        model.getEpisodes().observe(viewLifecycleOwner) { loadedEpisodes ->
            if (loadedEpisodes != null) {
                val episodes = loadedEpisodes[media.selected!!.source]
                if (episodes != null) {
                    episodes.forEach { (i, episode) ->
                        if (media.anime?.fillerEpisodes != null) {
                            if (media.anime!!.fillerEpisodes!!.containsKey(i)) {
                                episode.title =
                                    episode.title ?: media.anime!!.fillerEpisodes!![i]?.title
                                episode.filler = media.anime!!.fillerEpisodes!![i]?.filler ?: false
                            }
                        }
                        if (media.anime?.kitsuEpisodes != null) {
                            if (media.anime!!.kitsuEpisodes!!.containsKey(i)) {
                                episode.desc =
                                    episode.desc ?: media.anime!!.kitsuEpisodes!![i]?.desc
                                episode.title =
                                    episode.title ?: media.anime!!.kitsuEpisodes!![i]?.title
                                episode.thumb =
                                    episode.thumb ?: media.anime!!.kitsuEpisodes!![i]?.thumb
                                            ?: FileUrl[media.cover]
                            }
                        }
                    }
                    media.anime?.episodes = episodes

                    //CHIP GROUP
                    val total = episodes.size

                    val arr = media.anime!!.episodes!!.keys.toTypedArray()
                    start = 0
                    end = total

                    reload()
                }
            }
        }

        model.getKitsuEpisodes().observe(viewLifecycleOwner) { i ->
            if (i != null)
                media.anime?.kitsuEpisodes = i
        }

        model.getFillerEpisodes().observe(viewLifecycleOwner) { i ->
            if (i != null)
                media.anime?.fillerEpisodes = i
        }
    }

    fun onSourceChange(i: Int): AnimeParser {
        media.anime?.episodes = null
        reload()
        val selected = model.loadSelected(media)
        model.watchSources?.get(selected.source)?.showUserTextListener = null
        selected.source = i
        selected.server = null
        model.saveSelected(media.id, selected, requireActivity())
        media.selected = selected
        return model.watchSources?.get(i)!!
    }

    fun onDubClicked(checked: Boolean) {
        val selected = model.loadSelected(media)
        model.watchSources?.get(selected.source)?.selectDub = checked
        selected.preferDub = checked
        model.saveSelected(media.id, selected, requireActivity())
        media.selected = selected
        lifecycleScope.launch(Dispatchers.IO) { model.forceLoadEpisode(media, selected.source) }
    }



    fun loadEpisodes(i: Int) {
        lifecycleScope.launch(Dispatchers.IO) { model.loadEpisodes(media, i) }
    }

    fun onIconPressed(viewType: Int, rev: Boolean) {
        reverse = rev
        media.selected!!.recyclerReversed = reverse
        model.saveSelected(media.id, media.selected!!, requireActivity())
        reload()
    }

    fun onChipClicked(i: Int, s: Int, e: Int) {
        media.selected!!.chip = i
        start = s
        end = e
        model.saveSelected(media.id, media.selected!!, requireActivity())
        reload()
    }


    fun onEpisodeClick(i: String) {
        model.continueMedia = false
        model.saveSelected(media.id, media.selected!!, requireActivity())
        model.onEpisodeClick(media, i, requireActivity().supportFragmentManager)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reload() {
        val selected = model.loadSelected(media)

        //Find latest episode for subscription
        selected.latest =
            media.anime?.episodes?.values?.maxOfOrNull { it.number.toFloatOrNull() ?: 0f } ?: 0f
        selected.latest =
            media.userProgress?.toFloat()?.takeIf { selected.latest < it } ?: selected.latest

        model.saveSelected(media.id, selected, requireActivity())
        headerAdapter.handleEpisodes()
        episodeAdapter.notifyItemRangeRemoved(0, episodeAdapter.arr.size)
        var arr: ArrayList<Episode> = arrayListOf()
        if (media.anime!!.episodes != null) {
            val end = if (end != null && end!! < media.anime!!.episodes!!.size) end else null
            arr.addAll(
                media.anime!!.episodes!!.values.toList()
                    .slice(start..(end ?: (media.anime!!.episodes!!.size - 1)))
            )
            if (reverse)
                arr = (arr.reversed() as? ArrayList<Episode>) ?: arr
        }
        episodeAdapter.arr = arr
        episodeAdapter.notifyItemRangeInserted(0, arr.size)
    }

    override fun onDestroy() {
        model.watchSources?.flushText()
        super.onDestroy()
    }

    var state: Parcelable? = null
    override fun onResume() {
        super.onResume()
        binding.mediaInfoProgressBar.visibility = progress
        binding.animeSourceRecycler.layoutManager?.onRestoreInstanceState(state)
    }

    override fun onPause() {
        super.onPause()
        state = binding.animeSourceRecycler.layoutManager?.onSaveInstanceState()
    }

}