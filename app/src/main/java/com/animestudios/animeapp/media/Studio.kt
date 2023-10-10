package com.animestudios.animeapp.media

import java.io.Serializable
import java.util.*

data class Studio(
    val id: String,
    val name: String,
    var yearMedia: MutableMap<String, ArrayList<Media>>? = null
) : Serializable
