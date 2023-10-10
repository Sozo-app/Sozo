package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.tryWithSuspend
import com.animestudios.animeapp.viewmodel.ListViewModel

class ListViewModelImp : ListViewModel, ViewModel() {
    private val repositoryImp: AniListRepositoryImp = AniListRepositoryImp()
    override val lists: MutableLiveData<MutableMap<String, ArrayList<Media>>> = MutableLiveData()
    var loaded: Boolean = false

    override suspend fun loadLists(anime: Boolean, userId: Int, sortOrder: String?) {
        tryWithSuspend {
            lists.postValue(repositoryImp.getMediaLists(anime, userId, sortOrder))
        }
    }
}