package com.animestudios.animeapp.media

import java.io.Serializable

data class Source(
    val link: String,
    val name: String,
    val cover: String,
    val headers: MutableMap<String, String>? = null
) : Serializable