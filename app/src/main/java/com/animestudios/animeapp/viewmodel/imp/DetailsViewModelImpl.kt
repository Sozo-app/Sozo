package com.animestudios.animeapp.viewmodel.imp

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.GetFullDataByIdQuery
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.anilist.response.Episode
import com.animestudios.animeapp.jikan.Jikan
import com.animestudios.animeapp.kitsu.Kitsu
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.media.Selected
import com.animestudios.animeapp.model.RelationData
import com.animestudios.animeapp.others.AniSkip
import com.animestudios.animeapp.parsers.ShowResponse
import com.animestudios.animeapp.parsers.VideoExtractor
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.sourcers.WatchSources
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.tools.tryWithSuspend
import com.animestudios.animeapp.ui.screen.detail.dialog.SelectorDialogFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModelImpl @Inject constructor(private val aniListClient: AniListClient) :
    ViewModel() {
    private val repositoryImp: AniListRepositoryImp = AniListRepositoryImp()
    val scrolledToTop = MutableLiveData(true)
    private val _coverImageForPreview = MutableLiveData<String>()
    val coverImageForPreview: LiveData<String>
        get() = _coverImageForPreview

    val getRelations = MutableLiveData<Resource<ArrayList<RelationData>>>()


    private val _getFullData = MutableLiveData<Resource<GetFullDataByIdQuery.Data>>()
    val getFullData: LiveData<Resource<GetFullDataByIdQuery.Data>>
        get() = _getFullData

    val getMediaData = MutableLiveData<Media>()

    fun getMediaById(id: Int) {
        viewModelScope.launch {
            getMediaData.postValue(Anilist.getMedia(id = id))
        }
    }

    val epChanged = MutableLiveData(true)


    fun setEpisode(ep: Episode?, who: String) {
//        logger("set episode ${ep?.number} - $who", false)
        episode.postValue(ep)
        MainScope().launch(Dispatchers.Main) {
            episode.value = null
        }
    }

    fun onEpisodeClick(
        media: Media,
        i: String,
        manager: FragmentManager,
        launch: Boolean = true,
        prevEp: String? = null
    ) {
        Handler(Looper.getMainLooper()).post {
            if (manager.findFragmentByTag("dialog") == null && !manager.isDestroyed) {
                if (media.anime?.episodes?.get(i) != null) {
                    media.anime.selectedEpisode = i
                } else {
                    snackString("Couldn't find episode : $i")
                    return@post
                }
                media.selected = this.loadSelected(media)
                val selector =
                    SelectorDialogFragment.newInstance(media.selected!!.server, launch, prevEp)
                selector.show(manager, "dialog")
            }
        }
    }

    fun loadRelationsById(id: Int) {
        viewModelScope.launch {
            getRelations.value = Resource.Loading
            val media = aniListClient.getRelationsById(id).data
            if (media != null) {
                getRelations.value = Resource.Success(media.convert())
            } else {
                getRelations.value = Resource.Error(Exception("Have Problem"))
            }
        }
    }

    fun saveSelected(id: Int, data: Selected, activity: Activity? = null) {
        saveData("$id-select", data, activity)
    }

    fun loadSelected(media: Media): Selected {
        return readData<Selected>("${media.id}-select") ?: Selected().let {
            it.source = if (media.isAdult) 0 else when (media.anime != null) {
                true -> readData("settings_def_anime_source") ?: 0
                else -> readData("settings_def_manga_source") ?: 0
            }
            it.preferDub = readData("settings_prefer_dub") ?: false
            saveSelected(media.id, it)
            it
        }
    }

    var continueMedia: Boolean? = null
    private var loading = false

    private val media: MutableLiveData<Media> = MutableLiveData<Media>(null)
    fun getMedia(): LiveData<Media> = media
    fun loadMedia(m: Media) {
        if (!loading) {
            loading = true
            viewModelScope.launch {
            }
            repositoryImp.getFullDataById(m).onEach {
                val data = aniListClient.getExtraLargeImage(m.id)
                if (data.data != null) {
                    val extraLarge = data.data?.Media?.coverImage?.extraLarge
                    val color = data.data?.Media?.coverImage?.color
                    println(color)
                    println(extraLarge)
                    val newMedia = it.copy(
                        nameRomaji = data.data!!.Media!!.title!!.romaji ?: "",
                        nativeName = data.data!!.Media!!.title!!.native ?: "",
                        englishName = data.data!!.Media!!.title?.english ?: "",
                        extraLarge = extraLarge,
                        averageScore = data.data!!.Media!!.averageScore ?: 0,
                        popularity = data.data!!.Media!!.popularity ?: 0,
                        meanScore = data.data!!.Media!!.meanScore ?: 0,
                        favourites = data.data!!.Media!!.favourites ?: 0
                    )
                    println(newMedia.extraLarge)
                    media.postValue(newMedia)
                }
            }.launchIn(viewModelScope)
        }
        loading = false
    }

    fun setMedia(m: Media) {
        media.postValue(m)
    }

    fun toggleFavorite(id: Int) {
        viewModelScope.launch {
            aniListClient.toggleFavorite(id)
        }
    }

    val responses = MutableLiveData<List<ShowResponse>?>(null)


    //Anime
    private val animeEpisodes: MutableLiveData<Map<String, Episode>> =
        MutableLiveData<Map<String, Episode>>(null)

    fun getKitsuEpisodes(): LiveData<Map<String, Episode>> = animeEpisodes
    suspend fun loadKitsuEpisodes(s: Media) {
        tryWithSuspend {
            if (animeEpisodes.value == null) animeEpisodes.postValue(Kitsu.getKitsuEpisodesDetails(s))
        }
    }

    private val fillerEpisodes: MutableLiveData<Map<String, Episode>> =
        MutableLiveData<Map<String, Episode>>(null)

    fun getFillerEpisodes(): LiveData<Map<String, Episode>> = fillerEpisodes
    suspend fun loadFillerEpisodes(s: Media) {
        tryWithSuspend {
            if (fillerEpisodes.value == null) fillerEpisodes.postValue(
                Jikan.getEpisodes(
                    s.idMAL ?: return@tryWithSuspend
                )
            )
        }
    }

    var watchSources: WatchSources? = null

    private val episodes = MutableLiveData<MutableMap<Int, MutableMap<String, Episode>>>(null)
    private val epsLoaded = mutableMapOf<Int, MutableMap<String, Episode>>()
    fun getEpisodes(): LiveData<MutableMap<Int, MutableMap<String, Episode>>> = episodes
    suspend fun loadEpisodes(media: Media, i: Int) {
        if (!epsLoaded.containsKey(i)) {
            epsLoaded[i] = watchSources?.loadEpisodesFromMedia(i, media) ?: return
        }
        episodes.postValue(epsLoaded)
    }

    suspend fun forceLoadEpisode(media: Media, i: Int) {
        epsLoaded[i] = watchSources?.loadEpisodesFromMedia(i, media) ?: return
        episodes.postValue(epsLoaded)
    }

    suspend fun overrideEpisodes(i: Int, source: ShowResponse, id: Int) {
        watchSources?.saveResponse(i, id, source)
        epsLoaded[i] = watchSources?.loadEpisodes(i, source.link, source.extra) ?: return
        episodes.postValue(epsLoaded)
    }

    private var episode = MutableLiveData<Episode?>(null)
    fun getEpisode(): LiveData<Episode?> = episode

    suspend fun loadEpisodeVideos(ep: Episode, i: Int, post: Boolean = true) {
        val link = ep.link ?: return
        if (!ep.allStreams || ep.extractors.isNullOrEmpty()) {
            val list = mutableListOf<VideoExtractor>()
            ep.extractors = list
            watchSources?.get(i)?.apply {
                if (!post && !allowsPreloading) return@apply
                loadByVideoServers(link, ep.extra) {
                    if (it.videos.isNotEmpty()) {
                        list.add(it)
                        ep.extractorCallback?.invoke(it)
                    }
                }
                ep.extractorCallback = null
                if (list.isNotEmpty())
                    ep.allStreams = true
            }
        }


        if (post) {
            episode.postValue(ep)
            MainScope().launch(Dispatchers.Main) {
                episode.value = null
            }
        }
    }

    fun getFulDataById(media: Media) {
        _getFullData.value = Resource.Loading
        viewModelScope.launch {
            _getFullData.value = Resource.Success(aniListClient.getFullDataById(media).data!!)
        }

    }

    val timeStamps = MutableLiveData<List<AniSkip.Stamp>?>()
    private val timeStampsMap: MutableMap<Int, List<AniSkip.Stamp>?> = mutableMapOf()
    suspend fun loadTimeStamps(
        malId: Int?,
        episodeNum: Int?,
        duration: Long,
        useProxyForTimeStamps: Boolean
    ) {
        malId ?: return
        episodeNum ?: return
        if (timeStampsMap.containsKey(episodeNum))
            return timeStamps.postValue(timeStampsMap[episodeNum])
        val result = AniSkip.getResult(malId, episodeNum, duration, useProxyForTimeStamps)
        timeStampsMap[episodeNum] = result
        timeStamps.postValue(result)
    }

    suspend fun loadEpisodeSingleVideo(
        ep: Episode,
        selected: Selected,
        post: Boolean = true
    ): Boolean {
        if (ep.extractors.isNullOrEmpty()) {

            val server = selected.server ?: return false
            val link = ep.link ?: return false

            ep.extractors = mutableListOf(watchSources?.get(selected.source)?.let {
                if (!post && !it.allowsPreloading) null
                else it.loadSingleVideoServer(server, link, ep.extra, post)
            } ?: return false)
            ep.allStreams = false
        }
        if (post) {
            episode.postValue(ep)
            MainScope().launch(Dispatchers.Main) {
                episode.value = null
            }
        }
        return true
    }


}