package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val client: AniListClient) :ProfileRepository{
    override fun loadUserById(userId: Int)=flow<Result<UserQuery.Data>> {
        val response=client.getUserDataById(userId).data!!
        emit(Result.success(response))
    }
}