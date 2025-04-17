package com.animestudios.animeapp.anilist.repo

import com.animestudios.animeapp.model.ChatMessage
import com.animestudios.animeapp.model.Message
import com.animestudios.animeapp.model.UserStatus
import kotlinx.coroutines.flow.Flow

// MessageRepository.kt
interface MessageRepository {
    fun chatIdFor(meId: Int, otherId: Int): String

    /** Emits the _raw_ message list from Firebase (no presence/enrichment yet) */
    fun observeChatMessages(chatId: String): Flow<List<ChatMessage>>

    /** Sends a new ChatMessage under /chats/{chatId}/messages */
    fun sendMessage(meId: Int, otherId: Int, text: String): Flow<Result<Unit>>

    /** Emits presence updates for a given user */
    fun observeUserStatus(userId: Int): Flow<UserStatus>
    suspend fun markMessagesRead(chatId: String, readerId: Int)

}