package com.animestudios.animeapp.parsers

import android.net.Uri
import com.animestudios.animeapp.parsers.extractors.FPlayer
import com.animestudios.animeapp.parsers.extractors.GogoCDN
import com.animestudios.animeapp.parsers.extractors.StreamSB
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.client

class Gogo : AnimeParser() {
    override val name = "Gogo"
    override val saveName = "gogo_anime_hu"
    override val hostUrl = "https://anitaku.to"
    override val malSyncBackupName = "Gogoanime"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val list = mutableListOf<Episode>()

        val pageBody = client.get("$hostUrl/category/$animeLink").document
        val lastEpisode = pageBody.select("ul#episode_page > li:last-child > a").attr("ep_end").toString()
        val animeId = pageBody.select("input#movie_id").attr("value").toString()

        val epList = client
            .get("https://ajax.gogocdn.net/ajax/load-list-episode?ep_start=0&ep_end=$lastEpisode&id=$animeId").document
            .select("ul > li > a").reversed()
        epList.forEach {
            val num = it.select(".name").text().replace("EP", "").trim()
            list.add(Episode(num,hostUrl + it.attr("href").trim()))
        }

        return list
    }

    private fun httpsIfy(text: String): String {
        return if (text.take(2) == "//") "https:$text"
        else text
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Map<String,String>?): List<VideoServer> {
        val list = mutableListOf<VideoServer>()
        client.get(episodeLink).document.select("div.anime_muti_link > ul > li").forEach {
            val name = it.select("a").text().replace("Choose this server", "")
            val url = httpsIfy(it.select("a").attr("data-video"))
            val embed = FileUrl(url, mapOf("referer" to hostUrl))

            list.add(VideoServer(name,embed))
        }
        return list
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        val domain = Uri.parse(server.embed.url).host ?: return null
        val extractor: VideoExtractor? = when {
            "gogo" in domain    -> GogoCDN(server)
            "goload" in domain  -> GogoCDN(server)
            "playgo" in domain  -> GogoCDN(server)
            "anihdplay" in domain  -> GogoCDN(server)
            "taku" in domain  -> GogoCDN(server)
            "sb" in domain      -> StreamSB(server)
            "sss" in domain      -> StreamSB(server)
            "fplayer" in domain -> FPlayer(server)
            "fembed" in domain  -> FPlayer(server)
            else                -> null
        }
        return extractor
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val encoded = encode(query + if(selectDub) " (Dub)" else "")
        val list = mutableListOf<ShowResponse>()
        client.get("$hostUrl/search.html?keyword=$encoded").document
            .select(".last_episodes > ul > li div.img > a").forEach {
                val link = it.attr("href").toString().replace("/category/", "")
                val title = it.attr("title")
                val cover = it.select("img").attr("src")
                list.add(ShowResponse(title, link, cover))
            }
        return list
    }
}