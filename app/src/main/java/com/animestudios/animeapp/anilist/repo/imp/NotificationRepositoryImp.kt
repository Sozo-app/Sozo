package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.NotificationRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NotificationRepositoryImp @Inject constructor(private val client: AniListClient) :
    NotificationRepository {
    override fun getNotificationUnReadCount() = flow {
        val response = client.getNotificationsUnreadCount().data!!
        emit(Result.success(response.Viewer?.unreadNotificationCount ?: 0))
    }
}