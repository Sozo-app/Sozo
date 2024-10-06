package com.animestudios.animeapp.parsers

import android.os.Build
import android.text.Html
import com.animestudios.animeapp.model.aniworld.AniWorldSearchResponseItem
import com.animestudios.animeapp.tools.getJsoup
import com.animestudios.animeapp.tools.okHttpClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

class AniWorld : AnimeParser() {
    override val name = "AniWorld"
    override val saveName = "aniworld_ger"
    override val hostUrl = "https://aniworld.to"
    override val malSyncBackupName = "aniworld"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> {
        val episodeList = ArrayList<Episode>()
        val doc: Document = getJsoup("${hostUrl}$animeLink", extra)

// Barcha staffel linklarini olish
        val staffelLinks: Elements = doc.select("a[href*='staffel']")
        val staffelUrls = staffelLinks.map { it.attr("href") }.filter { it.isNotEmpty() }

        for (href in staffelUrls) {
            println("Href Sezon :${href}")

            // Har bir staffel uchun alohida so'rov yuborish
            val staffelDoc: Document = getJsoup("${hostUrl}${href}")

            // Epizodlarni olish
            val episodes: Elements = staffelDoc.select("ul:nth-of-type(2) li a")
            var count = 0
            for (episode: Element in episodes) {
                println("EPISODE DATT" + episode)
                val episodeTitle: String = episode.text() // Episod nomi
                if (isValidNumber(episodeTitle)) {
                    count++
                    val episodeLink: String = episode.absUrl("href")
                    println("EPISODE LINK ${episodeLink}")
                    println("EPISODE DATA :${episodeTitle}")
                    episodeList.add(Episode(count.toString(), episodeLink, episodeTitle))
                }
            }

        }

        return episodeList
    }

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        TODO("Not yet implemented")
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val list = ArrayList<ShowResponse>()
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "keyword",
                query,
            )
            .build()
        val requestBuilder = Request.Builder().post(requestBody).url(
            "${hostUrl}/ajax/search",
        ).build()
        val response = okHttpClient.newCall(requestBuilder).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            response.body?.string()
        }
        val gson = Gson()
        val listType = object : TypeToken<List<AniWorldSearchResponseItem>>() {}.type
        val animeList: List<AniWorldSearchResponseItem> = gson.fromJson(response, listType)
        animeList.onEach {
            list.add(ShowResponse(cleanUpString(it.title), it.link, ""))
        }
        return list
    }

    private fun cleanUpString(input: String): String {
        val noHtml = input.replace(Regex("<.*?>"), "")
        val decodedString = decodeUnicode(noHtml)
        return decodedString.trim()
    }

    private fun isValidNumber(input: String): Boolean {
        return input.toDoubleOrNull() != null
    }

    private fun decodeUnicode(input: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(input).toString()
        }
    }

}