package com.animestudios.animeapp.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData

interface MainViewModel {
    val genres: MutableLiveData<Boolean?>
    val unReadNotificationCountLiveData: MutableLiveData<Int>

    fun getGenresAndTags(activity: Activity)
    fun getUnreadNotificationsCount()
    fun getGenres(activity: Activity)


}