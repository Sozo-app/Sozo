package com.animestudios.animeapp.model.shikimori

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class Anime(
    val id: Int? = null,
    val name: String? = null,
    val russian: String? = null,
    val image: Image? = null,
    val url: String? = null,
    val kind: String? = null,
    val score: String? = null,
    val status: String? = null,
    val episodes: Int? = null,
    @SerialName("episodes_aired") val episodesAired: Int? = null,
    @SerialName("aired_on") val airedOn: String? = null,
    @SerialName("released_on") val releasedOn: String? = null,
    val rating: String? = null,
    val english: List<String> = emptyList(),
    val japanese: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    @SerialName("license_name_ru") val licenseNameRu: String? = null,
    val duration: Int? = null,
    val description: String? = null,
    @SerialName("description_html") val descriptionHtml: String? = null,
    @SerialName("description_source") val descriptionSource: String? = null,
    val franchise: String? = null,
    val favoured: Boolean = false,
    val anons: Boolean = false,
    val ongoing: Boolean = false,
    @SerialName("thread_id") val threadId: Int? = null,
    @SerialName("topic_id") val topicId: Int? = null,
    @SerialName("myanimelist_id") val myanimelistId: Int? = null,
    @SerialName("rates_scores_stats") val ratesScoresStats: List<RateScoreStat> = emptyList(),
    @SerialName("rates_statuses_stats") val ratesStatusesStats: List<RateStatusStat> = emptyList(),
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("next_episode_at") val nextEpisodeAt: String? = null,
    val fansubbers: List<String> = emptyList(),
    val fandubbers: List<String> = emptyList(),
    val licensors: List<String> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val studios: List<Studio> = emptyList(),
    val videos: List<Video> = emptyList(),
    val screenshots: List<Screenshot> = emptyList(),
    @SerialName("user_rate") val userRate: String? = null
)
@Serializable
data class Image(
    val original: String,
    val preview: String,
    val x96: String,
    val x48: String
)

@Serializable
data class RateScoreStat(
    val name: Int,
    val value: Int
)

@Serializable
data class RateStatusStat(
    val name: String,
    val value: Int
)

@Serializable
data class Genre(
    val id: Int,
    val name: String,
    val russian: String,
    val kind: String,
    @SerialName("entry_type") val entryType: String
)

@Serializable
data class Studio(
    val id: Int,
    val name: String,
    @SerialName("filtered_name") val filteredName: String,
    val real: Boolean,
    val image: String?
)

@Serializable
data class Video(
    val id: Int,
    val url: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("player_url") val playerUrl: String,
    val name: String,
    val kind: String,
    val hosting: String
)

@Serializable
data class Screenshot(
    val original: String,
    val preview: String
)