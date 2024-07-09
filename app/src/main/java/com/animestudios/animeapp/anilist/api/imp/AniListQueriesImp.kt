package com.animestudios.animeapp.anilist.api.imp

import android.app.Activity
import android.util.Log
import androidx.annotation.Keep
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.api.AniListQueries
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.common.Anilist.executeQuery
import com.animestudios.animeapp.anilist.api.common.Anilist.userid
import com.animestudios.animeapp.anilist.response.*
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.media.Studio
import com.animestudios.animeapp.others.MalScraper
import com.animestudios.animeapp.tools.logError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

@Keep
class AniListQueriesImp constructor() : AniListQueries {
    override suspend fun getMediaFullData(media: Media): Media {
        val query =
            """{Media(id:${media.id}){id mediaListEntry{id status score(format:POINT_100) progress private notes repeat customLists updatedAt startedAt{year month day}completedAt{year month day}}isFavourite siteUrl idMal nextAiringEpisode{episode airingAt}source countryOfOrigin format duration season seasonYear startDate{year month day}endDate{year month day}genres studios(isMain:true){nodes{id name siteUrl}}description trailer { site id } synonyms tags { name rank isMediaSpoiler } characters(sort:[ROLE,FAVOURITES_DESC],perPage:25,page:1){edges{role node{id image{medium}name{userPreferred,native}}}}relations{edges{relationType(version:2)node{id idMal mediaListEntry{progress private score(format:POINT_100) status} episodes chapters nextAiringEpisode{episode} popularity meanScore isAdult isFavourite format title{english romaji userPreferred}type status(version:2)bannerImage coverImage{large,extraLarge}}}}staffPreview: staff(perPage: 8, sort: [RELEVANCE, ID]) {edges{role node{id name{userPreferred}}}}recommendations(sort:RATING_DESC){nodes{mediaRecommendation{id idMal mediaListEntry{progress private score(format:POINT_100) status} episodes chapters nextAiringEpisode{episode}meanScore isAdult isFavourite format title{english romaji userPreferred}type status(version:2)bannerImage coverImage{large}}}}externalLinks{url site}}}"""
        runBlocking {
            val anilist = async {
                var response = executeQuery<Query.Media>(query, force = true, show = true)
                if (response != null) {
                    fun parse() {
                        val fetchedMedia = response?.data?.media ?: return
                        println(response?.data?.media?.coverImage?.extraLarge)
                        media.source = fetchedMedia.source?.toString()
                        media.countryOfOrigin = fetchedMedia.countryOfOrigin
                        media.format = fetchedMedia.format?.toString()

                        media.startDate = fetchedMedia.startDate
                        media.endDate = fetchedMedia.endDate

                        if (fetchedMedia.genres != null) {
                            media.genres = arrayListOf()
                            fetchedMedia.genres?.forEach { i ->
                                media.genres.add(i)
                            }
                        }

                        media.trailer = fetchedMedia.trailer?.let { i ->
                            if (i.site != null && i.site.toString() == "youtube")
                                "https://www.youtube.com/embed/${i.id.toString().trim('"')}"
                            else null
                        }

                        fetchedMedia.synonyms?.apply {
                            media.synonyms = arrayListOf()
                            this.forEach { i ->
                                media.synonyms.add(
                                    i
                                )
                            }
                        }

                        fetchedMedia.tags?.apply {
                            media.tags = arrayListOf()
                            this.forEach { i ->
                                if (i.isMediaSpoiler == false)
                                    media.tags.add("${i.name} : ${i.rank.toString()}%")
                            }
                        }

                        media.description = fetchedMedia.description.toString()

                        if (fetchedMedia.characters != null) {
                            media.characters = arrayListOf()
                            fetchedMedia.characters?.edges?.forEach { i ->
                                i.node?.apply {
                                    media.characters?.add(
                                        com.animestudios.animeapp.media.Character(
                                            id = id,
                                            name = i.node?.name?.userPreferred,
                                            image = i.node?.image?.medium,
                                            banner = media.banner ?: media.cover,
                                            role = i.role.toString(),
                                            native = i.node?.name?.native
                                        )
                                    )
                                }
                            }
                        }
                        if (fetchedMedia.relations != null) {
                            media.relations = arrayListOf()
                            fetchedMedia.relations?.edges?.forEach { mediaEdge ->
                                val m = Media(mediaEdge)
                                media.relations?.add(m)
                                if (m.relation == "SEQUEL") {
                                    media.sequel =
                                        if ((media.sequel?.popularity ?: 0) < (m.popularity
                                                ?: 0)
                                        ) m else media.sequel

                                } else if (m.relation == "PREQUEL") {
                                    media.prequel =
                                        if ((media.prequel?.popularity ?: 0) < (m.popularity
                                                ?: 0)
                                        ) m else media.prequel
                                }
                            }
                            media.relations?.sortByDescending { it.popularity }
                            media.relations?.sortByDescending { it.startDate?.year }
                            media.relations?.sortBy { it.relation }
                        }
                        if (fetchedMedia.recommendations != null) {
                            media.recommendations = arrayListOf()
                            fetchedMedia.recommendations?.nodes?.forEach { i ->
                                i.mediaRecommendation?.apply {
                                    media.recommendations?.add(
                                        Media(this)
                                    )
                                }
                            }
                        }

                        if (fetchedMedia.mediaListEntry != null) {
                            fetchedMedia.mediaListEntry?.apply {
                                media.userProgress = progress
                                media.isListPrivate = private ?: false
                                media.notes = notes
                                media.userListId = id
                                media.userScore = score?.toInt() ?: 0
                                media.userStatus = status?.toString()
                                media.inCustomListsOf = customLists?.toMutableMap()
                                media.userRepeat = (repeat ?: 0) as Int
                                media.userUpdatedAt = updatedAt?.toString()?.toLong()?.times(1000)
                                media.userCompletedAt = completedAt ?: FuzzyDate()
                                media.userStartedAt = startedAt ?: FuzzyDate()
                            }
                        } else {
                            media.isListPrivate = false
                            media.userStatus = null
                            media.userListId = null
                            media.userProgress = null
                            media.userScore = 0
                            media.userRepeat = 0
                            media.userUpdatedAt = null
                            media.userCompletedAt = FuzzyDate()
                            media.userStartedAt = FuzzyDate()
                        }

                        if (media.anime != null) {
                            media.anime.episodeDuration = fetchedMedia.duration
                            media.anime.season = fetchedMedia.season?.toString()
                            media.anime.seasonYear = fetchedMedia.seasonYear

                            fetchedMedia.studios?.nodes?.apply {
                                if (isNotEmpty()) {
                                    val firstStudio = get(0)
                                    media.anime.mainStudio = Studio(
                                        firstStudio.id.toString(),
                                        firstStudio.name ?: "N/A"
                                    )
                                }
                            }

                            media.anime.nextAiringEpisodeTime =
                                fetchedMedia.nextAiringEpisode?.airingAt?.toLong()

                            fetchedMedia.externalLinks?.forEach { i ->
                                when (i.site.lowercase()) {
                                    "youtube" -> media.anime.youtube = i.url
                                    "crunchyroll" -> media.crunchySlug =
                                        i.url?.split("/")?.getOrNull(3)
                                    "vrv" -> media.vrvId = i.url?.split("/")?.getOrNull(4)
                                }
                            }
                        }
                        media.shareLink = fetchedMedia.siteUrl
                    }

                    if (response.data?.media != null) parse()
                    else {
                        snackString("Adult Stuff? ( ͡° ͜ʖ ͡°)")
                        response = executeQuery(query, force = true, useToken = false)
                        if (response?.data?.media != null) parse()
                        else snackString("What did you even open?")
                    }
                } else {
                    snackString("Error getting Data from Anilist.")
                }

            }
            val mal = async {
                if (media.idMAL != null) {
                    MalScraper.loadMedia(media)
                }
            }
            awaitAll(anilist, mal)
        }
        return media
    }

