package com.animestudios.animeapp.model

import kotlinx.serialization.Serializable

@Serializable()
data class Data(
    @Serializable
    val episodes: Int,
    @Serializable
    val id: Int ?,
    @Serializable
    val poster: String?,
    @Serializable
    val score: Double?,
    @Serializable
    val season: String?,
    @Serializable
    val session: String?,
    @Serializable
    val status: String?,
    @Serializable
    val title: String?,
    @Serializable
    val type: String?,
    @Serializable
    val year: Int?
)