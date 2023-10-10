package com.animestudios.animeapp.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData

interface ActivityViewModel {
    val genres: MutableLiveData<Boolean?>

    fun getGenresAndTags(activity: Activity)

}