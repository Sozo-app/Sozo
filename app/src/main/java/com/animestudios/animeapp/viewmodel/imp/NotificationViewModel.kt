package com.animestudios.animeapp.viewmodel.imp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.anilist.repo.imp.NotificationRepositoryImp
import com.animestudios.animeapp.anilist.response.AniNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repo: NotificationRepositoryImp
) : ViewModel() {

    private val _notifications =
        MutableLiveData<Pair<NotificationsQuery.PageInfo, List<AniNotification>>>()
    val notifications: LiveData<Pair<NotificationsQuery.PageInfo, List<AniNotification>>> get() = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    var currentPage = 1
    private var isLastPage = false

    fun fetchNotifications(page: Int) {
        if (isLastPage) return

        if (currentPage == 1) {
            _isLoading.postValue(true)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result =
                repo.getNotifications(page)
            if (result.second != null && result.first != null) {
                Log.d("GGG", "fetchNotifications:${result.second} ")
                if (result.second!!.isNotEmpty()) {
                    _notifications.postValue(
                        Pair(
                            result.first ?: NotificationsQuery.PageInfo(
                                null,
                                null,
                                null, null
                            ),
                            result.second ?: arrayListOf()
                        )
                    )
                    isLastPage = !result.first!!.hasNextPage!!
                } else {
                    isLastPage = true
                }
            }
        }
    }

    fun loadNextPage() {
        currentPage++
        fetchNotifications(currentPage)
    }
}