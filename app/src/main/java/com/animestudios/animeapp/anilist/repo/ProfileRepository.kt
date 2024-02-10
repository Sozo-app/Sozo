package com.animestudios.animeapp.anilist.repo

import com.animestudios.animeapp.UserQuery
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun loadUserById(userId: Int): Flow<Result<UserQuery.Data>>
}