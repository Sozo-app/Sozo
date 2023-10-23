package com.animestudios.animeapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserAvatar(
    val large: String = "",
    val medium: String = ""
) : Parcelable {
    fun getImageUrl(): String {
        return large
    }
}
