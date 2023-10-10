package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.anilist.response.SearchResults

interface SearchViewModel {
     val result: MutableLiveData<SearchResults?>
     fun loadSearch(r:SearchResults)
     fun loadNextPage(r:SearchResults)
}