package com.animestudios.animeapp.anilist.apollo

import com.animestudios.animeapp.*
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.type.NotificationType
import com.animestudios.animeapp.type.ReviewSort
import com.apollographql.apollo3.api.ApolloResponse

interface AniListAsync {
    suspend fun getNotifications(page: Int): ApolloResponse<NotificationsQuery.Data>
    suspend fun getNotificationsUnreadCount(): ApolloResponse<UnreadNotificationCountQuery.Data>
    suspend fun getExtraLargeImage(id: Int): ApolloResponse<GetImageQuery.Data>
    suspend fun toggleFavorite(animeId: Int): ApolloResponse<ToggleFavouriteMutation.Data>
    suspend fun getUserDataById(userId: Int): ApolloResponse<UserQuery.Data>
    suspend fun sendMessage(
        recipientId: Int,
        message: String,
        parentId: Int?
    ): ApolloResponse<SendMessageMutation.Data>

    suspend fun getMessages(recipientId: Int): ApolloResponse<GetMessagesQuery.Data>

    suspend fun getReview(reviewSort: ReviewSort): ApolloResponse<ReviewQuery.Data>
    suspend fun getFullDataById(media: Media): ApolloResponse<GetFullDataByIdQuery.Data>
    suspend fun getNotificationsByType(
        page: Int,
        typeIn: List<NotificationType>?,
        resetNotificationCount: Boolean
    ):ApolloResponse<NotificationsByTypeQuery.Data>

}