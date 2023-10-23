package com.animestudios.animeapp.model

sealed class PagingDataItem {
    data class NotificationItem(val notification: Notification) : PagingDataItem()
    data class HeaderItem(val header: String) : PagingDataItem()
}