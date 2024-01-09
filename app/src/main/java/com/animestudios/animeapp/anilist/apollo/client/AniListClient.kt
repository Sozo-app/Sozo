package com.animestudios.animeapp.anilist.apollo.client

import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.apollo.AniListAsync
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import javax.inject.Inject

class AniListClient @Inject constructor(
    private val apolloClient: ApolloClient
) : AniListAsync {
    override suspend fun getNotifications(page: Int) =
        apolloClient.query(NotificationsQuery(Optional.present(page))).execute()

    override suspend fun getNotificationsUnreadCount() =
        apolloClient.query(UnreadNotificationCountQuery()).execute()

    override suspend fun getExtraLargeImage(id: Int) =
        apolloClient.query(GetImageQuery(Optional.present(id))).execute()

    override suspend fun toggleFavorite(animeId: Int) =
        apolloClient.mutation(ToggleFavouriteMutation(Optional.present(animeId))).execute()

    override suspend fun getUserDataById(userId: Int) =
        apolloClient.query(UserQuery(Optional.present(userId))).execute()

    override suspend fun sendMessage(
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

    override suspend fun getMessages(recipientId: Int) = apolloClient.query(
        GetMessagesQuery(recipientId)
    ).execute()

}