package com.animestudios.animeapp.anilist.repo

import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun loadUserById(userId: Int): Flow<Result<UserQuery.Data>>
    /** 2️⃣ Check if the user exists in RTDB; if not, add them */
    fun ensureUserInRealtimeDb(user: UserProfile): Flow<Result<Unit>>

    /** 3️⃣ Observe the live RTDB user object */
    fun observeUserProfile(userId: Int): Flow<Result<UserProfile>>

}