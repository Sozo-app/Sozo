package com.animestudios.animeapp.viewmodel.imp

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.imp.AniListQueriesImp
import com.animestudios.animeapp.anilist.repo.imp.NotificationRepositoryImp
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.viewmodel.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModelImp @Inject constructor(private var notificationRepository: NotificationRepositoryImp) :
    ViewModel(), MainViewModel {
    private val queriesImp = AniListQueriesImp()
    override val genres: MutableLiveData<Boolean?> = MutableLiveData(null)
    override val unReadNotificationCountLiveData: MutableLiveData<Int> = MutableLiveData()

    override fun getGenresAndTags(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            Anilist.getSavedToken(activity)
        }
    }

    fun getSavedTokenByType(activity: Activity, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Anilist.getSavedToken(activity, type, byType = true)
        }
    }


    override fun getUnreadNotificationsCount() {
    }

   suspend  fun loadProfile(success: (() -> Unit)) {
        viewModelScope.launch {
            if (queriesImp.loadProfile()) {
                success.invoke()
            } else
                snackString("Error loading AniList User Data")
        }
    }


    override fun getGenres(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            genres.postValue(
                queriesImp.getGenresAndTags(
                    activity
                )
            )
        }
    }
}