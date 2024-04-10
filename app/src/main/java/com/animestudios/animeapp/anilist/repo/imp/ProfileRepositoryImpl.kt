package com.animestudios.animeapp.anilist.repo.imp
import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.ProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val client: AniListClient) :
    ProfileRepository {

    override fun loadUserById(userId: Int): Flow<Result<UserQuery.Data>> = flow {
        val response = client.getUserDataById(userId)
        val data = response.data

        if (data != null) {
            emit(Result.success(data))
        } else {
            emit(Result.failure(IOException("Failed to load user data. Response: $response")))
        }
    }.retryWhen { cause, attempt ->
        if (cause is IOException && attempt < 3) {
            delay(1000) // Optional: Wait for 1 second before retrying
            true // Retry
        } else {
            false // Don't retry
        }
    }.catch { cause ->
        emit(Result.failure(cause))
    }
}
