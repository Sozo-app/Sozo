package com.animestudios.animeapp.anilist.apollo

import com.animestudios.animeapp.GetImageQuery
import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.ToggleFavouriteMutation
import com.animestudios.animeapp.UnreadNotificationCountQuery
import com.apollographql.apollo3.api.ApolloResponse

interface AniListAsync {
    suspend fun getNotifications(page: Int): ApolloResponse<NotificationsQuery.Data>
    suspend fun getNotificationsUnreadCount():ApolloResponse<UnreadNotificationCountQuery.Data>
    suspend fun getExtraLargeImage(id:Int):ApolloResponse<GetImageQuery.Data>
    suspend fun toggleFavorite(animeId:Int):ApolloResponse<ToggleFavouriteMutation.Data>

}