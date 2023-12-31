package com.animestudios.animeapp.anilist.apollo.client

import com.animestudios.animeapp.GetImageQuery
import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.ToggleFavouriteMutation
import com.animestudios.animeapp.UnreadNotificationCountQuery
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

    override suspend fun getExtraLargeImage(id: Int) = apolloClient.query(GetImageQuery(Optional.present(id))).execute()
    override suspend fun toggleFavorite(animeId: Int) = apolloClient.mutation(ToggleFavouriteMutation(Optional.present(animeId))).execute()

}