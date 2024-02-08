package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.anilist.response.FuzzyDate
import com.animestudios.animeapp.media.Media

interface ListViewModel {

    val lists: MutableLiveData<MutableMap<String, ArrayList<Media>>>
    suspend fun loadLists(anime: Boolean, userId: Int, sortOrder: String? = null)
    suspend fun editList(
        mediaID: Int,
        progress: Int? = null,
        score: Int? = null,
        repeat: Int? = null,
        status: String? = null,
        private: Boolean = false,
        startedAt: FuzzyDate? = null,
        completedAt: FuzzyDate? = null,
        customList: List<String>? = null
    )

}