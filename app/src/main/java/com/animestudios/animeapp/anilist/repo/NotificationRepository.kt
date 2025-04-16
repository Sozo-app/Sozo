package com.animestudios.animeapp.anilist.repo

import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.type.Query
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationUnReadCount():Flow<Result<Int>>
}