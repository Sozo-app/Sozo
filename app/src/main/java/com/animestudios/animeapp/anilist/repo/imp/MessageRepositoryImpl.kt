package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.MessageRepository
import com.animestudios.animeapp.anilist.repo.ProfileRepository
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.model.ChatMessage
import com.animestudios.animeapp.model.Message
import com.animestudios.animeapp.model.UserStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// MessageRepositoryImpl.kt
class MessageRepositoryImpl @Inject constructor(
    private val firebaseDb: FirebaseDatabase,
    private val profileRepo: ProfileRepository
) : MessageRepository {

    private val chatsRef get() = firebaseDb.getReference("chats")
    private val statusRef get() = firebaseDb.getReference("status")

    override fun chatIdFor(meId: Int, otherId: Int) =
        listOf(meId, otherId).sorted().joinToString("_")

    override fun observeChatMessages(chatId: String): Flow<List<ChatMessage>> =
        callbackFlow {
            val msgsRef = chatsRef.child(chatId).child("messages")
            val listener = object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val list = snap.children
                        .mapNotNull {
                            it.getValue(ChatMessage::class.java)
                        }
                        .sortedBy { it.timestamp }
                    trySend(list)
                }

                override fun onCancelled(err: DatabaseError) {
                    close(err.toException())
                }
            }
            msgsRef.addValueEventListener(listener)
            awaitClose { msgsRef.removeEventListener(listener) }
        }

    override fun sendMessage(meId: Int, otherId: Int, text: String): Flow<Result<Unit>> = flow {
        val chatId = chatIdFor(meId, otherId)
        val msgRef = chatsRef.child(chatId).child("messages").push()

        val profile = profileRepo.observeUserProfile(meId)
            .firstOrNull()?.getOrNull()

        val message = ChatMessage(
            id = msgRef.key.orEmpty(),
            fromId = meId,
            toId = otherId,
            text = text,
            timestamp = System.currentTimeMillis(),
            senderName = profile?.name,
            senderAvatar = profile?.avatarUrl,
            isRead = false // new messages start unread
        )

        msgRef.setValue(message).await()
        emit(Result.success(Unit))
    }.catch { emit(Result.failure(it)) }

    override fun observeUserStatus(userId: Int): Flow<UserStatus> =
        callbackFlow {
            val ref = statusRef.child(userId.toString())
            val listener = object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    snap.getValue(UserStatus::class.java)?.let { trySend(it) }
                }

                override fun onCancelled(err: DatabaseError) {
                    close(err.toException())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }

    override suspend fun markMessagesRead(chatId: String, fromUserId: Int) {
        val msgsRef = chatsRef.child(chatId).child("messages")
        msgsRef
            .orderByChild("fromId")
            .equalTo(fromUserId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { msgSnap ->
                        // set the isRead flag to true
                        msgSnap.ref.child("isRead").setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // log or handle error
                }
            })
    }
}