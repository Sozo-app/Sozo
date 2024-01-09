package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.repo.imp.MessageRepositoryImpl
import com.animestudios.animeapp.tools.asResult
import com.animestudios.animeapp.tools.or1
import com.animestudios.animeapp.viewmodel.MessageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModelImpl @Inject constructor(
    private val messageRepositoryImpl: MessageRepositoryImpl,
    savedStateHandle: SavedStateHandle,
    ) :
    MessageViewModel, ViewModel() {
    var parentId: Int? = null
    val messageList = savedStateHandle.getStateFlow("userId", Anilist.userid.or1())
        .flatMapLatest { userId ->
            messageRepositoryImpl.getNewMessagesFlow(userId)
        }.map { messageList ->
            parentId = messageList.first().parentId
            messageList.sortedBy { message ->
                message.createdAt
            }
        }.asResult()
        .distinctUntilChanged()

    override fun sendMessage(recipientId: Int, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            messageRepositoryImpl.sendMessage(recipientId, message, parentId)
                .asResult()
                .collect()
        }
    }
}