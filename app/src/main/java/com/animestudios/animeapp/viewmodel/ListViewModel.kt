package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.tryWithSuspend

interface ListViewModel {

    val lists: MutableLiveData<MutableMap<String, ArrayList<Media>>>
    suspend fun loadLists(anime: Boolean, userId: Int, sortOrder: String? = null)

}