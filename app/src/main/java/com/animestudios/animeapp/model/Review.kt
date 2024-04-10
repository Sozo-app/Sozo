package com.animestudios.animeapp.model

import android.os.Parcelable
import com.animestudios.animeapp.type.MediaType
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Review(
    val id: Int = 0,
    val userId: Int = 0,
    val mediaId: Int = 0,
    val mediaType: MediaType? = null,
    val summary: String = "",
    val body: String = "",
    val rating: Int = 0,
    val ratingAmount: Int = 0,
    val score: Int = 0,
    val private: Boolean = false,
    val siteUrl: String = "",
    val createdAt: Int = 0,
    val updatedAt: Int = 0,
    val user: User = User(),
    val aniListMedia: AniListMedia = AniListMedia()
) : Parcelable,Serializable