package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.anilist.response.Query
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.tools.Resource

interface AniListViewModel {
    val recentlyUpdatedList: MutableLiveData<MutableList<Media>>
    val recentlyTrendList: MutableLiveData<MutableList<Media>>
    val loadPopular: MutableLiveData<SearchResults>
    val loadBannerFullData: MutableLiveData<Resource<Media>>
    val getHomeAnimeList: MutableLiveData<Resource<List<Media>>>
    val genreCollection: MutableLiveData<Resource<Query.GenreCollection>>
    val getMainData: MutableLiveData<Resource<Resource<Pair<Query.GenreCollection, List<Media>>>>>
    val getReview: MutableLiveData<Resource<List<Review>>>
    fun getHomeAnimeListByGenre(genre: MutableList<String>)
    fun loadHome()
    fun loadFullDataByMedia(media: Media)
    fun loadAnimeSection(i: Int)
    fun loadReview()

}