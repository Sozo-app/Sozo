package com.animestudios.animeapp.parsers.extractors

import com.animestudios.animeapp.findBetween
import com.animestudios.animeapp.parsers.*
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.client
import com.animestudios.animeapp.tools.defaultHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.floor
import kotlin.random.Random

class StreamSB(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val videos = mutableListOf<Video>()
        val id = server.embed.url.let { it.findBetween("/e/", ".html") ?: it.split("/e/")[1] }
        val jsonLink =
            "https://sbani.pro/375664356a494546326c4b797c7c6e756577776778623171737/${encode(id)}"
        val json = client.get(jsonLink, mapOf("watchsb" to "sbstream")).parsed<Response>()
        if (json.statusCode == 200) {
            videos.add(Video(null, VideoType.M3U8, FileUrl(json.streamData!!.file, defaultHeaders)))
        }
        return VideoContainer(videos)
    }

    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    private fun encode(id: String): String {
        val code = "${makeId()}||${id}||${makeId()}||streamsb"
        val sb = StringBuilder()
        val arr = code.toCharArray()
        for (j in arr.indices) {
            sb.append(arr[j].code.toString().toInt(10).toString(16))
        }
        return sb.toString()
    }

    private fun makeId(): String {
        val sb = StringBuilder()
        for (j in 0..12) {
            sb.append(alphabet[(floor(Random.nextDouble() * alphabet.length)).toInt()])
        }
        return sb.toString()
    }

    @Serializable
    private data class Response(
        @SerialName("stream_data")
        val streamData: StreamData? = null,
        @SerialName("status_code")
        val statusCode: Int? = null
    )

    @Serializable
    private data class StreamData(
        @SerialName("file") val file: String
    )
}
