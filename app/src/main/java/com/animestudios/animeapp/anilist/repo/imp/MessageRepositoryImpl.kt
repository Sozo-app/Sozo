package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.MessageRepository
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.model.Message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(private val aniListGraphQlClient: AniListClient) :
    MessageRepository {
    private val messageSentFlow = MutableSharedFlow<Unit>()

    override fun getMessages(userId: Int) = flow<List<Message>> {
        val allMessages = aniListGraphQlClient.getMessages(userId)

        emit(
            allMessages.convert().filter { message ->
                message.recipient.id == userId || message.messenger.id == userId
            }
        )

    }

    override fun sendMessage(recipientId: Int, message: String, parentId: Int?) = flow<Message> {
        val recipientDetails = aniListGraphQlClient.getUserDataById(recipientId).convert()
        val sentMessage = aniListGraphQlClient.sendMessage(recipientId, message, parentId).convert(
            recipientId = recipientDetails.id,
            recipientName = recipientDetails.name,
            recipientAvatarLarge = recipientDetails.avatar.large,
            recipientAvatarMedium = recipientDetails.avatar.medium
        )
        messageSentFlow.emit(Unit)
        emit(sentMessage)


    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNewMessagesFlow(userId: Int, pollingInterval: Long): Flow<List<Message>> =
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