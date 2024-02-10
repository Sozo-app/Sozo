package com.animestudios.animeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.tools.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileViewModel {
    val userData:MutableLiveData<Resource<UserQuery.Data>>
    fun loadUserById(userId:Int)
}