    override suspend fun getGenre(): Query.GenreCollection? {
        return executeQuery(
            """{GenreCollection}""",
            force = true,
            useToken = false
        )

    }

    override suspend fun getGenresAndTags(activity: Activity): Boolean {
        var genres: ArrayList<String>? =
            readData("genres_list", activity)
        var tags: Map<Boolean, List<String>>? =
            readData("tags_map", activity)

        if (genres == null) {
            executeQuery<Query.GenreCollection>(
                """{GenreCollection}""",
                force = true,
                useToken = false
            )?.data?.genreCollection?.apply {
                genres = arrayListOf()
                forEach {
                    genres?.add(it)
                }
                saveData("genres_list", genres!!)
            }
        }
        if (tags == null) {
            executeQuery<Query.MediaTagCollection>(
                """{ MediaTagCollection { name isAdult } }""",
                force = true
            )?.data?.mediaTagCollection?.apply {
                val adult = mutableListOf<String>()
                val good = mutableListOf<String>()
                forEach { node ->
                    if (node.isAdult == true) adult.add(node.name)
                    else good.add(node.name)
                }
                tags = mapOf(
                    true to adult,
                    false to good
                )
                saveData("tags_map", tags)
            }
        }
        return if (genres != null && tags != null) {
            Anilist.genres = genres
            Anilist.tags = tags
            true
        } else {
            snackString("Check Internet Your internet Bad ? ")
            return false
        }
    }

