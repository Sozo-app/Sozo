package com.animestudios.animeapp.parsers

import android.net.Uri
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.getSize
import com.animestudios.animeapp.parsers.extractors.FPlayer
import com.animestudios.animeapp.parsers.extractors.GogoCDN
import com.animestudios.animeapp.parsers.extractors.StreamSB
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.asyncMapNotNull
import com.animestudios.animeapp.tools.client
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.text.DecimalFormat



class AllAnime : AnimeParser() {
    override val name = "AllAnime"
    override val saveName = "allanime"
    override val hostUrl = "https://allanime.to"
    override val isDubAvailableSeparately = true

    private val apiHost = "api.allanime.day"
    private val ytAnimeCoversHost = "https://wp.youtube-anime.com/aln.youtube-anime.com"
    private val referer = "https://embed.ssbcontent.site"
    private val idRegex = Regex("${hostUrl}/anime/(\\w+)")
    private val epNumRegex = Regex("/[sd]ub/(\\d+)")


    private val idHash = "9d7439c90f203e534ca778c4901f9aa2d3ad42c06243ab2c5e6b79612af32028"
    private val episodeInfoHash = "c8f3ac51f598e630a1d09d7f7fb6924cff23277f354a23e473b962a367880f7d"
    private val searchHash = "06327bc10dd682e1ee7e07b6db9c16e9ad2fd56c1b769e47513128cd5c9fc77a"
    private val videoServerHash = "5f1a64b73793cc2234a389cf3a8f93ad82de7043017dd551f38f65b89daa65e0"

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val showId = idRegex.find(animeLink)?.groupValues?.get(1)
        if (showId != null) {
            val episodeInfos = getEpisodeInfos(showId)
            val format = DecimalFormat("#####.#####")
            return episodeInfos?.sortedBy { it.episodeIdNum }?.map { epInfo ->
                val link = """${hostUrl}/anime/$showId/episodes/${if (selectDub) "dub" else "sub"}/${epInfo.episodeIdNum}"""
                val epNum = format.format(epInfo.episodeIdNum).toString()
                val thumbnail = epInfo.thumbnails?.let {

                    if (it.isNotEmpty()) {
                        var url = it[0]
                        if (!url.startsWith("https")) {
                            url = "$ytAnimeCoversHost$url"
                        }
                        FileUrl(url)
                    } else {
                        null
                    }
                }
                val title = epInfo.notes?.substringBefore("<note-split>")
                var desc = epInfo.notes?.substringAfter("<note-split>", "")
                desc = if (desc?.isEmpty() == true) null else desc
                Episode(epNum, link = link, title, thumbnail, desc)
            } ?: emptyList()
        }
        return emptyList()
    }



    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        val showId = idRegex.find(episodeLink)?.groupValues?.get(1) ?: return emptyList()
        val episodeNum = epNumRegex.find(episodeLink)?.groupValues?.get(1) ?: return emptyList()
        val variables =
            """{"showId":"$showId","translationType":"${if (selectDub) "dub" else "sub"}","episodeString":"$episodeNum"}"""
        return graphqlQuery(variables, videoServerHash)
            .data?.episode?.sourceUrls?.map { source ->
                val serverName = source.sourceName
                val jsonUrl = if (!source.sourceUrl.startsWith("http")) {
                    val url = source.sourceUrl.decodeHash()
                    if (!url.startsWith("http"))
                        FileUrl("$referer${url.replace("clock", "clock.json")}")
                    else FileUrl(url, mapOf("referer" to referer))
                } else FileUrl(source.sourceUrl)
                VideoServer(serverName, jsonUrl, mapOf("type" to source.type))
            } ?: emptyList()
    }

    private fun String.hexDecode(): ByteArray {
        return substringAfterLast('-')
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun String.decodeHash(): String {
        return hexDecode().map {
            (it.toInt() xor 56).toChar()
        }.joinToString("")
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        val serverUrl = Uri.parse(server.embed.url)
        val domain = serverUrl.host ?: return null
        val path = serverUrl.path ?: return null
        val extractor: VideoExtractor? = when {
            "apivtwo" in path   -> AllAnimeExtractor(server)
            "taku" in domain    -> GogoCDN(server)
            "sb" in domain      -> StreamSB(server)
            else                -> null
        }
        return extractor
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val variables =
            """{"search":{"allowAdult":${Anilist.adult},"query":"$query"},"translationType":"${if (selectDub) "dub" else "sub"}"}"""
        return graphqlQuery(variables, searchHash).data?.shows?.edges?.map { show ->
            val link = """${hostUrl}/anime/${show.id}"""
            val otherNames = mutableListOf<String>()
            show.englishName?.let { otherNames.add(it) }
            show.nativeName?.let { otherNames.add(it) }
            show.altNames?.forEach { otherNames.add(it) }

            ShowResponse(
                show.name,
                link,
                show.thumbnail ?: "https://s4.anilist.co/file/anilistcdn/media/manga/cover/medium/default.jpg",
                otherNames,
                show.availableEpisodes.let { if (selectDub) it.dub else it.sub }
            )
        } ?: emptyList()
    }

    private suspend fun graphqlQuery(variables: String, persistHash: String): Query {
        val extensions = """{"persistedQuery":{"version":1,"sha256Hash":"$persistHash"}}"""
        val res = client.get(
            "https://$apiHost/api",
            headers = mapOf("origin" to hostUrl),
            params = mapOf(
                "variables" to variables,
                "extensions" to extensions
            )
        ).parsed<Query>()
        if (res.data == null) throw Exception("Var : $variables\nError : ${res.errors!![0].message}")
        return res
    }

    private suspend fun getEpisodeInfos(showId: String): List<EpisodeInfo>? {
        val variables = """{"_id": "$showId"}"""
        val show = graphqlQuery(variables, idHash).data?.show
        if (show != null) {
            val epCount = if (selectDub) show.availableEpisodes.dub else show.availableEpisodes.sub
            val epVariables = """{"showId":"$showId","episodeNumStart":0,"episodeNumEnd":${epCount}}"""
            return graphqlQuery(
                epVariables,
                episodeInfoHash
            ).data?.episodeInfos
        }
        return null
    }

    private inner class AllAnimeExtractor(override val server: VideoServer, val direct: Boolean = false) : VideoExtractor() {
        override suspend fun extract(): VideoContainer {
            val url = server.embed
            return if (direct)
                VideoContainer(listOf(Video(null, VideoType.CONTAINER, url, getSize(url))))
            else {
                val res = client.get(url.url, url.headers).parsed<VideoResponse>()
                val sub = mutableListOf<Subtitle>()
                val vid = res.links?.asyncMapNotNull { i ->
                    i.subtitles?.forEach {
                        if (it.label?.contains("vtt") == true)
                            sub.add(Subtitle(it.lang ?: return@forEach, it.src ?: return@forEach))
                    }

                    when {
                        i.crIframe == true -> {
                            i.portData?.streams?.mapNotNull {
                                val fileUrl = FileUrl(it.url ?: return@mapNotNull null, mapOf("referer" to referer))
                                when {
                                    it.format == "adaptive_dash" && it.hardsubLang == "en-US"
                                    ->
                                        Video(null, VideoType.DASH, fileUrl, null, "DASH")

                                    it.format == "adaptive_hls" && it.hardsubLang == "en-US"
                                    ->
                                        Video(null, VideoType.M3U8, fileUrl, null, "M3U8")

                                    else -> null
                                }
                            }
                        }

                        i.hls == true      -> {
                            val fileUrl = FileUrl(i.link ?: return@asyncMapNotNull null, mapOf("referer" to referer))
                            listOf(
                                Video(null, VideoType.M3U8, fileUrl, null, i.resolutionStr)
                            )
                        }

                        i.mp4 == true      -> {
                            val fileUrl = FileUrl(i.link ?: return@asyncMapNotNull null, mapOf("referer" to referer))
                            listOf(
                                Video(null, VideoType.M3U8, fileUrl, getSize(fileUrl), i.resolutionStr)
                            )
                        }

                        else               -> null
                    }
                }?.flatten() ?: listOf()
                VideoContainer(vid, sub)
            }
        }
    }

    @Serializable
    private data class Query(
        @SerialName("data") var data: Data?,
        var errors: List<Error>?
    ) {

        @Serializable
        data class Error(
            var message: String
        )

        @Serializable
        data class Data(
            @SerialName("shows") val shows: ShowsConnection?,
            @SerialName("show") val show: Show?,
            @SerialName("episodeInfos") val episodeInfos: List<EpisodeInfo>?,
            @SerialName("episode") val episode: AllAnimeEpisode?,
        )

        @Serializable
        data class ShowsConnection(
            @SerialName("edges") val edges: List<Show>
        )

        @Serializable
        data class Show(
            @SerialName("_id") val id: String,
            @SerialName("name") val name: String,
            @SerialName("englishName") val englishName: String?,
            @SerialName("nativeName") val nativeName: String?,
            @SerialName("thumbnail") val thumbnail: String?,
            @SerialName("availableEpisodes") val availableEpisodes: AvailableEpisodes,
            @SerialName("altNames") val altNames: List<String>?
        )

        @Serializable
        data class AvailableEpisodes(
            @SerialName("sub") val sub: Int,
            @SerialName("dub") val dub: Int,
            // @SerialName("raw") val raw: Int,
        )

        @Serializable
        data class AllAnimeEpisode(
            @SerialName("sourceUrls") var sourceUrls: List<SourceUrl>
        )

        @Serializable
        data class SourceUrl(
            val sourceUrl: String,
            val sourceName: String,
            val type: String
        )
    }

    @Serializable
    private data class EpisodeInfo(
        // Episode "numbers" can have decimal values, hence float
        @SerialName("episodeIdNum") val episodeIdNum: Float,
        @SerialName("notes") val notes: String?,
        @SerialName("thumbnails") val thumbnails: List<String>?,
        @SerialName("vidInforssub") val vidInforssub: VidInfo?,
        @SerialName("vidInforsdub") val vidInforsdub: VidInfo?,
    ) {
        @Serializable
        data class VidInfo(
            // @SerialName("vidPath") val vidPath
            @SerialName("vidResolution") val vidResolution: Int?,
            @SerialName("vidSize") val vidSize: Double?,
        )
    }

    @Serializable
    private data class VideoResponse(
        val links: List<Link>? = null
    ) {
        @Serializable
        data class Link(
            val link: String? = null,
            val crIframe: Boolean? = null,
            val portData: PortData? = null,
            val resolutionStr: String? = null,
            val hls: Boolean? = null,
            val mp4: Boolean? = null,
            val subtitles: List<Subtitle>? = null
        )

        @Serializable
        data class PortData(
            val streams: List<Stream>? = null
        )

        @Serializable
        data class Stream(
            val format: String? = null,
            val url: String? = null,

            @SerialName("audio_lang")
            val audioLang: String? = null,

            @SerialName("hardsub_lang")
            val hardsubLang: String? = null
        )

        @Serializable
        data class Subtitle(
            val lang: String? = null,
            val src: String? = null,
            val label: String? = null,
            val default: String? = null
        )
    }
}
