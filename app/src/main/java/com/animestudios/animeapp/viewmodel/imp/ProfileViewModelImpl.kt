package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.anilist.repo.imp.ProfileRepositoryImpl
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.model.UserProfile
import com.animestudios.animeapp.viewmodel.ProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModelImpl @Inject constructor(val repositoryImpl: ProfileRepositoryImpl) :
    ProfileViewModel, ViewModel() {
    override val userData: MutableLiveData<Resource<UserQuery.Data>> = MutableLiveData()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()
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

    fun loadProfile(userId: Int) {
        viewModelScope.launch {
            userData.postValue(Resource.Loading)
            repositoryImpl.loadUserById(userId).collect { result ->
                result.fold(
                    onSuccess = { data ->
                        data.user?.let { gqlUser ->
                            val profile = UserProfile(
                                id = gqlUser.id,
                                name = gqlUser.name,
                                avatarUrl = gqlUser.avatar?.large,
                                bannerUrl = gqlUser.bannerImage
                            )

                            userData.postValue(Resource.Success(data))
                            repositoryImpl.ensureUserInRealtimeDb(profile)
                                .catch { e -> _error.emit(e.message ?: "Firebase error") }
                                .onEach {

                                }.launchIn(viewModelScope)
                        } ?: run {
                            _error.emit("No user data returned")
                        }
                    },
                    onFailure = { throwable ->
                        _error.emit(throwable.message ?: "Failed to load user")
                    }
                )
            }
        }
    }
}