package com.animestudios.animeapp.model

data class ChatListItem(
    val chatId: String,
    val otherUserId: Int,
    val otherName: String,
    val otherAvatar: String,
    val lastText: String,
    val lastTimestamp: Long,
    val lastWasRead: Boolean,
    val otherOnline: Boolean,
    val otherLastSeen: Long
)
