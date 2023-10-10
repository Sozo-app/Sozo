package com.animestudios.animeapp.anilist.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable()
data class data(
    @SerialName("GenreCollection")
    val data:ArrayList<String>
)

