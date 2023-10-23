package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.anilist.response.Query
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.viewmodel.AniListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class AniListViewModelImp() : AniListViewModel, ViewModel() {
    private val repository = AniListRepositoryImp()
    override val recentlyUpdatedList: MutableLiveData<MutableList<Media>> =
        MutableLiveData()
    override val recentlyTrendList: MutableLiveData<MutableList<Media>> = MutableLiveData()
    override val loadPopular: MutableLiveData<SearchResults> =
        MutableLiveData()
    override val loadBannerFullData: MutableLiveData<Resource<Media>> =
        MutableLiveData()
    override val genreCollection: MutableLiveData<Resource<Query.GenreCollection>> =
        MutableLiveData()
    override val getMainData: MutableLiveData<Resource<Resource<Pair<Query.GenreCollection, List<Media>>>>> =
        MutableLiveData()
    override val getHomeAnimeList: MutableLiveData<Resource<List<Media>>> =
        MutableLiveData()
    private val trending: MutableLiveData<MutableList<Media>> =
        MutableLiveData()
    lateinit var searchResults: SearchResults


    private val type = "ANIME"
    var loaded = false
    var notSet = true


    init {
        loadAnimeSection(1)

    }

    override fun getHomeAnimeListByGenre(genre: MutableList<String>) {
        getHomeAnimeList.postValue(Resource.Loading)
        repository.getAnimeListByGenre(genre).onEach {
            it.onSuccess {
                getHomeAnimeList.postValue(Resource.Success(it.results))

            }

            it.onFailure {
                getHomeAnimeList.postValue(Resource.Error(it))

            }

        }.launchIn(viewModelScope)
    }

    override fun loadHome() {

        viewModelScope.launch(Dispatchers.IO) {
            genreCollection.postValue(Resource.Loading)
            getHomeAnimeList.postValue(Resource.Loading)
            repository.getGenre()
                .zip(repository.getAnimeListByGenre(mutableListOf("Action"))) { firstData, secondData ->
                    firstData.onSuccess {
                        genreCollection.postValue(Resource.Success(it))
                    }

                    secondData.onSuccess {
                        getHomeAnimeList.postValue(Resource.Success(it.results))

                    }
                }.launchIn(viewModelScope)

        }

    }


    override fun loadAnimeSection(i: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPopularBanner(i)
                .onEach { firstData ->
                    firstData.onFailure {

                    }
                    firstData.onSuccess {
                        loadPopular.postValue(it)
                    }

                }.launchIn(viewModelScope)
            delay(100)
            repository.recentlyUpdated()
                .onEach { secondData ->

                    secondData.onFailure {

                    }

                    secondData.onSuccess {
                        saveData("dabbaList", it!!)
                        recentlyUpdatedList.postValue(it)

                    }
                }.launchIn(viewModelScope)
            delay(100)

            repository.forYouAnimeList().onEach {
                it.onSuccess {
                    recentlyTrendList.postValue(it)
                }
            }.launchIn(viewModelScope)
        }
    }

    override fun loadFullDataByMedia(media: Media) {
        loadBannerFullData.postValue(Resource.Loading)
        repository.getFullDataById(media).onEach {
            it.onSuccess {
                loadBannerFullData.postValue(
                    Resource.Success(it)
                )
            }
            it.onFailure {
                loadBannerFullData.postValue(
                    Resource.Error(it)
                )
            }

        }.launchIn(viewModelScope)
    }

}