package com.animestudios.animeapp.parsers.extractors

import com.animestudios.animeapp.getSize
import com.animestudios.animeapp.parsers.*
import com.animestudios.animeapp.tools.asyncMap
import com.animestudios.animeapp.tools.client
import kotlinx.serialization.Serializable

class FPlayer(override val server: VideoServer) : VideoExtractor() {

    override suspend fun extract(): VideoContainer {
        val url = server.embed.url
        val apiLink = url.replace("/v/", "/api/source/")
        try {
            val json = client.post(apiLink, referer = url).parsed<Json>()
            if (json.success) {
                return VideoContainer(json.data?.asyncMap {
                    Video(
                        it.label.replace("p", "").toIntOrNull(),
                        VideoType.CONTAINER,
                        it.file,
                        getSize(it.file)
                    )
                }?: listOf())
            }

        } catch (e: Exception) {}
        return VideoContainer(listOf())
    }

    @Serializable
    private data class Data(
        val file: String,
        val label: String
    )

    @Serializable
    private data class Json(
        val success: Boolean,
        val data: List<Data>?
    )
}