package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SearchViewModelImp : ViewModel(), SearchViewModel {
    private val repository = AniListRepositoryImp()
    override val result: MutableLiveData<SearchResults?> = MutableLiveData()
    var searched = false
    var notSet = true
    lateinit var searchResults: SearchResults

    override fun loadSearch(r: SearchResults) {
        repository.getSearch(r).onEach {
            it.onSuccess {
                result.postValue(it)
            }
            it.onFailure {

            }
        }.launchIn(viewModelScope)
    }

    override fun loadSearchCharacter(r: SearchResults) {
        TODO("Not yet implemented")
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

    override fun loadNextPageCharacter(r: SearchResults) {
        TODO("Not yet implemented")
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