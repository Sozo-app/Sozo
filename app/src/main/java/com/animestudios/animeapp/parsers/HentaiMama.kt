package com.animestudios.animeapp.parsers


import com.animestudios.animeapp.findBetween
import com.animestudios.animeapp.getSize
import com.animestudios.animeapp.tools.Mapper
import com.animestudios.animeapp.tools.client
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class HentaiMama : AnimeParser() {
    override val name = "Hentaimama"
    override val saveName = "hentai_mama"
    override val hostUrl = "https://hentaimama.io"
    override val isDubAvailableSeparately = false
    override val isNSFW = true

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val pageBody = client.get(animeLink).document
        return pageBody.select("div#episodes.sbox.fixidtab div.module.series div.content.series div.items article").reversed()
            .map {
                val epNum = it.select("div.data h3").text().replace("Episode", "")
                val url = it.select("div.poster div.season_m.animation-3 a").attr("href")
                val thumb1 = it.select("div.poster img").attr("data-src")
                Episode(epNum, url, thumbnail = thumb1)
            }
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Map<String,String>?): List<VideoServer> {
        val animeId = client.get(episodeLink).document.select("#post_report > input:nth-child(5)").attr("value")
        val json = Mapper.parse<List<String>>(
            client.post(
                "https://hentaimama.io/wp-admin/admin-ajax.php", data = mapOf(
                    "action" to "get_player_contents",
                    "a" to animeId
                )
            ).text
        )
        return json.mapIndexed { i, it ->
            val url = it.substringAfter("src=\"").substringBefore("\"")
            VideoServer("Mirror $i", url)
        }

    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor = HentaiMamaExtractor(server)

    class HentaiMamaExtractor(override val server: VideoServer) : VideoExtractor() {

        override suspend fun extract(): VideoContainer {
            val doc = client.get(server.embed.url)

            doc.document.selectFirst("video>source")?.attr("src")?.apply {
                return VideoContainer(listOf(Video(null, VideoType.CONTAINER, this, getSize(this))))
            }
            val unSanitized = doc.text.findBetween("sources: [", "],") ?: return VideoContainer(listOf())
            val json = Mapper.parse<List<ResponseElement>>(
                "[${
                    unSanitized
                        .replace("type:", "\"type\":")
                        .replace("file:", "\"file\":")
                }]"
            )

            return VideoContainer(json.map {
                if (it.type == "hls")
                    Video(null, VideoType.M3U8, it.file, null)
                else
                    Video(null,VideoType.CONTAINER, it.file, getSize(it.file))
            })
        }

        @Serializable
        data class ResponseElement(
            @SerialName("type") val type: String,
            @SerialName("file") val file: String
        )
    }

    override suspend fun search(query: String): List<ShowResponse> {
        return client.get("$hostUrl/?s=$query").document
            .select("div.result-item article").map {
                val link = it.select("div.details div.title a").attr("href")
                val title = it.select("div.details div.title a").text()
                val cover = it.select("div.image div a img").attr("src")
                ShowResponse(title, link, cover)
            }
    }
}