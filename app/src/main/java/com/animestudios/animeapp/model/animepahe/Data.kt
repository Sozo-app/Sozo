package com.animestudios.animeapp.model.animepahe

import kotlinx.serialization.Serializable

@Serializable

data class Data(
    @Serializable
    val anime_id: Int?,
    @Serializable
    val audio: String?,
    @Serializable
    val created_at: String?,
    @Serializable
    val disc: String?,
    @Serializable
    val duration: String?,
    @Serializable
    val edition: String?,
    @Serializable
    val episode: Int?,
    @Serializable
    val episode2: Int?,
    @Serializable
    val filler: Int?,
    @Serializable
    val id: Int?,
    @Serializable
    val session: String?,
    @Serializable
    val snapshot: String?,
    @Serializable
    val title: String?
)