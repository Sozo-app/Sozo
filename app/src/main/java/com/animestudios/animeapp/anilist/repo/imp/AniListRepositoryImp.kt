package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.imp.AniListQueriesImp
import com.animestudios.animeapp.anilist.repo.AniListRepository
import com.animestudios.animeapp.anilist.response.Query
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.anilist.response.SearchResultsCharacter
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.randomSelectFromList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AniListRepositoryImp() : AniListRepository {
    val api = AniListQueriesImp()
    private var currentPage = 1
    override fun getGenre() = flow<Result<Query.GenreCollection>> {
        val response = api.getGenre()
        if (response != null)
            emit(Result.success(response))
    }.flowOn(Dispatchers.IO)


    override fun getPopularBanner(
        i: Int
    ) = flow<Result<SearchResults>> {
        val (season, year) = Anilist.currentSeasons[i]
        val response = api.search(
            "ANIME",
            perPage = 14,
            sort = "Trending",
            hd = true,
            seasonYear = year,
            season = season,
        )
        if (response != null) {
            ((response.page)).also { currentPage = it }
            emit(Result.success(response))
        }

    }.flowOn(Dispatchers.IO)

    override suspend fun loadProfile(): kotlin.Boolean {
        return api.loadProfile()
    }

    override fun getFullDataById(id: Int) = flow<Media> {
        api.getMediaFullDataById(id)?.let {
            emit(it)
        }
    }

    override fun getFullDataById(media: Media): Flow<Media> = flow<Media> {
        val response = api.getMediaFullData(media)
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getAnimeListByGenre(genre: MutableList<String>) = flow<Result<SearchResults>> {
        val response =
            api.search(sort = "Trending", genres = genre, type = "ANIME", perPage = 30, hd = true)
        if (response != null) {
            emit(Result.success(response))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getGenres(
        genres: ArrayList<String>,
        listener: ((Pair<String, String>) -> Unit)
    ) {
        genres.forEach {
            this.api.getGenreThumbnail(it).apply {
                if (this != null) {
                    listener.invoke(it to this.thumbnail)
                }
            }
        }
    }

    override fun getSearch(r: SearchResults) = flow<Result<SearchResults>> {
        val response = api.search(
            r.type,
            r.page,
            r.perPage,
            r.search,
            r.sort,
            r.genres,
            r.tags,
            r.format,
            r.isAdult,
            r.onList,
            r.excludedGenres,
            r.excludedTags,
            r.seasonYear,
            r.season
        )
        if (response != null) {
            emit(Result.success(response))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSearchCharacter(search: SearchResultsCharacter) =
        flow<Result<SearchResultsCharacter>> {
            val response = api.searchCharacter(
                "ANIME",
                page = search.page,
                perPage = search.perPage,
                search = search.search
            )
            if (response != null) {
                emit(Result.success(response))
            }
        }.flowOn(Dispatchers.IO)

    override fun recommendedAnimeList() = flow<Result<MutableList<Media?>?>> {
        val response = api.recommendedAnime()
        if (response != null) {
            emit(Result.success(response))
        }
    }.flowOn(Dispatchers.IO)

    override fun recentlyUpdated() = flow<Result<MutableList<Media>?>> {
        val response = api.recentlyUpdated()
        if (response != null) {
            emit(Result.success(response))
        }
    }.flowOn(Dispatchers.IO)

    override fun forYouAnimeList() = flow<Result<MutableList<Media>>> {
        val response = api.search("ANIME", hd = true)
        if (response != null) {
            emit(Result.success(response!!.results))
        }
    }.flowOn(Dispatchers.IO)

    override fun randomAnimeList() = flow<Result<SearchResults>> {
        val response = api.search(
            genres = mutableListOf(randomSelectFromList(Anilist.genres!!)!!),
            hd = true,
            type = "ANIME"
        )
        if (response != null) emit(Result.success(response))
    }.flowOn(Dispatchers.IO)

    override suspend fun getMediaLists(
        anime: kotlin.Boolean,
        userId: Int,
        sortOrder: String?
    ): MutableMap<String, ArrayList<Media>> {
        val response = api.getMediaLists(anime, userId, sortOrder)
        return response
    }


}