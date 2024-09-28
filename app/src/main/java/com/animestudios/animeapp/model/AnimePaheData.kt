package com.animestudios.animeapp.model

import kotlinx.serialization.Serializable
@Serializable
data class AnimePaheData(
    @Serializable
    val current_page: Int,
    @Serializable
    val `data`: List<Data>,
    val from: Int,
    val last_page: Int,
    val per_page: Int,
    val to: Int,
    val total: Int
)