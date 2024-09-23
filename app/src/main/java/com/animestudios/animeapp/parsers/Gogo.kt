package com.animestudios.animeapp.parsers

import android.net.Uri
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.client
import com.animestudios.animeapp.tools.getJsoup
import com.animestudios.animeapp.utils.JsUnpacker

class Gogo : AnimeParser() {
    override val name = "Gogo"
    override val saveName = "gogo_anime_hu"
    override val hostUrl = "https://anitaku.pe"
    override val malSyncBackupName = "Gogoanime"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> {
        val list = mutableListOf<Episode>()

        val pageBody = client.get("$hostUrl/category/$animeLink").document
        val lastEpisode =
            pageBody.select("ul#episode_page > li:last-child > a").attr("ep_end").toString()
        val animeId = pageBody.select("input#movie_id").attr("value").toString()

        println("ANME ID :${animeId}")
        val epList = client
            .get("https://ajax.gogocdn.net/ajax/load-list-episode?ep_start=0&ep_end=$lastEpisode&id=$animeId").document
            .select("ul > li > a").reversed()
        epList.forEach {
            val num = it.select(".name").text().replace("EP", "").trim()
            list.add(Episode(num, hostUrl + it.attr("href").trim()))
        }

        return list
    }

    private fun httpsIfy(text: String): String {
        return if (text.take(2) == "//") "https:$text"
        else text
    }

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        val list = mutableListOf<VideoServer>()
        getJsoup(episodeLink).select("div.anime_muti_link > ul > li").forEach {

            val name = it.select("a").text().replace("Choose this server", "")
            val url = httpsIfy(it.select("a").attr("data-video"))
            val embed = FileUrl(url, mapOf("referer" to hostUrl))
            val domain = Uri.parse(embed.url).host!!
            println("DOMAIN L::: :::   : :::: " + domain)
            println("DOMAIN L::: :::   : :::: " + url)
            if ("awish" in domain) {
                list.add(VideoServer(name, embed))
            }

        }
        return list
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        val domain = Uri.parse(server.embed.url).host ?: return null
        println(server.extraData.toString())

        if ("awish" in domain) {
            println(server.embed.url)
            println(server.extraData.toString())
        }
        val extractor: VideoExtractor? = when {
            "awish" in domain -> GogoMpUploadExtractor(server)
            else -> null
        }
        return extractor
    }

    class GogoMpUploadExtractor(override val server: VideoServer) : VideoExtractor() {

        override suspend fun extract(): VideoContainer {
            val scrapVideos = mutableListOf<Video>()

            val list = mutableListOf<Video>()
            val url = server.embed.url
            val host = server.embed.headers["referer"]
            val document = getJsoup(url, server.extraData)
            // Find the eval function in the script
            val scriptElements = document.select("script")
            println(scriptElements)
            var evalContent: String? = null
            for (script in scriptElements) {
                val scriptData = script.data()
                if (scriptData.contains("eval(function(p,a,c,k,e,d){")) {
                    evalContent = scriptData
                    println(evalContent)

                    break
                }
            }

            val urlM3u8 =extractFileUrl(getAndUnpack(evalContent.toString()))?:""

            scrapVideos.add(Video(null, VideoType.M3U8, urlM3u8))

            return VideoContainer(scrapVideos)
        }
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val encoded = encode(query + if (selectDub) " (Dub)" else "")
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

private val packedRegex = Regex("""eval\(function\(p,a,c,k,e,.*\)\)""")
fun getPacked(string: String): String? {
    return packedRegex.find(string)?.value
}

fun getAndUnpack(string: String): String {
    val packedText = getPacked(string)
    return JsUnpacker(packedText).unpack() ?: string
}

fun extractFileUrl(input: String): String? {
    val regex = Regex("""file:"(https://[^\s"]+)"""")
    val matchResult = regex.find(input)
    return matchResult?.groups?.get(1)?.value
}