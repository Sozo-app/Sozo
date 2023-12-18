package com.animestudios.animeapp.anilist.repo

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationUnReadCount():Flow<Result<Int>>
}