package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.Resource
import com.animestudios.animeapp.anilist.response.SearchResults

interface AllAnimeViewModel {
    val result: MutableLiveData<Resource<SearchResults>>
    fun loadNextPage(r: SearchResults)
    fun loadAllAnimeList()
}