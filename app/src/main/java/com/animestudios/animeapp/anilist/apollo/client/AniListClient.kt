package com.animestudios.animeapp.anilist.apollo.client

import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.apollo.AniListAsync
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.type.NotificationType
import com.animestudios.animeapp.type.ReviewSort
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import javax.inject.Inject

class AniListClient @Inject constructor(
    private val apolloClient: ApolloClient
) : AniListAsync {
    override suspend fun getNotifications(page: Int) =
        apolloClient.query(NotificationsQuery(Optional.present(page))).execute()

    override suspend fun getNotificationsByType(
        page: Int,
        typeIn: List<NotificationType>?,
        resetNotificationCount: Boolean
    ) = apolloClient.query(
        NotificationsByTypeQuery(
            Optional.present(page),
            Optional.presentIfNotNull(typeIn),
            Optional.present(resetNotificationCount)

        )
    ).execute()

    override suspend fun getNotificationsUnreadCount() =
        apolloClient.query(UnreadNotificationCountQuery()).execute()

    override suspend
    fun getExtraLargeImage(id: Int) =
        apolloClient.query(GetImageQuery(Optional.present(id))).execute()

    override suspend fun getRelationsById(id: Int): ApolloResponse<GetRelationByIdQuery.Data> =
        apolloClient.query(
            GetRelationByIdQuery(
                Optional.present(id)
            )
        ).execute()

    override suspend
    fun toggleFavorite(animeId: Int) =
        apolloClient.mutation(ToggleFavouriteMutation(Optional.present(animeId))).execute()

    override suspend
    fun getUserDataById(userId: Int) =
        apolloClient.query(UserQuery(Optional.present(userId))).execute()

    override suspend
    fun sendMessage(
        recipientId: Int,
        message: String,
        parentId: Int?
    ): ApolloResponse<SendMessageMutation.Data> {
        val sendMessageMutation = SendMessageMutation(
            recipientId,
            message,
            Optional.presentIfNotNull(parentId),
            false
        )
        return apolloClient.mutation(sendMessageMutation).execute()
    }

    override suspend
    fun getMessages(recipientId: Int) = apolloClient.query(
        GetMessagesQuery(recipientId)
    ).execute()

    override suspend
    fun getReview(reviewSort: ReviewSort): ApolloResponse<ReviewQuery.Data> =


        apolloClient.query(
            ReviewQuery(
                Optional.present(listOf(reviewSort))
            )
        ).execute()

    override suspend
    fun getFullDataById(media: Media): ApolloResponse<GetFullDataByIdQuery.Data> =
        apolloClient.query(GetFullDataByIdQuery(Optional.present(media.id))).execute()
}