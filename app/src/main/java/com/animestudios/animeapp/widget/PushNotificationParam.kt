package com.animestudios.animeapp.widget

import com.animestudios.animeapp.model.Notification
import com.animestudios.animeapp.model.PagingDataItem
import com.animestudios.animeapp.settings.UISettings

data class PushNotificationParam(
    val notifications: List<Notification>,
    val unreadNotificationCount: Int,
    val lastNotificationId: Int
)
