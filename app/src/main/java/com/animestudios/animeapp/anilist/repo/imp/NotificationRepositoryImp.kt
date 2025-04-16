package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.NotificationRepository
import com.animestudios.animeapp.anilist.response.AniNotification
import com.animestudios.animeapp.mapper.AniNotificationMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationRepositoryImp @Inject constructor(private val client: AniListClient) :
    NotificationRepository {
    override fun getNotificationUnReadCount() = flow {
        val response = client.getNotificationsUnreadCount().data!!
        emit(Result.success(response.Viewer?.unreadNotificationCount ?: 0))
    }

    suspend fun getNotifications(page: Int): List<AniNotification> = withContext(Dispatchers.IO) {
        val response = client.getNotifications(page)

        if (response.hasErrors() || response.data == null) {
            throw Exception(response.errors?.firstOrNull()?.message ?: "Unknown error")
        }

        val notificationsData = response.data!!.page!!.notifications

        notificationsData!!.mapNotNull { notification ->
            when (notification!!.__typename) {
                // FollowingNotification
                "FollowingNotification" -> notification!!.onFollowingNotification?.let {
                    AniNotificationMapper.mapFollowingNotification(it)
                }
                // AiringNotification
                "AiringNotification" -> notification.onAiringNotification?.let {
                    AniNotificationMapper.mapAiringNotification(it)
                }
                // ActivityLikeNotification
                "ActivityLikeNotification" -> notification.onActivityLikeNotification?.let {
                    AniNotificationMapper.mapActivityLikeNotification(it)
                }
                // ActivityMessageNotification
                "ActivityMessageNotification" -> notification.onActivityMessageNotification?.let {
                    AniNotificationMapper.mapActivityMessageNotification(it)
                }
                // ActivityMentionNotification
                "ActivityMentionNotification" -> notification.onActivityMentionNotification?.let {
                    AniNotificationMapper.mapActivityMentionNotification(it)
                }
                // ActivityReplyNotification
                "ActivityReplyNotification" -> notification.onActivityReplyNotification?.let {
                    AniNotificationMapper.mapActivityReplyNotification(it)
                }
                // ThreadCommentMentionNotification
                "ThreadCommentMentionNotification" -> notification.onThreadCommentMentionNotification?.let {
                    AniNotificationMapper.mapThreadCommentMentionNotification(it)
                }
                // ThreadCommentReplyNotification
                "ThreadCommentReplyNotification" -> notification.onThreadCommentReplyNotification?.let {
                    AniNotificationMapper.mapThreadCommentReplyNotification(it)
                }

                else -> null
            }
        }
    }
}