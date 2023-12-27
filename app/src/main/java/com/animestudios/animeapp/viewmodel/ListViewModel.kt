package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.media.Media

interface ListViewModel {

    val lists: MutableLiveData<MutableMap<String, ArrayList<Media>>>
    suspend fun loadLists(anime: Boolean, userId: Int, sortOrder: String? = null)

}