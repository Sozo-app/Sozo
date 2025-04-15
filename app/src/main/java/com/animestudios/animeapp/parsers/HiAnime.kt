package com.animestudios.animeapp.parsers

import com.animestudios.animeapp.parsers.extractor.HiAnimeExtractor
import com.animestudios.animeapp.tools.getJsoup
import org.jsoup.nodes.Document

class HiAnime : AnimeParser() {
    override val name = "HiAnime"
    override val saveName = "hi_anime"
    override val hostUrl = "https://hianimez.to/"
    override val malSyncBackupName = "hianime"
    override val isDubAvailableSeparately = true
    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> {
        val url = if (animeLink.startsWith("http")) animeLink else "$hostUrl/$animeLink"
        val doc: Document = getJsoup(url)
        val osItems = doc.select("div.other-season div.os-list a.os-item")
        val episodes = mutableListOf<Episode>()

        for (osItem in osItems) {
            val link = osItem.attr("href").let {
                if (it.startsWith("http")) it else "$hostUrl$it"
            }

            val fullTitle = osItem.selectFirst("div.title")?.text() ?: ""
            val regex = Regex("Movie\\s*(\\d+):\\s*(.+)")
            val matchResult = regex.find(fullTitle)
            val number = matchResult?.groups?.get(1)?.value ?: fullTitle
            val title = matchResult?.groups?.get(2)?.value ?: fullTitle

            val styleAttr = osItem.selectFirst("div.season-poster")?.attr("style") ?: ""
            val thumbRegex = Regex("url\\((.*?)\\)")
            val thumbMatch = thumbRegex.find(styleAttr)
            val thumbUrl = thumbMatch?.groups?.get(1)?.value ?: ""

            episodes.add(
                Episode(
                    number,
                    link,
                    title,
                    thumbUrl,
                    description = null,
                    isFiller = false,
                    extra = extra
                )
            )
        }
        return episodes
    }

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        val epDoc = getJsoup("$hostUrl$episodeLink")
        val servers = mutableListOf<VideoServer>()
        val anchorElement =
            epDoc.selectFirst("div.film-buttons a.btn.btn-radius.btn-primary.btn-play")
        val href = anchorElement?.attr("href") ?: ""
        servers.add(
            VideoServer(
                "HiAnimeZ",
                href
            )
        )
        return servers
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        return HiAnimeExtractor(server)
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val hiAnimeRequest = getJsoup("https://hianimez.to/search?keyword=${query}")
        val doc: Document = hiAnimeRequest

        val shows = mutableListOf<ShowResponse>()

        val items = doc.select("div.flw-item")
        for (item in items) {
            val filmPoster = item.selectFirst("div.film-poster")
            val imgElement = filmPoster?.selectFirst("img.film-poster-img")
            val coverUrl = imgElement?.attr("data-src") ?: ""

            val linkElement = filmPoster?.selectFirst("a.film-poster-ahref")
            val link = linkElement?.attr("href") ?: ""

            val filmDetail = item.selectFirst("div.film-detail")
            val nameElement = filmDetail?.selectFirst("h3.film-name a")
            val name = nameElement?.text() ?: ""

            shows.add(ShowResponse(name = name, link = link, coverUrl = coverUrl))
        }

        shows.forEach { println(it) }
        return shows
    }

}