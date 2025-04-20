package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.ViewModel
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.repo.imp.ChatListRepository
import com.animestudios.animeapp.model.ChatListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class ChatListViewModelImp @Inject constructor(
     repository: ChatListRepository
) : ViewModel() {
    private val currentUserId: Int = Anilist.userid!!
    val chats: Flow<List<ChatListItem>> = repository.observeChatList(currentUserId)
}