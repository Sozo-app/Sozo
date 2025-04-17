package com.animestudios.animeapp.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties  // optional, but helps if your DB may have extra fields
data class UserProfile(
    var id: Int = 0,
    var name: String = "",
    var avatarUrl: String? = null,
    var bannerUrl: String? = null
)