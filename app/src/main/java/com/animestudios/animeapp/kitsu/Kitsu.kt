package com.animestudios.animeapp.kitsu

import com.animestudios.animeapp.anilist.response.Episode
import com.animestudios.animeapp.logger
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.client
import com.animestudios.animeapp.tools.tryWithSuspend
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object Kitsu {
    private suspend fun getKitsuData(query: String): KitsuResponse? {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "Connection" to "keep-alive",
            "DNT" to "1",
            "Origin" to "https://kitsu.io"
        )
        val json = tryWithSuspend {
            client.post(
                "https://kitsu.io/api/graphql",
                headers,
                data = mapOf("query" to query)
            )
        }
        return json?.parsed()
    }

    suspend fun getKitsuEpisodesDetails(media: com.animestudios.animeapp.media.Media): Map<String, Episode>? {
        val print = false
        logger("Kitsu : title=${media.mainName()}", print)
        val query =
            """
query {
  lookupMapping(externalId: ${media.id}, externalSite: ANILIST_ANIME) {
    __typename
    ... on Anime {
      id
      episodes(first: 2000) {
        nodes {
          number
          titles {
            canonical
          }
          description
          thumbnail {
            original {
              url
            }
          }
        }
      }
    }
  }
}"""


        val result = getKitsuData(query) ?: return null
        logger("Kitsu : result=$result", print)
        return (result.data?.lookupMapping?.episodes?.nodes ?: return null).mapNotNull { ep ->
            val num = ep?.num?.toString() ?: return@mapNotNull null
            num to Episode(
                number = num,
                title = ep.titles?.canonical,
                desc = ep.description?.en,
                thumb = FileUrl[ep.thumbnail?.original?.url],
            )
        }.toMap()
    }

    @kotlinx.serialization.Serializable
    private data class KitsuResponse(
        @SerialName("data") val data: Data? = null
    ) {
        @kotlinx.serialization.Serializable
        data class Data(
            @SerialName("lookupMapping") val lookupMapping: LookupMapping? = null
        )

        @kotlinx.serialization.Serializable
        data class LookupMapping(
            @SerialName("id") val id: String? = null,
            @SerialName("episodes") val episodes: Episodes? = null
        )

        @kotlinx.serialization.Serializable
        data class Episodes(
            @SerialName("nodes") val nodes: List<Node?>? = null
        )

        @kotlinx.serialization.Serializable
        data class Node(
            @SerialName("number") val num: Long? = null,
            @SerialName("titles") val titles: Titles? = null,
            @SerialName("description") val description: Description? = null,
            @SerialName("thumbnail") val thumbnail: Thumbnail? = null
        )

        @kotlinx.serialization.Serializable
        data class Description(
            @SerialName("en") val en: String? = null
        )

        @kotlinx.serialization.Serializable
        data class Thumbnail(
            @SerialName("original") val original: Original? = null
        )

        @kotlinx.serialization.Serializable
        data class Original(
            @SerialName("url") val url: String? = null
        )

        @Serializable
        data class Titles(
            @SerialName("canonical") val canonical: String? = null
        )

    }

}