    override suspend fun recentlyUpdated(
        smaller: Boolean,
        greater: Long,
        lesser: Long
    ): MutableList<Media>? {
        suspend fun execute(page: Int = 1): Page? {
            val query = """{
Page(page:$page,perPage:50) {
    pageInfo {
        hasNextPage
        total
    }
    airingSchedules(
        airingAt_greater: $greater
        airingAt_lesser: $lesser
        sort:TIME_DESC
    ) {
        episode
        airingAt
        media {
            id
            idMal
            status
            chapters
            episodes
            nextAiringEpisode { episode }
            isAdult
            type
            meanScore
            isFavourite
            format
            bannerImage
            countryOfOrigin
            coverImage { large }
            title {
                english
                romaji
                userPreferred
            }
            mediaListEntry {
                progress
                private
                score(format: POINT_100)
                status
            }
        }
    }
}
        }""".replace("\n", " ").replace("""  """, "")
            return executeQuery<Query.Page>(query, force = true)?.data?.page
        }
        if (smaller) {
            val response = execute()?.airingSchedules ?: return null
            val idArr = mutableListOf<Int>()
            val listOnly = readData("recently_list_only") ?: false
            return response.mapNotNull { i ->
                i.media?.let {
                    if (!idArr.contains(it.id))
                        if (!listOnly && (it.countryOfOrigin == "JP" && (if (!Anilist.adult) it.isAdult == false else true)) || (listOnly && it.mediaListEntry != null)) {
                            idArr.add(it.id)
                            Media(it)
                        } else null
                    else null
                }
            }.toMutableList()
        } else {
            var i = 1
            val list = mutableListOf<Media>()
            var res: Page? = null
            suspend fun next() {
                res = execute(i)
                list.addAll(res?.airingSchedules?.mapNotNull { j ->
                    j.media?.let {
                        if (it.countryOfOrigin == "JP" && (if (!Anilist.adult) it.isAdult == false else true)) {
                            Media(it).apply { relation = "${j.episode},${j.airingAt}" }
                        } else null
                    }
                } ?: listOf())
            }
            next()
            while (res?.pageInfo?.hasNextPage == true) {
                next()
                i++
            }
            return list.reversed().toMutableList()
        }
    }

    override suspend fun getGenres(
        genres: ArrayList<String>,
        listener: ((Pair<String, String>) -> Unit)
    ) {
        genres.forEach {
            getGenreThumbnail(it).apply {
                if (this != null) {
                    listener.invoke(it to this.thumbnail)
                }
            }
        }
    }

    override suspend fun getGenreThumbnail(genre: String): Genre? {
        val genres = readData<MutableMap<String, Genre>>("genre_thumb") ?: mutableMapOf()
        if (genres.checkGenreTime(genre)) {
            try {
                val genreQuery =
                    """{ Page(perPage: 10){media(genre:"$genre", sort: TRENDING_DESC, type: ANIME, countryOfOrigin:"JP") {id bannerImage title{english romaji userPreferred} } } }"""
                executeQuery<Query.Page>(genreQuery, force = true)?.data?.page?.media?.forEach {
                    if (genres.checkId(it.id) && it.bannerImage != null) {
                        genres[genre] = Genre(
                            genre,
                            it.id,
                            it.bannerImage!!,
                            System.currentTimeMillis()
                        )
                        println("Is Thumbali:" + genres[genre]?.thumbnail)
                        println("Is Title:" + genres[genre]?.name)
                        println("Is Id:" + genres[genre]?.id)
                        saveData("genre_thumb", genres)
                        return genres[genre]
                    }
                }
            } catch (e: Exception) {
                logError(e)
            }
        } else {
            return genres[genre]
        }
        return null
    }


