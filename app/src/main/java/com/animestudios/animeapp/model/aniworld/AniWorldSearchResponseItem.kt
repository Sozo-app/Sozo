package com.animestudios.animeapp.model.aniworld

import kotlinx.serialization.Serializable

@Serializable

data class AniWorldSearchResponseItem(
    @Serializable
    val description: String,
    @Serializable
    val link: String,
    @Serializable
    val title: String
)