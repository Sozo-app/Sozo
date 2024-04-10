package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.anilist.response.SearchResultsCharacter
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.viewmodel.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchViewModelImp @Inject constructor(val client: AniListClient) : ViewModel(),
    SearchViewModel {
    private val repository = AniListRepositoryImp()
    override val result: MutableLiveData<SearchResults?> = MutableLiveData()
    override val resultCharacter: MutableLiveData<SearchResultsCharacter?> = MutableLiveData()
    override val resultRecommendedAnimeList: MutableLiveData<ArrayList<Media?>?> = MutableLiveData()
    var searched = false
    var notSet = true
    lateinit var searchResults: SearchResults
    lateinit var searchResultsCharacter: SearchResultsCharacter

    override fun loadSearch(r: SearchResults) {
        repository.getSearch(r).onEach {
            it.onSuccess {
                result.postValue(it)
            }
            it.onFailure {

            }
        }.launchIn(viewModelScope)
    }

    override fun loadSearchCharacter(r: SearchResultsCharacter) {
        repository.getSearchCharacter(r).onEach {
            it.onSuccess {
                resultCharacter.postValue(it)
            }
            it.onFailure {

            }
        }.launchIn(viewModelScope)
    }

    override fun recommendedAnimeList() {
        repository.recommendedAnimeList().onEach {
            it.onSuccess {
                resultRecommendedAnimeList.postValue(it as ArrayList<Media?>?)
            }
        }.launchIn(viewModelScope)
    }

    override fun loadNextPage(r: SearchResults) {
        val data = r.copy(page = r.page + 1)
        repository.getSearch(data).onEach {
            it.onFailure {

            }

            it.onSuccess {
                result.postValue(it)
            }
        }.launchIn(viewModelScope)
    }

    override fun loadNextPageCharacter(r: SearchResultsCharacter) {
        val data = r.copy(page = r.page + 1)
        repository.getSearchCharacter(data).onEach {
            it.onFailure {

            }
            it.onSuccess {
                resultCharacter.postValue(it)
            }
        }.launchIn(viewModelScope)
    }

    override fun loadNextPageUser(r: SearchResults) {
        TODO("Not yet implemented")
    }

    override fun loadNextPageStaff(r: SearchResults) {
        TODO("Not yet implemented")
    }

    override fun loadSearchUser(r: SearchResults) {
        TODO("Not yet implemented")
    }

    override fun loadSearchStaff(r: SearchResults) {
        TODO("Not yet implemented")
    }
}