package com.animestudios.animeapp.mapper

import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.model.User
import com.animestudios.animeapp.model.UserAvatar
import com.apollographql.apollo3.api.ApolloResponse

fun ApolloResponse<UserQuery.Data>.convert(): User {
    val userData = this.data?.user ?: return User()
    return User(
        userData.id,
        userData.name,
        userData.about.orEmpty(),
        UserAvatar(
            userData.avatar?.large.orEmpty(),
            userData.avatar?.medium.orEmpty()
        ),
        userData.bannerImage.orEmpty()
    )
}
