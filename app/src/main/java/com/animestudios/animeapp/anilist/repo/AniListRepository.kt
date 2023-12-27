package com.animestudios.animeapp.anilist.repo

import com.animestudios.animeapp.anilist.response.Query
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.media.Media
import kotlinx.coroutines.flow.Flow

interface AniListRepository {
    fun getGenre(): kotlinx.coroutines.flow.Flow<Result<Query.GenreCollection>>
    fun getFullDataById(media: Media): kotlinx.coroutines.flow.Flow<Media>
    fun getAnimeListByGenre(genre: MutableList<String>): Flow<Result<SearchResults>>
    fun getSearch(search: SearchResults): Flow<Result<SearchResults>>
    fun recentlyUpdated(): Flow<Result<MutableList<Media>?>>
    fun forYouAnimeList(): Flow<Result<MutableList<Media>>>
    fun randomAnimeList(): Flow<Result<SearchResults>>
    suspend fun getMediaLists(
        anime: Boolean,
        userId: Int,
        sortOrder: String? = null
    ): MutableMap<String, ArrayList<Media>>

    suspend fun getGenres(genres: ArrayList<String>, listener: ((Pair<String, String>) -> Unit))
    fun getPopularBanner(i: Int): Flow<Result<SearchResults>>
    suspend fun loadProfile(): Boolean
}