package com.animestudios.animeapp.parsers

import android.os.Build
import android.text.Html
import com.animestudios.animeapp.model.aniworld.AniWorldSearchResponseItem
import com.animestudios.animeapp.parsers.extractor.AniWorldExtractor
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

class AniWorldHind : AnimeParser() {
    override val name = "AniWorld"
    override val saveName = "aniworld_hind"
    override val hostUrl = "https://aniworld.to"
    override val malSyncBackupName = "aniworld"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> {
        val episodeList = ArrayList<Episode>()
        val doc: Document = getJsoup("${hostUrl}$animeLink", extra)

        val staffelLinks: Elements = doc.select("a[href*='staffel']")
        if (staffelLinks.isNotEmpty()) {
            val href = staffelLinks[0].attr("href")
            println("Href Sezon :${href}")

            val staffelDoc: Document = getJsoup("${hostUrl}${href}")

            val episodesForDoc = staffelDoc.select("tr[data-episode-id]")
            var count = 0
            for (episodeRow: Element in episodesForDoc) {
                count++
                val episodeLinkElement =
                    episodeRow.select("td.season1EpisodeID a[itemprop=url]").first()
                val episodeTitleElement = episodeRow.select("td.seasonEpisodeTitle a").first()
                val episodeLink: String = episodeLinkElement!!.attr("href")
                val episodeTitle: String = episodeTitleElement!!.text()


                episodeList.add(Episode(count.toString(), episodeLink, episodeTitle))
            }

            for (i in 0 until episodesForDoc.size) {
                if (i < episodeList.size) {
                    val description = episodesForDoc[i].select("span").text()
                    episodeList[i] = episodeList[i].copy(description = description)
                } else {
                    println("Warning: episodeList does not have an entry for index $i")
                }
            }
        } else {
            println("Hech qanday staffel link topilmadi.")
        }

        if (episodeList.isEmpty()) {
            println("Epizodlar ro'yxati bo'sh.")
        } else {
            println("Topilgan epizodlar soni: ${episodeList.size}")
        }

        return episodeList
    }

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        val serverList = ArrayList<VideoServer>()
        println("For Test: $episodeLink")
        val doc = getJsoup("$hostUrl$episodeLink")

        val episodeLinks = doc.select("li[data-link-target]")
        println("Total episode links found: ${episodeLinks.size}")

        // Check if episodeLinks is not empty
        if (episodeLinks.isNotEmpty()) {
            var count = 0
            for (link: Element in episodeLinks) {
                val redirectLink: String = link.attr("data-link-target") // Redirect link
                val fullLink = "$hostUrl$redirectLink" // Full URL
                val hostName: String = link.select("h4").text() // Hoster name
                count++
                // Print debug information
                println("FullLink: $fullLink")
                println("HosterName: $hostName")

                if (count < 5) {
                    serverList.add(VideoServer(hostName, fullLink))
                }
            }
        } else {
            println("Error: No episode links found in inSiteWebStream.")
        }
        return serverList

    }

    //Infinity loop fixed ithink
    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        return AniWorldExtractor(server)
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