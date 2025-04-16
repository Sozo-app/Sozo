package com.animestudios.animeapp.utils

data class AppUpdate(
    val version: String? = null,
    val releaseDate: Long? = null,
    val isMandatory: Boolean = false,
    val changeLog: String? = null,
    var appLink : String? = null
)
