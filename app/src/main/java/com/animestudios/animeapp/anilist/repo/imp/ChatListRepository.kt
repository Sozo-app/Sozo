package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.model.ChatListItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class ChatListRepository @Inject constructor(
    database: FirebaseDatabase
) {
    private val chatsRef = database.getReference("chats")
    private val usersRef = database.getReference("users")
    private val statusRef = database.getReference("status")

    // In-memory caches for user profiles and presence
    private val userCache = mutableMapOf<Int, Pair<String, String>>()      // id -> (name, avatarUrl)
    private val statusCache = mutableMapOf<Int, Pair<Boolean, Long>>()     // id -> (online, lastSeen)

    init {
        // Keep userCache up-to-date
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { userSnap ->
                    userSnap.key?.toIntOrNull()?.let { id ->
                        val name = userSnap.child("name").getValue(String::class.java).orEmpty()
                        val avatar = userSnap.child("avatarUrl").getValue(String::class.java).orEmpty()
                        userCache[id] = name to avatar
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Keep statusCache up-to-date
        statusRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { statusSnap ->
                    statusSnap.key?.toIntOrNull()?.let { id ->
                        val online = statusSnap.child("online").getValue(Boolean::class.java) ?: false
                        val lastSeen = statusSnap.child("lastSeen").getValue(Long::class.java) ?: 0L
                        statusCache[id] = online to lastSeen
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Emits a list of ChatListItem sorted by lastTimestamp descending.
     * Uses in-memory caches to avoid synchronous Task.get() calls.
     */
    fun observeChatList(currentUserId: Int): Flow<List<ChatListItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { chatSnap ->
                    val chatId = chatSnap.key ?: return@mapNotNull null
                    // Only include chats involving this user
                    if (!chatId.startsWith("${currentUserId}_") && !chatId.endsWith("_${currentUserId}")) return@mapNotNull null

                    // Determine the other user ID
                    val (a, b) = chatId.split("_")
                    val otherId = a.toInt().takeIf { it != currentUserId } ?: b.toInt()

                    // Find the most recent message
                    val lastMsg = chatSnap.child("messages").children.maxByOrNull {
                        it.child("timestamp").getValue(Long::class.java) ?: 0L
                    } ?: return@mapNotNull null

                    val text   = lastMsg.child("text").getValue(String::class.java).orEmpty()
                    val ts     = lastMsg.child("timestamp").getValue(Long::class.java) ?: 0L
                    val isRead = lastMsg.child("isRead").getValue(Boolean::class.java) ?: false

                    // Lookup from caches (fallbacks if missing)
                    val (name, avatarUrl) = userCache[otherId] ?: ("Unknown" to "")
                    val (online, lastSeen) = statusCache[otherId] ?: (false to 0L)

                    ChatListItem(
                        chatId = chatId,
                        otherUserId = otherId,
                        otherName = name,
                        otherAvatar = avatarUrl,
                        lastText = text,
                        lastTimestamp = ts,
                        lastWasRead = isRead,
                        otherOnline = online,
                        otherLastSeen = lastSeen
                    )
                }
                    .sortedByDescending { it.lastTimestamp }

                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        chatsRef.addValueEventListener(listener)
        awaitClose { chatsRef.removeEventListener(listener) }
    }
}
