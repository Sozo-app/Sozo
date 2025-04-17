package com.animestudios.animeapp.model

import com.google.firebase.database.PropertyName

// ChatMessage.kt
data class ChatMessage(
    val id: String = "",
    val fromId: Int = 0,
    val toId: Int = 0,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    // these come from your ProfileRepository when sending
    val senderName: String? = null,
    val senderAvatar: String? = null,
    @get:PropertyName("isRead")
    val isRead: Boolean = false,

    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)

// UserStatus.kt
data class UserStatus(
    @get:PropertyName("online")
    val online: Boolean = false,
    val lastSeen: Long = 0L
)