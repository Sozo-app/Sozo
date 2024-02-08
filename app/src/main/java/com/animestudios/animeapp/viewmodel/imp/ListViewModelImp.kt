package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.animestudios.animeapp.anilist.api.imp.AniListMutation
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.anilist.response.FuzzyDate
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.tools.tryWithSuspend
import com.animestudios.animeapp.viewmodel.ListViewModel

class ListViewModelImp : ListViewModel, ViewModel() {
    private val repositoryImp: AniListRepositoryImp = AniListRepositoryImp()
    private val mutation = AniListMutation()
    override val lists: MutableLiveData<MutableMap<String, ArrayList<Media>>> = MutableLiveData()
    var loaded: Boolean = false

    override suspend fun loadLists(anime: Boolean, userId: Int, sortOrder: String?) {
        tryWithSuspend {
            lists.postValue(repositoryImp.getMediaLists(anime, userId, sortOrder))
        }
    }

    override suspend fun editList(
        mediaID: Int,
        progress: Int?,
        score: Int?,
        repeat: Int?,
        status: String?,
        private: Boolean,
        startedAt: FuzzyDate?,
        completedAt: FuzzyDate?,
        customList: List<String>?
    ) {
        tryWithSuspend {
            mutation.editList(
                mediaID,
                progress,
                score,
                repeat,
                status,
                private,
                startedAt,
                completedAt,
                customList
            )
        }
    }

}