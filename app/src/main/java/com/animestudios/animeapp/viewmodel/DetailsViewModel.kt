package com.animestudios.animeapp.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.media.Selected
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.parsers.ShowResponse

interface DetailsViewModel {
    fun saveSelected(id: Int, data: Selected, activity: Activity? = null)

    fun loadSelected(media: Media): Selected

    var continueMedia: Boolean?
    val loadMedia:MutableLiveData<Media>
    val responses : MutableLiveData<List<ShowResponse>?>
    fun loadById(media:Media)
    fun setMedia(media:Media)

}