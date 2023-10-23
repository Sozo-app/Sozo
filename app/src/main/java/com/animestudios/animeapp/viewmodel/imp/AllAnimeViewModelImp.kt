package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.viewmodel.AllAnimeViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AllAnimeViewModelImp : AllAnimeViewModel, ViewModel() {
    override val result: MutableLiveData<Resource<SearchResults>> = MutableLiveData()
    var searched = false
    var notSet = true
    private val repository = AniListRepositoryImp()
    lateinit var searchResults: SearchResults
    var loaded: Boolean = false
     var doneListener: (() -> Unit)? = null

    override fun loadNextPage(r: SearchResults) {
        val data = r.copy(page = r.page + 1)
        result.postValue(Resource.Loading)
        repository.getSearch(data).onEach {

            it.onFailure {
                result.postValue(Resource.Error(it))

            }

            it.onSuccess {
                result.postValue(Resource.Success(it))
            }
        }.launchIn(viewModelScope)

    }

    override fun loadAllAnimeList() {
        result.postValue(Resource.Loading)
        repository.randomAnimeList().onEach {

            it.onFailure {
                result.postValue(Resource.Error(it))

            }

            it.onSuccess {

                result.postValue(Resource.Success(it))
            }
        }.launchIn(viewModelScope)
    }
}