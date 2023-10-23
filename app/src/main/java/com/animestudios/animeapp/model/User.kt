package com.animestudios.animeapp.model

import android.os.Parcelable
import com.animestudios.animeapp.anilist.response.MediaListOptions
import com.animestudios.animeapp.anilist.response.UserStatisticTypes
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class User(
    val id: Int = 0,
    val name: String = "",
    val about: String = "",
    val avatar: UserAvatar = UserAvatar(),
    val bannerImage: String = "",
    var isFollowing: Boolean = false,
    val isFollower: Boolean = false,
    val isBlocked: Boolean = false,
    val unreadNotificationCount: Int = 0,
    val siteUrl: String = "",
    val donatorTier: Int = 0,
    val donatorBadge: String = "",
) : Parcelable {
    override fun toString(): String {
        return name
    }
}
