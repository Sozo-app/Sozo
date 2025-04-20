package com.animestudios.animeapp.viewmodel.imp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.imp.AniListQueriesImp
import com.animestudios.animeapp.anilist.repo.imp.NotificationRepositoryImp
import com.animestudios.animeapp.anilist.repo.imp.ProfileRepositoryImpl
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.di.FirebaseService
import com.animestudios.animeapp.model.UserProfile
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.utils.AppUpdate
import com.animestudios.animeapp.utils.AppUtils
import com.animestudios.animeapp.utils.PresenceManager
import com.animestudios.animeapp.viewmodel.MainViewModel
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModelImp @Inject constructor(
    private var notificationRepository: NotificationRepositoryImp,
    private val firebaseService: FirebaseService,
    private val firebaseDb: FirebaseDatabase,
    val repositoryImpl: ProfileRepositoryImpl
) :
    ViewModel(), MainViewModel {
    private val queriesImp = AniListQueriesImp()
    override val genres: MutableLiveData<Boolean?> = MutableLiveData(null)
    override val unReadNotificationCountLiveData: MutableLiveData<Int> = MutableLiveData()
    val isUpdateAvailableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val getAppUpdateInfo: MutableLiveData<AppUpdate> = MutableLiveData()

    override fun getGenresAndTags(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            Anilist.getSavedToken(activity)
        }
    }


    fun checkForAppUpdate() {
        firebaseService.getAppUpdateInfo().observeForever { appUpdate ->
            if (appUpdate != null) {
                if (isUpdateAvailable(
                        AppUtils.getAppVersion(context = App.currentContext()!!)!!,
                        appUpdate.version
                    )
                ) {

                    isUpdateAvailableLiveData.postValue(true)
                } else {
                    isUpdateAvailableLiveData.postValue(false)
                }
            } else {
                isUpdateAvailableLiveData.postValue(false)
            }
        }
    }

    fun getAppUpdateInfo() {

        firebaseService.getAppUpdateInfo().observeForever { appUpdate ->
            appUpdate?.let {
                getAppUpdateInfo.postValue(appUpdate!!)
            }
        }
    }

    private fun isUpdateAvailable(currentVersion: String, newVersion: String?): Boolean {
        return if (newVersion != null) {
            compareVersions(currentVersion, newVersion) < 0
        } else {
            false
        }
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val version1Parts = version1.split(".").map { it.toInt() }
        val version2Parts = version2.split(".").map { it.toInt() }

        for (i in 0 until Math.min(version1Parts.size, version2Parts.size)) {
            val comparison = version1Parts[i].compareTo(version2Parts[i])
            if (comparison != 0) {
                return comparison
            }
        }
        Log.d("GGG", "compareVersions:${version1Parts} ${version2Parts} ")
        return version1Parts.size.compareTo(version2Parts.size)
    }


    fun getSavedTokenByType(activity: Activity, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Anilist.getSavedToken(activity, type, byType = true)
        }
    }


    override fun getUnreadNotificationsCount() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.getNotificationUnReadCount()
                .onEach {
                    it.onSuccess {
                        unReadNotificationCountLiveData.postValue(it)
                    }
                    it.onFailure {

                    }
                }.launchIn(viewModelScope)
        }
    }

    suspend fun loadProfile(success: (() -> Unit)) {
        viewModelScope.launch {
            if (queriesImp.loadProfile()) {
                repositoryImpl.loadUserById(Anilist.userid!!).collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            data.user?.let { gqlUser ->
                                val profile = UserProfile(
                                    id = gqlUser.id,
                                    name = gqlUser.name,
                                    avatarUrl = gqlUser.avatar?.large,
                                    bannerUrl = gqlUser.bannerImage
                                )

//                                userData.postValue(Resource.Success(data))
                                repositoryImpl.ensureUserInRealtimeDb(profile)
                                    .catch { e -> }
                                    .onEach {

                                    }.launchIn(viewModelScope)
                            } ?: run {
                            }
                        },
                        onFailure = { throwable ->
                        }
                    )
                }
                Anilist.userid?.let { PresenceManager(firebaseDb).start(myUserId = it) }
                success.invoke()
            } else
                snackString("Error loading AniList User Data")
        }
    }


    override fun getGenres(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            genres.postValue(
                queriesImp.getGenresAndTags(
                    activity
                )
            )
        }
    }
}