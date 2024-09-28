package com.animestudios.animeapp.model.animepahe

import kotlinx.serialization.Serializable

@Serializable

data class EpisodeData(
    val current_page: Int?,
    @Serializable
    val `data`: List<Data>?,
    val from: Int?,
    val last_page: Int?,
    val next_page_url: String?,
    val per_page: Int,
    @Serializable
    val prev_page_url: String?,
    val to: Int,
    val total: Int
)