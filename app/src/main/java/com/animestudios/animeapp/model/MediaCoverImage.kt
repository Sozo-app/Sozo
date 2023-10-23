package com.animestudios.animeapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaCoverImage(
    val extraLarge: String = "",
    val large: String = "",
    val medium: String = ""
) : Parcelable
