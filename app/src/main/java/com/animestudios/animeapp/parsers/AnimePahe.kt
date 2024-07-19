package com.animestudios.animeapp.parsers

import com.animestudios.animeapp.parsers.*
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.tools.client
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue
import kotlin.math.pow

class       AnimePahe : AnimeParser() {

    override val hostUrl = "https://animepahe.ru"
    override val name = "AnimePahe"
    override val saveName = "animepahe_ru"
    override val isDubAvailableSeparately = false

    override suspend fun search(query: String): List<ShowResponse> {
        val resp = client.get("$hostUrl/api?m=search&q=${encode(query)}").parsed<SearchQuery>()
        return resp.data.map {
            val epLink = "$hostUrl/api?m=release&id=${it.session}&sort=episode_asc"
            ShowResponse(
                it.title,
                epLink,
                it.poster,
                extra = mapOf("t" to System.currentTimeMillis().toString())
            )
        }
    }

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> {
        val resp = client.get(animeLink).parsed<ReleaseRouteResponse>()
        val releaseId = animeLink.substringAfter("&id=").substringBefore("&sort")
        return (1 until resp.lastPage + 1).map { i ->
            val url = "$hostUrl/api?m=release&id=$releaseId&sort=episode_asc&page=$i"
            client.get(url).parsed<ReleaseRouteResponse>().data!!.map { ep ->
                val kwikEpLink = "$hostUrl/play/${releaseId}/${ep.session}"
                Episode(
                    ep.episode.toString().substringBefore(".0"),
                    kwikEpLink,
                    ep.title,
                    ep.snapshot
                )
            }
        }.flatten()
    }

    private val epRegex = Regex("(.+) · (.+)p \\((.+)MB\\) ?(.*)")
    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        return client.get(episodeLink).document.select("#pickDownload > a").map {
            val (subgroup, quality, mb, audio) = epRegex.find(it.text())?.destructured!!
            VideoServer(
                name = "$subgroup ${if (audio.isNotEmpty()) "($audio) " else ""}- ${quality}p",
                embedUrl = it.attr("href"),
                extraData = mapOf(
                    "size" to mb,
                    "referer" to hostUrl,
                    "quality" to quality
                )
            )
        }
    }

    //Responses get deprecated after a week, so we do not load them if they are half a week old
    override suspend fun loadSavedShowResponse(mediaId: Int): ShowResponse? {
        val loaded = readData<ShowResponse>("${saveName}_$mediaId")
        return if ((loaded?.extra?.get("t")?.toLongOrNull()
                ?.minus(System.currentTimeMillis())?.absoluteValue ?: 0) <= 302400000
        )
            loaded
        else null
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor =
        AnimePaheExtractor(server)

    class AnimePaheExtractor(override val server: VideoServer) : VideoExtractor() {

        private val data = server.extraData as Map<*, *>
        private val quality = data["quality"] as String
        private val size = (data["size"] as String).toDoubleOrNull()
        private val ref = data["referer"] as String

        private val redirectRegex = Regex("<a href=\"(.+?)\" .+?>Redirect me</a>")
        private val paramRegex = Regex("""\(\"(\w+)\",\d+,\"(\w+)\",(\d+),(\d+),(\d+)\)""")
        private val urlRegex = Regex("action=\"(.+?)\"")
        private val tokenRegex = Regex("value=\"(.+?)\"")

        override suspend fun extract(): VideoContainer {

            val resp = client.get(server.embed.url, referer = ref).text
            val kwikLink = redirectRegex.find(resp)?.groupValues?.get(1)!!

            val kwikRes = client.get(kwikLink)
            val cookies = kwikRes.headers.toMultimap()["set-cookie"]!![0]
            val (fullKey, key, v1, v2) = paramRegex.find(kwikRes.text)?.destructured!!

            val decrypted = decrypt(fullKey, key, v1.toInt(), v2.toInt())
            val postUrl = urlRegex.find(decrypted)?.groupValues?.get(1)!!
            val token = tokenRegex.find(decrypted)?.groupValues?.get(1)!!

            val mp4Url = client.post(
                postUrl,
                mapOf("referer" to kwikLink, "cookie" to cookies),
                data = mapOf("_token" to token),
                allowRedirects = false
            ).headers["location"]!!
            return VideoContainer(
                listOf(
                    Video(quality.toInt(), VideoType.CONTAINER, mp4Url, size)
                )
            )
        }

        private val map = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+/"

        private fun getString(content: String, s1: Int): Int {
            val s2 = 10
            val slice = map.substring(0, s2)
            var acc = 0
            content.reversed().forEachIndexed { index, c ->
                acc += (if (c.isDigit()) c.toString().toInt() else 0) * s1.toDouble().pow(index)
                    .toInt()
            }
            var k = ""
            while (acc > 0) {
                k = slice[acc % s2] + k
                acc = (acc - (acc % s2)) / s2
            }
            return k.toIntOrNull() ?: 0
        }

        private fun decrypt(fullKey: String, key: String, v1: Int, v2: Int): String {
            var r = ""
            var i = 0
            while (i < fullKey.length) {
                var s = ""
                while (fullKey[i] != key[v2]) {
                    s += fullKey[i]
                    i++
                }
                var j = 0
                while (j < key.length) {
                    s = s.replace(key[j].toString(), j.toString())
                    j++
                }
                r += (getString(s, v2) - v1).toChar()
                i++
            }
            return r
        }
    }

    @Serializable
    private data class SearchQuery(@SerialName("data") val data: List<SearchQueryData>) {

        @Serializable
        data class SearchQueryData(
            @SerialName("title") val title: String,
            @SerialName("poster") val poster: String,
            @SerialName("session") val session: String,
        )
    }

    @Serializable
    private data class ReleaseRouteResponse(
        @SerialName("last_page") val lastPage: Int,
        @SerialName("data") val data: List<ReleaseResponse>?
    ) {

        @Serializable
        data class ReleaseResponse(
            @SerialName("episode") val episode: Float,
            @SerialName("anime_id") val anime_id: Int,
            @SerialName("title") val title: String,
            @SerialName("snapshot") val snapshot: String,
            @SerialName("session") val session: String,
        )
    }

}
