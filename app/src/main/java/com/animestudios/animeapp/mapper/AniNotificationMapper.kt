package com.animestudios.animeapp.mapper

import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.anilist.response.ActivityLikeNotification
import com.animestudios.animeapp.anilist.response.ActivityMentionNotification
import com.animestudios.animeapp.anilist.response.ActivityMessageNotification
import com.animestudios.animeapp.anilist.response.ActivityReplyNotification
import com.animestudios.animeapp.anilist.response.AiringNotification
import com.animestudios.animeapp.anilist.response.AniActivity
import com.animestudios.animeapp.anilist.response.AniAvatar
import com.animestudios.animeapp.anilist.response.AniHomeMedia
import com.animestudios.animeapp.anilist.response.AniNotification
import com.animestudios.animeapp.anilist.response.AniUser
import com.animestudios.animeapp.anilist.response.FollowingNotification
import com.animestudios.animeapp.anilist.response.ThreadCommentMentionNotification
import com.animestudios.animeapp.anilist.response.ThreadCommentReplyNotification
import com.animestudios.animeapp.fragment.HomeMedia
import com.animestudios.animeapp.type.Query


// Mapper klassi – GraphQL tiplarini domen modeliga aylantiradi.
object AniNotificationMapper {

    fun mapFollowingNotification(
        data: NotificationsQuery.OnFollowingNotification
    ): AniNotification {
        return FollowingNotification(
            id = data.id,
            type = data.type.toString(),
            createdAt = data.createdAt!!.toLong(),
            context = data.context,
            userId = data.userId,
            user = AniUser(
                id = data.user!!.id,
                name = data.user.name,
                avatar = AniAvatar(
                    large = data.user.avatar!!.large,
                    medium = data.user.avatar.medium
                )
            )
        )
    }

    fun mapAiringNotification(
        data: NotificationsQuery.OnAiringNotification
    ): AniNotification {
        return AiringNotification(
            id = data.id,
            type = data.type.toString(),
            createdAt = System.currentTimeMillis(), // Agar createdAt mavjud bo'lsa, uni ishlating
            context = null, // AiringNotification’da context mavjud emas, yoki kerak bo‘lsa uni moslang
            episode = data.episode,
            contexts = data.contexts!!,
            media = mapHomeMedia(
                NotificationsQuery.Media1(
                    __typename = "Media",
                    homeMedia = data.media!!.homeMedia
                )
            )
        )
    }

    fun mapActivityLikeNotification(
        data: NotificationsQuery.OnActivityLikeNotification
    ): AniNotification? {
        val activity = data.activity?.let { activityData ->
            when (activityData.__typename) {
                "ListActivity" -> activityData.onListActivity?.let {
                    AniActivity.ListActivity(
                        id = it.id ?: return null, // Use safe calls to avoid NPE
                        type = it.type.toString(),
                        createdAt = it.createdAt?.toLong()
                            ?: return null, // Safe call for `createdAt`
                        status = it.status,
                        progress = it.progress,
                        media = mapHomeMedia(it.media ?: return null) // Handle null media
                    )
                }

                "MessageActivity" -> activityData.onMessageActivity?.let {
                    AniActivity.MessageActivity(
                        id = it.id ?: return null,
                        type = it.type.toString(),
                        createdAt = it.createdAt?.toLong() ?: return null,
                        message = it.message,
                        recipient = AniUser(
                            id = it.recipient?.id ?: return null,
                            name = it.recipient?.name ?: return null,
                            avatar = AniAvatar(
                                large = it.recipient?.avatar?.large ?: return null,
                                medium = it.recipient?.avatar?.medium ?: return null
                            )
                        )
                    )
                }

                "TextActivity" -> activityData.onTextActivity?.let {
                    AniActivity.TextActivity(
                        id = it.id ?: return null,
                        type = it.type.toString(),
                        createdAt = it.createdAt?.toLong() ?: return null,
                        text = it.text
                    )
                }

                else -> null
            }
        } ?: return null // If `activity` data is null, return null

        return ActivityLikeNotification(
            id = data.id ?: return null,
            type = data.type.toString(),
            createdAt = data.createdAt?.toLong() ?: return null,
            context = data.context,
            userId = data.userId,
            user = AniUser(
                id = data.user?.id ?: return null,
                name = data.user?.name ?: return null,
                avatar = AniAvatar(
                    large = data.user?.avatar?.large ?: return null,
                    medium = data.user?.avatar?.medium ?: return null
                )
            ),
            activity = activity
        )
    }

