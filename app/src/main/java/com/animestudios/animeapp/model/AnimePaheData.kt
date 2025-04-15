package com.animestudios.animeapp.model

import kotlinx.serialization.Serializable
@Serializable
data class AnimePaheData(
    @Serializable
    val current_page: Int? = null,
    @Serializable
    val `data`: List<Data>? =null,
    val from: Int? = null,
    val last_page: Int? = null,
    val per_page: Int? = null,
    val to: Int? = null,
    val total: Int? = null
)