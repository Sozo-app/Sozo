package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.imp.AniListQueriesImp
import com.animestudios.animeapp.media.Studio
import java.text.DateFormat

class OtherDetailsViewModel : ViewModel() {

    private val repository =AniListQueriesImp()
    private val character: MutableLiveData<com.animestudios.animeapp.media.Character> = MutableLiveData(null)
    fun getCharacter(): LiveData<com.animestudios.animeapp.media.Character> = character
    suspend fun loadCharacter(m: com.animestudios.animeapp.media.Character) {
        if (character.value == null) character.postValue(repository.getCharacterDetails(m))
    }

    private val studio: MutableLiveData<Studio> = MutableLiveData(null)
    fun getStudio(): LiveData<Studio> = studio
    suspend fun loadStudio(m: Studio) {
        if (studio.value == null) studio.postValue(repository.getStudioDetails(m))
    }
}