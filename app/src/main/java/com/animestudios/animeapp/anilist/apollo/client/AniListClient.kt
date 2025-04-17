package com.animestudios.animeapp.anilist.apollo.client

import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.apollo.AniListAsync
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.type.NotificationType
import com.animestudios.animeapp.type.ReviewSort
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.http.get
import com.apollographql.apollo3.exception.ApolloHttpException
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.pow

class AniListClient @Inject constructor(
    private val apolloClient: ApolloClient
) : AniListAsync {
    override suspend fun getNotifications(page: Int): ApolloResponse<NotificationsQuery.Data>? {
        val maxRetries = 10
        var retryCount = 0

        val request = apolloClient.query(NotificationsQuery(Optional.present(page)))

        while (retryCount < maxRetries) {
            try {
                val response = request.execute()

                if (response.hasErrors()) {
                    throw Exception("API errors: ${response.errors}")
                }

                return response

            } catch (e: ApolloHttpException) {
                if (e.statusCode == 429) {
                    retryCount++
                    val retryAfter = e?.headers?.get("Retry-After")?.toLongOrNull()
                    val delayTime = retryAfter
                        ?: (2.0.pow(retryCount.toDouble()) * 1000).toLong() // Exponential backoff
                    println("Rate limit exceeded, retrying after $delayTime ms")
                    delay(delayTime) // Wait before retrying
                } else {
                    return null
                }
            }
        }

//        throw IllegalStateException("Max retries reached, request failed.")
        return null
    }

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