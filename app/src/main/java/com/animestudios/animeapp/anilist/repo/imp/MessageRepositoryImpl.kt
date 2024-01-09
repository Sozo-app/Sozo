package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.MessageRepository
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(private val aniListGraphQlClient: AniListClient) :
    MessageRepository {
    private val messageSentFlow = MutableSharedFlow<Unit>()

    override fun sendMessage(
        recipientId: Int,
        message: String,
        parentId: Int?
    ): Flow<Message> = flow {
        val recipientDetails = aniListGraphQlClient.getUserDataById(recipientId).convert()

        val sentMessage =
            aniListGraphQlClient.sendMessage(recipientId, message, Anilist.userid).convert(
                recipientId = recipientDetails.id,
                recipientName = recipientDetails.name,
                recipientAvatarLarge = recipientDetails.avatar.large,
                recipientAvatarMedium = recipientDetails.avatar.medium
            )

        // Emit a unit to trigger the flow to re-emit
        messageSentFlow.emit(Unit)
        emit(sentMessage)
    }

    override fun getMessages(userId: Int): Flow<List<Message>> = flow {
        val allMessages = aniListGraphQlClient.getMessages(userId)

        emit(
            allMessages.convert().filter { message ->
                message.recipient.id == userId || message.messenger.id == userId
            }
        )
    }

    override fun getNewMessagesFlow(
        userId: Int,
        pollingInterval: Long
    ): Flow<List<Message>> =
        messageSentFlow.onStart { emit(Unit) } // Emit a unit to trigger the flow to emit immediately
            .flatMapLatest { // Use flatMapLatest to switch to the latest emission
                flow {
                    emit(Unit) // Emit a unit to trigger the flow to emit immediately
                    while (true) {
                        delay(pollingInterval) // Add delay for polling interval
                        emit(Unit)
                    }
                }
            }.flatMapLatest { // Use flatMapLatest to switch to the latest emission
                getMessages(userId)
            }
}