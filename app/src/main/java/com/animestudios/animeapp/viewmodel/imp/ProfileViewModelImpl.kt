package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.anilist.repo.imp.ProfileRepositoryImpl
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.viewmodel.ProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModelImpl @Inject constructor(val repositoryImpl: ProfileRepositoryImpl): ProfileViewModel, ViewModel() {
    override val userData: MutableLiveData<Resource<UserQuery.Data>> = MutableLiveData()

    override fun loadUserById(userId: Int) {
        userData.postValue(Resource.Loading)
        repositoryImpl.loadUserById(userId).onEach {
            it.onSuccess {
                userData.postValue(Resource.Success(it))
            }
            it.onFailure {
                userData.postValue(Resource.Error(it))
            }
        }.launchIn(viewModelScope)

    }
}