package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.anilist.response.SearchResults

interface SearchViewModel {
     val result: MutableLiveData<SearchResults?>
     fun loadSearch(r:SearchResults)
     fun loadSearchCharacter(r:SearchResults)
     fun loadNextPage(r:SearchResults)
     fun loadNextPageCharacter(r:SearchResults)
     fun loadNextPageUser(r:SearchResults)
     fun loadNextPageStaff(r:SearchResults)
     fun loadSearchUser(r:SearchResults)
     fun loadSearchStaff(r:SearchResults)
}