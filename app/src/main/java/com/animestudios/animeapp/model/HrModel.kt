package com.animestudios.animeapp.model

import com.animestudios.animeapp.media.Media

data class HrModel(
    val title: String,
    val mediaList: MutableList<Media>
)