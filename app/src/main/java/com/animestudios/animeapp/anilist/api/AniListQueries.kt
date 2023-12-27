package com.animestudios.animeapp.anilist.api

import android.app.Activity
import com.animestudios.animeapp.anilist.response.Episode
import com.animestudios.animeapp.anilist.response.Genre
import com.animestudios.animeapp.anilist.response.Query
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.media.Media

interface AniListQueries {
    suspend fun getMediaFullData(media: Media): Media
    suspend fun getGenre(): Query.GenreCollection?
    suspend fun getGenresAndTags(activity: Activity): Boolean
    suspend fun recentlyUpdated(
        smaller: Boolean = true,
        greater: Long = 0,
        lesser: Long = System.currentTimeMillis() / 1000 - 10000
    ): MutableList<Media>?

    suspend fun getGenres(genres: ArrayList<String>, listener: ((Pair<String, String>) -> Unit))
    suspend fun getGenreThumbnail(genre: String): Genre?
    suspend fun search(
        type: String,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: String? = null,
        genres: MutableList<String>? = null,
        tags: MutableList<String>? = null,
        format: String? = null,
        isAdult: Boolean = false,
        onList: Boolean? = null,
        excludedGenres: MutableList<String>? = null,
        excludedTags: MutableList<String>? = null,
        seasonYear: Int? = null,
        season: String? = null,
        id: Int? = null,
        hd: Boolean = false,
    ): SearchResults?



    suspend fun getMediaLists(
        anime: Boolean,
        userId: Int,
        sortOrder: String? = null
    ): MutableMap<String, ArrayList<Media>>

    suspend fun loadProfile(): Boolean
}