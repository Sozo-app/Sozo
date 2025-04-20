package com.animestudios.animeapp.parsers

import android.util.Log
import com.animestudios.animeapp.tools.getJsoup

class AniWatch : AnimeParser() {
    override val hostUrl: String = "https://aniwatchtv.to/"
    override val saveName: String = "AniWatch"
    override val malSyncBackupName = "aniwatch"
    override val name: String = "AniWatch"
    override val isDubAvailableSeparately: Boolean = false
    override suspend fun loadEpisodes(
        animeLink: String, extra: Map<String, String>?
    ): List<Episode> {
        val document = getJsoup("$hostUrl$animeLink")
        val watchButtonHref = document.selectFirst(".film-buttons a.btn-play")?.attr("href") ?: ""
        val doc = getJsoup(
            "$hostUrl$watchButtonHref", mapOf(
                "Referer" to "$hostUrl$animeLink",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36"
            )
        )
        Log.d("GGG", "loadEpisodes: ${hostUrl}$watchButtonHref")
        //    adjust the selector to whatever container holds your <a> tags:
        val anchors = doc.select("div.episode-list a, ul.episodes li a, .pagination a")
        Log.d("GGG", "loadEpisodes:${
            anchors.map { a ->
                val epNum = a.text().trim()
                val href = a.absUrl("href")
                epNum to href
            }
        } ")
        return emptyList()
    }


    override suspend fun loadVideoServers(
        episodeLink: String, extra: Map<String, String>?
    ): List<VideoServer> {
        TODO("Not yet implemented")
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val animeList = mutableListOf<ShowResponse>()
        val request = getJsoup("$hostUrl/search?keyword=$query")
        val doc = request
        val elements = doc.select(".flw-item")

        for (element in elements) {
            val titleElement = element.selectFirst(".film-name a")
            val title = titleElement?.text() ?: continue
            val link = element.selectFirst("a.film-poster-ahref")?.attr("href") ?: continue

            val imageElement = element.selectFirst(".film-poster-img")
            val imageUrl = imageElement?.attr("data-src") ?: ""

            animeList.add(
                ShowResponse(
                    name = title,
                    link = link,
                    coverUrl = imageUrl,
                )
            )
        }
        return animeList
    }


}