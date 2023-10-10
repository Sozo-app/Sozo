package com.animestudios.animeapp.viewmodel.imp

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.imp.AniListQueriesImp
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModelImp : ViewModel(), MainViewModel {
    val queriesImp = AniListQueriesImp()
    override val genres: MutableLiveData<Boolean?> = MutableLiveData(null)

    override fun getGenresAndTags(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            Anilist.getSavedToken(activity)

        }
    }

    fun loadProfile(runnable: Runnable) {
        viewModelScope.launch {
            if (queriesImp.loadProfile()) {
                runnable.run()
            } else
                snackString("Error loading Anilist User Data")
        }
    }

    override fun getGenres(activity: Activity) {
        viewModelScope.launch {
            genres.postValue(
                queriesImp.getGenresAndTags(
                    activity
                )
            )
        }
    }
}