    override suspend fun search(
        type: String,
        page: Int?,
        perPage: Int?,
        search: String?,
        sort: String?,
        genres: MutableList<String>?,
        tags: MutableList<String>?,
        format: String?,
        isAdult: Boolean,
        onList: Boolean?,
        excludedGenres: MutableList<String>?,
        excludedTags: MutableList<String>?,
        seasonYear: Int?,
        season: String?,
        id: Int?,
        hd: Boolean,
    ): SearchResults? {
        val query = """
            query (${"$"}page: Int = 1, ${"$"}id: Int, ${"$"}type: MediaType, ${"$"}isAdult: Boolean = false, ${"$"}search: String, ${"$"}format: [MediaFormat], ${"$"}status: MediaStatus, ${"$"}countryOfOrigin: CountryCode, ${"$"}source: MediaSource, ${"$"}season: MediaSeason, ${"$"}seasonYear: Int, ${"$"}year: String, ${"$"}onList: Boolean, ${"$"}yearLesser: FuzzyDateInt, ${"$"}yearGreater: FuzzyDateInt, ${"$"}episodeLesser: Int, ${"$"}episodeGreater: Int, ${"$"}durationLesser: Int, ${"$"}durationGreater: Int, ${"$"}chapterLesser: Int, ${"$"}chapterGreater: Int, ${"$"}volumeLesser: Int, ${"$"}volumeGreater: Int, ${"$"}licensedBy: [String], ${"$"}isLicensed: Boolean, ${"$"}genres: [String], ${"$"}excludedGenres: [String], ${"$"}tags: [String], ${"$"}excludedTags: [String], ${"$"}minimumTagRank: Int, ${"$"}sort: [MediaSort] = [POPULARITY_DESC, SCORE_DESC]) {
      Page(page: ${"$"}page, perPage: ${perPage ?: 50}) {
    pageInfo {
      total
      perPage
      currentPage
      lastPage
      hasNextPage
    }
    media(id: ${"$"}id, type: ${"$"}type, season: ${"$"}season, format_in: ${"$"}format, status: ${"$"}status, countryOfOrigin: ${"$"}countryOfOrigin, source: ${"$"}source, search: ${"$"}search, onList: ${"$"}onList, seasonYear: ${"$"}seasonYear, startDate_like: ${"$"}year, startDate_lesser: ${"$"}yearLesser, startDate_greater: ${"$"}yearGreater, episodes_lesser: ${"$"}episodeLesser, episodes_greater: ${"$"}episodeGreater, duration_lesser: ${"$"}durationLesser, duration_greater: ${"$"}durationGreater, chapters_lesser: ${"$"}chapterLesser, chapters_greater: ${"$"}chapterGreater, volumes_lesser: ${"$"}volumeLesser, volumes_greater: ${"$"}volumeGreater, licensedBy_in: ${"$"}licensedBy, isLicensed: ${"$"}isLicensed, genre_in: ${"$"}genres, genre_not_in: ${"$"}excludedGenres, tag_in: ${"$"}tags, tag_not_in: ${"$"}excludedTags, minimumTagRank: ${"$"}minimumTagRank, sort: ${"$"}sort, isAdult: ${"$"}isAdult) {
      id
      idMal
      isAdult
      status
      chapters
      episodes
      nextAiringEpisode {
        episode
      }
      type
      genres
      meanScore
      isFavourite
      format
      bannerImage
      coverImage {
        large
        extraLarge
      }
      title {
        english
        romaji
        userPreferred
      }
      mediaListEntry {
        progress
        private
        score(format: POINT_100)
        status
      }
 }
}
}
        """.replace("\n", " ").replace("""  """, "")
        val variables = """{"type":"$type","isAdult":$isAdult
            ${if (onList != null) ""","onList":$onList""" else ""}
            ${if (page != null) ""","page":"$page"""" else ""}
            ${if (id != null) ""","id":"$id"""" else ""}
            ${if (seasonYear != null) ""","seasonYear":"$seasonYear"""" else ""}
            ${if (season != null) ""","season":"$season"""" else ""}
            ${if (search != null) ""","search":"$search"""" else ""}
            ${if (Anilist.sortBy.containsKey(sort)) ""","sort":"${Anilist.sortBy[sort]}"""" else ""}
            ${if (format != null) ""","format":"${format.replace(" ", "_")}"""" else ""}
            ${if (genres?.isNotEmpty() == true) ""","genres":[${genres.joinToString { "\"$it\"" }}]""" else ""}
            ${
            if (excludedGenres?.isNotEmpty() == true)
                ""","excludedGenres":[${
                    excludedGenres.joinToString {
                        "\"${
                            it.replace(
                                "Not ",
                                ""
                            )
                        }\""
                    }
                }]"""
            else ""
        }
            ${if (tags?.isNotEmpty() == true) ""","tags":[${tags.joinToString { "\"$it\"" }}]""" else ""}
            ${
            if (excludedTags?.isNotEmpty() == true)
                ""","excludedTags":[${
                    excludedTags.joinToString {
                        "\"${
                            it.replace(
                                "Not ",
                                ""
                            )
                        }\""
                    }
                }]"""
            else ""
        }
            }""".replace("\n", " ").replace("""  """, "")

        val response = executeQuery<Query.Page>(query, variables, true)?.data?.page
        if (response?.media != null) {
            val responseArray = arrayListOf<Media>()
            response.media?.forEach { i ->
                val userStatus = i.mediaListEntry?.status.toString()
                val genresArr = arrayListOf<String>()
                if (i.genres != null) {
                    i.genres?.forEach { genre ->
                        genresArr.add(genre)
                    }
                }
                val media = Media(i)
                if (!hd) media.cover = i.coverImage?.large
                media.relation = if (onList == true) userStatus else null
                media.genres = genresArr
                responseArray.add(media)
            }

            val pageInfo = response.pageInfo ?: return null

            return com.animestudios.animeapp.anilist.response.SearchResults(
                type = type,
                perPage = perPage,
                search = search,
                sort = sort,
                isAdult = isAdult,

                onList = onList,
                genres = genres,
                excludedGenres = excludedGenres,
                tags = tags,
                excludedTags = excludedTags,
                format = format,
                seasonYear = seasonYear,
                season = season,
                results = responseArray,
                page = pageInfo.currentPage.toString().toIntOrNull() ?: 0,
                hasNextPage = pageInfo.hasNextPage == true,
            )
        } else snackString("Check Internet Your internet Bad ?")
        return null
    }

    override suspend fun searchCharacter(
        type: String,
        id: Int?,
        page: Int?,
        perPage: Int?,
        search: String?
    ): SearchResultsCharacter? {
        val query = """
            query (${"$"}page: Int = 1, ${"$"}id: Int ${"$"}search: String) {
      Page(page: ${"$"}page, perPage: ${perPage ?: 50}) {
    pageInfo {
      total
      perPage
      currentPage
      lastPage
      hasNextPage
    }
    characters(id: ${"$"}id, search: ${"$"}search) {
      id
      name {
        first
        last
        full
        native
        userPreferred
      }
      image {
        large
        medium
      }
      description
      dateOfBirth {
        year
        month
        day
      }
      
      gender
      bloodType
      age
} } } 
        """.replace("\n", " ").replace("""  """, "")
        val variables = """{
            ${if (id != null) """"id":"$id"""" else ""}
            ${if (search != null) ""","search":"$search"""" else ""}
                                }
                                
            }""".replace("\n", " ").replace("""  """, "")
        val response = executeQuery<Query.Page>(query, variables, true)?.data?.page
        if (response?.characters != null) {
            val responseArray = arrayListOf<Character>()
            response.characters?.forEach { i ->
                responseArray.add(i)
            }
            val pageInfo = response.pageInfo ?: return null

            return SearchResultsCharacter(
                perPage = perPage,
                search = search,
                results = responseArray,
                page = pageInfo.currentPage.toString().toIntOrNull() ?: 0,
                hasNextPage = pageInfo.hasNextPage == true,
            )
        } else snackString("Check Internet Your internet Bad ?")

        return null
    }

    override suspend fun recommendedAnime(): MutableList<Media?>? {
        val query = """query Recommendded { Page(page:1,perPage:40){
                 recommendations(sort:RATING_DESC){
      media{
       id
      idMal
      isAdult
      status
      chapters
      episodes
      nextAiringEpisode {
        episode
      }
      type
      genres
      meanScore
      isFavourite
      format
      bannerImage
      coverImage {
        large
        extraLarge
      }
      title {
        english
        romaji
        userPreferred
      }
      mediaListEntry {
        progress
        private
        score(format: POINT_100)
        status
      }
    }
              
                }
               }
             }""".replace("\n", " ").replace("""  """, "")

        val response = executeQuery<Query.Page>(query)?.data?.page
        if (response?.recommendations != null) {
            val responseArray = arrayListOf<Media?>()
            response.recommendations?.forEach { i ->
                val genresArr = arrayListOf<String>()
                if (i.media?.genres != null) {
                    i.media?.genres?.forEach { genre ->
                        genresArr.add(genre)
                    }
                }
                val media = Media(i.media!!)
                media.genres = genresArr
                media.cover = i.media?.coverImage?.large
                media.relation =  null
                responseArray.add(media)
            }
            return responseArray
        } else snackString("Check Internet Your internet Bad ?")
        return null
    }


    override suspend fun getMediaLists(
        animes: Boolean,
        userId: Int,
        sortOrder: String?
    ): MutableMap<String, ArrayList<Media>> {
        Log.e("TAG", "getMediaLists: ${userid}")

        val anime = true
        val response =
            executeQuery<Query.MediaListCollection>("""{ MediaListCollection(userId: $userId, type: ${if (anime) "ANIME" else "MANGA"}) { lists { name isCustomList entries { status progress private score (format:POINT_100) updatedAt media { id idMal isAdult type status chapters episodes nextAiringEpisode { episode } bannerImage meanScore isFavourite coverImage { large } startDate { year month day } title { english romaji userPreferred } } } } user { id mediaListOptions { rowOrder animeList { sectionOrder } mangaList { sectionOrder } } } } }""")
        val sorted = mutableMapOf<String, ArrayList<Media>>()
        val unsorted = mutableMapOf<String, ArrayList<Media>>()
        val all = arrayListOf<Media>()
        val allIds = arrayListOf<Int>()

        println(response?.data?.mediaListCollection?.lists)
        response?.data?.mediaListCollection?.lists?.forEach { i ->
            val name = i.name.toString().trim('"')
            unsorted[name] = arrayListOf()
            i.entries?.forEach {
                val a = Media(it)
                unsorted[name]?.add(a)
                if (!allIds.contains(a.id)) {
                    allIds.add(a.id)
                    all.add(a)
                }
            }
        }

        val options = response?.data?.mediaListCollection?.user?.mediaListOptions
        val mediaList = if (anime) options?.animeList else options?.mangaList
        mediaList?.sectionOrder?.forEach {
            if (unsorted.containsKey(it)) sorted[it] = unsorted[it]!!
        }
        unsorted.forEach {
            if (!sorted.containsKey(it.key)) sorted[it.key] = it.value
        }

        sorted["Favourites"] = favMedia(anime)
        sorted["Favourites"]?.sortWith(compareBy { it.userFavOrder })

        sorted["All"] = all

        val sort = sortOrder ?: options?.rowOrder
        for (i in sorted.keys) {
            when (sort) {
                "score" -> sorted[i]?.sortWith { b, a ->
                    compareValuesBy(
                        a,
                        b,
                        { it.userScore },
                        { it.meanScore })
                }
                "title" -> sorted[i]?.sortWith(compareBy { it.userPreferredName })
                "updatedAt" -> sorted[i]?.sortWith(compareByDescending { it.userUpdatedAt })
                "release" -> sorted[i]?.sortWith(compareByDescending { it.startDate })
                "id" -> sorted[i]?.sortWith(compareBy { it.id })
            }
        }

        println(sorted)
        return sorted
    }

    suspend fun favMedia(anime: Boolean): ArrayList<Media> {
        var hasNextPage = true
        var page = 0

        suspend fun getNextPage(page: Int): List<Media> {
            val response =
                executeQuery<Query.User>(
                    """{
            User(id:${Anilist.userid}){
            id favourites {
                ${if (anime) "anime" else "manga"}(
                page:$page){
                pageInfo { hasNextPage } edges {
                    favouriteOrder node {
                        id idMal isAdult mediaListEntry {
                            progress private score(
                                format:POINT_100
                            ) status
                        } chapters isFavourite episodes nextAiringEpisode { episode } meanScore isFavourite startDate { year month day } title { english romaji userPreferred } type status(
                            version:2)bannerImage coverImage{ large }
                    }
                }
            }
            }
        }
        }"""
                )
            val favourites = response?.data?.user?.favourites
            val apiMediaList = if (anime) favourites?.anime else favourites?.manga
            hasNextPage = apiMediaList?.pageInfo?.hasNextPage ?: false
            return apiMediaList?.edges?.mapNotNull {
                it.node?.let { i ->
                    Media(i).apply { isFav = true }
                }
            } ?: return listOf()
        }

        val responseArray = arrayListOf<Media>()
        while (hasNextPage) {
            page++
            responseArray.addAll(getNextPage(page))
        }
        return responseArray
    }

    override suspend fun loadProfile(): Boolean {
        val response: Query.Viewer?
        measureTimeMillis {
            response =
                executeQuery("""{ Viewer { name options { displayAdultContent } avatar { medium } bannerImage id mediaListOptions { rowOrder animeList { sectionOrder customLists } mangaList { sectionOrder customLists } } statistics { anime { episodesWatched } manga { chaptersRead } } } }""")
        }.also { println("time : $it") }
        val user = response?.data?.user ?: return false
        Anilist.userid = user.id
        Anilist.username = user.name
        Anilist.bg = user.bannerImage
        Anilist.avatar = user.avatar?.medium
        Anilist.episodesWatched = user.statistics?.anime?.episodesWatched
        Anilist.adult = user.options?.displayAdultContent ?: false
        Log.d("TAG", "loadProfile: ${user.id}")
        Log.d("TAG", "loadToke: ${Anilist.token}")
        Log.d("TAG", "loadToke: ${Anilist.token2}")
        return true
    }

    suspend fun getCharacterDetails(character: com.animestudios.animeapp.media.Character): com.animestudios.animeapp.media.Character {
        val query = """ {
  Character(id: ${character.id}) {
    id
    age
    gender
    description
    dateOfBirth {
      year
      month
      day
    }
    media(page: 0,sort:[POPULARITY_DESC,SCORE_DESC]) {
      pageInfo {
        total
        perPage
        currentPage
        lastPage
        hasNextPage
      }
      edges {
        id
        characterRole
        node {
          id
          idMal
          isAdult
          status
          chapters
          episodes
          nextAiringEpisode { episode }
          type
          meanScore
          isFavourite
          bannerImage
          countryOfOrigin
          coverImage { large }
          title {
              english
              romaji
              userPreferred
          }
          mediaListEntry {
              progress
              private
              score(format: POINT_100)
              status
          }
        }
      }
    }
  }
}""".replace("\n", " ").replace("""  """, "")
        executeQuery<Query.Character>(query, force = true)?.data?.character?.apply {
            character.age = age
            character.gender = gender
            character.description = description
            character.dateOfBirth = dateOfBirth
            character.roles = arrayListOf()
            media?.edges?.forEach { i ->
                val m = Media(i)
                m.relation = i.characterRole.toString()
                character.roles?.add(m)
            }
        }
        return character
    }

    suspend fun getStudioDetails(studio: Studio): Studio {
        fun query(page: Int = 0) = """ {
  Studio(id: ${studio.id}) {
    id
    media(page: $page,sort:START_DATE_DESC) {
      pageInfo{
        hasNextPage
      }
      edges {
        id
        node {
          id
          idMal
          isAdult
          status
          chapters
          episodes
          nextAiringEpisode { episode }
          type
          meanScore
          startDate{ year }
          isFavourite
          bannerImage
          countryOfOrigin
          coverImage { large }
          title {
              english
              romaji
              userPreferred
          }
          mediaListEntry {
              progress
              private
              score(format: POINT_100)
              status
          }
        }
      }
    }
  }
}""".replace("\n", " ").replace("""  """, "")

        var hasNextPage = true
        val yearMedia = mutableMapOf<String, ArrayList<Media>>()
        var page = 0
        while (hasNextPage) {
            page++
            hasNextPage = executeQuery<Query.Studio>(query(page), force = true)?.data?.studio?.media?.let {
                it.edges?.forEach { i ->
                    i.node?.apply {
                        val status = status.toString()
                        val year = startDate?.year?.toString() ?: "TBA"
                        val title = if (status != "CANCELLED") year else status
                        if (!yearMedia.containsKey(title))
                            yearMedia[title] = arrayListOf()
                        yearMedia[title]?.add(Media(this))
                    }
                }
                it.pageInfo?.hasNextPage == true
            } ?: false
        }

        if (yearMedia.contains("CANCELLED")) {
            val a = yearMedia["CANCELLED"]!!
            yearMedia.remove("CANCELLED")
            yearMedia["CANCELLED"] = a
        }
        studio.yearMedia = yearMedia
        return studio
    }
}