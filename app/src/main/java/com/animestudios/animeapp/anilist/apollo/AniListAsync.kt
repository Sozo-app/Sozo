package com.animestudios.animeapp.anilist.apollo

import com.animestudios.animeapp.NotificationsQuery
import com.apollographql.apollo3.api.ApolloResponse

interface AniListAsync {
    suspend fun getNotifications(page: Int): ApolloResponse<NotificationsQuery.Data>

}