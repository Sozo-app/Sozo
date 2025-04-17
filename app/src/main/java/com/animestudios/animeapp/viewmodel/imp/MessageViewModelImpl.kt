package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.anilist.repo.MessageRepository
import com.animestudios.animeapp.anilist.repo.ProfileRepository
import com.animestudios.animeapp.model.ChatMessage
import com.animestudios.animeapp.model.UserProfile
import com.animestudios.animeapp.model.UserStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModelImpl @Inject constructor(
    private val messageRepo: MessageRepository,
    private val profileRepo: ProfileRepository
) : ViewModel() {

    // 1) raw messages + presence enrichment
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // 2) other user’s static profile
    private val _otherProfile = MutableStateFlow<UserProfile?>(null)
    val otherProfile: StateFlow<UserProfile?> = _otherProfile.asStateFlow()

    // 3) other user’s presence
    private val _otherStatus = MutableStateFlow(UserStatus())
    val otherStatus: StateFlow<UserStatus> = _otherStatus.asStateFlow()

    // 4) sending flag & errors
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private var observeJob: Job? = null

    fun startConversation(meId: Int, otherId: Int) {
        observeJob?.cancel()

        // load static profile once
        profileRepo.observeUserProfile(otherId)
            .map { it.getOrNull() }
            .distinctUntilChanged()
            .onEach { _otherProfile.value = it }
            .launchIn(viewModelScope)

        // presence stream
        messageRepo.observeUserStatus(otherId)
            .distinctUntilChanged()
            .onEach { _otherStatus.value = it }
            .launchIn(viewModelScope)

        // chat + presence enrichment + mark‑read
        val chatId = messageRepo.chatIdFor(meId, otherId)
        observeJob = messageRepo.observeChatMessages(chatId)
            .combine(messageRepo.observeUserStatus(otherId)) { msgs, status ->
                msgs.map { msg ->
                    if (msg.fromId == otherId)
                        msg.copy(isOnline = status.online, lastSeen = status.lastSeen)
                    else msg
                }
            }
            .onEach { enriched ->
                _chatMessages.value = enriched
            }
            .catch { e -> _error.emit(e.message ?: "Failed to load chat") }
            .launchIn(viewModelScope)
    }

    fun markMessagesRead(myId: Int, otherId: Int) {
        // build the same chatId you used in startConversation
        val chatId = listOf(myId, otherId).sorted().joinToString("_")
        viewModelScope.launch {
            messageRepo.markMessagesRead(chatId, otherId)
        }
    }

    fun sendMessage(meId: Int, otherId: Int, text: String) {
        viewModelScope.launch {
            _isSending.value = true
            messageRepo.sendMessage(meId, otherId, text)
                .catch { _error.emit(it.message ?: "Send failed") }
                .collect()
            _isSending.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
