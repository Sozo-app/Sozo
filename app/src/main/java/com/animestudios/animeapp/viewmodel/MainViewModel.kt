package com.animestudios.animeapp.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData

interface MainViewModel {
    val genres: MutableLiveData<Boolean?>

    fun getGenresAndTags(activity: Activity)
    fun getGenres(activity: Activity)
}