    fun mapActivityMessageNotification(
        data: NotificationsQuery.OnActivityMessageNotification
    ): AniNotification? {
        return ActivityMessageNotification(
            id = data.id ?: return null,
            type = data.type.toString(),
            createdAt = data.createdAt?.toLong() ?: return null,
            context = data.context,
            userId = data.userId,
            message = data.message?.message ?: "",
            user = AniUser(
                id = data.user?.id ?: return null,
                name = data.user?.name ?: return null,
                avatar = AniAvatar(
                    large = data.user?.avatar?.large ?: return null,
                    medium = data.user?.avatar?.medium ?: return null
                )
            )
        )
    }

    fun mapActivityMentionNotification(
        data: NotificationsQuery.OnActivityMentionNotification
    ): AniNotification {
        return ActivityMentionNotification(
            id = data.id,
            type = data.type.toString(),
            createdAt = data.createdAt!!.toLong(),
            context = data.context,
            userId = data.userId,
            user = AniUser(
                id = data.user!!.id,
                name = data.user.name,
                avatar = AniAvatar(
                    large = data.user.avatar!!.large,
                    medium = data.user.avatar.medium
                )
            )
        )
    }

    fun mapActivityReplyNotification(
        data: NotificationsQuery.OnActivityReplyNotification
    ): AniNotification {
        return ActivityReplyNotification(
            id = data.id,
            type = data.type.toString(),
            createdAt = data.createdAt!!.toLong(),
            context = data.context,
            userId = data.userId,
            user = AniUser(
                id = data.user!!.id,
                name = data.user.name,
                avatar = AniAvatar(
                    large = data.user.avatar!!.large,
                    medium = data.user.avatar!!.medium
                )
            )
        )
    }

    fun mapThreadCommentMentionNotification(
        data: NotificationsQuery.OnThreadCommentMentionNotification
    ): AniNotification {
        return ThreadCommentMentionNotification(
            id = data.id,
            type = data.type.toString(),
            createdAt = data.createdAt!!.toLong(),
            context = data.context,
            userId = data.userId,
            user = AniUser(
                id = data.user!!.id,
                name = data.user.name,
                avatar = AniAvatar(
                    large = data.user!!.avatar!!.large,
                    medium = data.user.avatar!!.medium
                )
            )
        )
    }

    fun mapThreadCommentReplyNotification(
        data: NotificationsQuery.OnThreadCommentReplyNotification
    ): AniNotification {
        return ThreadCommentReplyNotification(
            id = data.id,
            type = data.type.toString(),
            createdAt = data.createdAt!!.toLong(),
            context = data.context,
            userId = data.userId,
            user = AniUser(
                id = data.user!!.id,
                name = data.user.name,
                avatar = AniAvatar(
                    large = data.user.avatar!!.large,
                    medium = data.user.avatar.medium
                )
            )
        )
    }

    // Mapper yordamida HomeMedia obyektini domen modeliga aylantiramiz.
    fun mapHomeMedia(data: NotificationsQuery.Media1): AniHomeMedia {
        return AniHomeMedia(
            id = data.homeMedia.id,
            title = data.homeMedia.title!!.userPreferred,
            coverImage = data.homeMedia.coverImage?.large // yoki media uchun boshqa maydonlar
        )
    }
}
