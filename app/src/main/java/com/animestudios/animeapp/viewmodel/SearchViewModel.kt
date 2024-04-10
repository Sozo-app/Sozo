package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.anilist.response.SearchResultsCharacter
import com.animestudios.animeapp.media.Media

interface SearchViewModel {
     val result: MutableLiveData<SearchResults?>
     val resultCharacter: MutableLiveData<SearchResultsCharacter?>
     val resultRecommendedAnimeList: MutableLiveData<ArrayList<Media?>?>
     fun loadSearch(r:SearchResults)
     fun loadSearchCharacter(r:SearchResultsCharacter)
     fun recommendedAnimeList()
     fun loadNextPage(r:SearchResults)
     fun loadNextPageCharacter(r:SearchResultsCharacter)
     fun loadNextPageUser(r:SearchResults)
     fun loadNextPageStaff(r:SearchResults)
     fun loadSearchUser(r:SearchResults)
     fun loadSearchStaff(r:SearchResults)
}