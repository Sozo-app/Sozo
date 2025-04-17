package com.animestudios.animeapp.anilist.api.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.animestudios.animeapp.anilist.api.imp.AniListQueriesImp
import com.animestudios.animeapp.anilist.response.Query
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.tools.defaultHeaders
import com.animestudios.animeapp.tools.tryWithSuspend
import com.sozo.nicehttp.Requests
import okhttp3.OkHttpClient
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object Anilist {
    const val BASE_URL = "https://graphql.anilist.co/"
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    val client = Requests(
        okHttpClient,
        defaultHeaders,
        defaultCacheTime = 6,
        defaultCacheTimeUnit = TimeUnit.HOURS,
    )
    var notFoundImg =
        "https://img.freepik.com/premium-vector/productive-woman-anime-error-404-page-found_150972-549.jpg"
    var animePlayId = 21 //Def =21
    var titlePlay = "One-Piece" //Def =One-Piece
    var genres: ArrayList<String>? = null
    var tags: Map<Boolean, List<String>>? = null
    var selected = 0
    var token: String? = null
    var token2: String? = null
    var token3: String? = null

    var username: String? = null
    var userid: Int? = null
    var avatar: String? = null
    var bg: String? = null
    var episodesWatched: Int? = null

    var adult: Boolean = false


    val aniListQueries = AniListQueriesImp()
    suspend fun getMedia(id: Int, mal: Boolean = false): Media? {
        val response = executeQuery<Query.Media>(
            """{Media(${if (!mal) "id:" else "idMal:"}$id){id idMal status chapters episodes nextAiringEpisode{episode}type meanScore isAdult isFavourite bannerImage coverImage{large}title{english romaji userPreferred}mediaListEntry{progress private score(format:POINT_100)status}}}""",
            force = true
        )
        val fetchedMedia = response?.data?.media ?: return null
        return Media(fetchedMedia)
    }


    suspend inline fun <reified T : Any> executeQuery(
        query: String,
        variables: String = "",
        force: Boolean = false,
        useToken: Boolean = true,
        show: Boolean = false,
        cache: Int? = null
    ): T? {
        return tryWithSuspend {
            val data = mapOf(
                "query" to query,
                "variables" to variables
            )
            val headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            )


            if (token != null || force) {
                if (token != null && useToken) {
                    val selectedType = readData<Int>("selectedAccount") ?: 1

                    when (selectedType) {
                        1 -> {
                            selected = 1
                            headers["Authorization"] = "Bearer $token"

                        }
                        2 -> {
                            headers["Authorization"] = "Bearer $token2"

                        }
                        3 -> {
                            headers["Authorization"] = "Bearer $token3"

                        }
                    }
                }
                val json = client.post(
                    "https://graphql.anilist.co/",
                    headers,
                    data = data,
                    cacheTime = cache ?: 10
                )
                if (!json.text.startsWith("{")) throw Exception("Seems like Anilist is down, maybe try using a VPN or you can wait for it to comeback.")
                json.parsed()
            } else null

        }

    }

    fun getSavedToken(context: Context, type: Int = 1, byType: Boolean = false): Boolean {

        if (byType) {
            when (type) {
                1 -> {
                    if ("anilistToken" in context.fileList()) {
                        token = File(context.filesDir, "anilistToken").readText()
                        saveData("selectedAccount", 1)
                        return true
                    }

                }
                2 -> {

                    if ("anilistToken2" in context.fileList()) {
                        token2 = File(context.filesDir, "anilistToken2").readText()
                        saveData("selectedAccount", 2)
                        return true
                    }
                }
                3 -> {
                    if ("anilistToken3" in context.fileList()) {
                        token3 = File(context.filesDir, "anilistToken3").readText()
                        saveData("selectedAccount", 3)
                        return true
                    }
                }
            }
        } else {
            val selectedAccountType = readData<Int>("selectedAccount") ?: 1

            println(selectedAccountType)

            when (selectedAccountType) {
                1 -> {

                    if ("anilistToken" in context.fileList()) {
                        token = File(context.filesDir, "anilistToken").readText()
                        return true
                    }
                }
                2 -> {

                    if ("anilistToken2" in context.fileList()) {
                        token2 = File(context.filesDir, "anilistToken2").readText()
                        return true
                    }
                }
                3 -> {
                    if ("anilistToken3" in context.fileList()) {
                        token3 = File(context.filesDir, "anilistToken3").readText()
                        return true
                    }
                }
            }
        }

        return false
    }

    fun loginIntent(context: Context) {
        val clientID = 14066

        try {

            val CHROME_PACKAGE_NAME = "com.android.chrome"

            val intent = CustomTabsIntent.Builder().build()

            intent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.intent.setPackage(CHROME_PACKAGE_NAME);
            intent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.launchUrl(
                context,
                Uri.parse("https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token")
            )

        } catch (e: ActivityNotFoundException) {
            println("GGG" + e.message + "GGG")
        }
    }

    val years = (1970 until 2024).map { it }.reversed().toMutableList()


    val sortBy = mapOf(
        "Score" to "SCORE_DESC",
        "Popular" to "POPULARITY_DESC",
        "Trending" to "TRENDING_DESC",
        "A-Z" to "TITLE_ENGLISH",
        "Z-A" to "TITLE_ENGLISH_DESC",
        "What?" to "SCORE",
    )

    val seasons = listOf(
        "WINTER", "SPRING", "SUMMER", "FALL"
    )

    val anime_formats = listOf(
        "TV", "TV SHORT", "MOVIE", "SPECIAL", "OVA", "ONA", "MUSIC"
    )

    val manga_formats = listOf(
        "MANGA", "NOVEL", "ONE SHOT"
    )

    val authorRoles = listOf(
        "Original Creator", "Story & Art", "Story"
    )

    private val cal: Calendar = Calendar.getInstance()
    private val currentYear = cal.get(Calendar.YEAR)

    private val currentSeason: Int = when (cal.get(Calendar.MONTH)) {
        0, 1, 2 -> 0
        3, 4, 5 -> 1
        6, 7, 8 -> 2
        9, 10, 11 -> 3
        else -> 0
    }

    fun removeSavedToken(context: Context) {
        token = null
        username = null
        adult = false
        userid = null
        avatar = null
        bg = null
        episodesWatched = null
        if ("anilistToken" in context.fileList()) {
            File(context.filesDir, "anilistToken").delete()
        }
    }


    val currentSeasons = listOf(
        getSeason(false),
        seasons[currentSeason] to currentYear,
        getSeason(true)
    )


    private fun getSeason(next: Boolean): Pair<String, Int> {
        var newSeason = if (next) currentSeason + 1 else currentSeason - 1
        var newYear = currentYear
        if (newSeason > 3) {
            newSeason = 0
            newYear++
        } else if (newSeason < 0) {
            newSeason = 3
            newYear--
        }
        return seasons[newSeason] to newYear
    }


}
