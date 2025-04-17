package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.UserQuery
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.ProfileRepository
import com.animestudios.animeapp.model.UserProfile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

class ProfileRepositoryImpl @Inject constructor(
    private val client: AniListClient, private val firebaseDb: FirebaseDatabase
) : ProfileRepository {

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

    override fun ensureUserInRealtimeDb(user: UserProfile) = flow {
        val usersRef = firebaseDb.getReference("users")
        val userRef = usersRef.child(user.id.toString())
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            userRef.setValue(user).await()
        }

        emit(Result.success(Unit))
    }.catch { emit(Result.failure(it)) }

    override fun observeUserProfile(userId: Int) = callbackFlow {
        val userRef = firebaseDb.getReference("users").child(userId.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                trySend(
                    if (profile != null) Result.success(profile)
                    else Result.failure(IllegalStateException("No profile"))
                )
            }

            override fun onCancelled(err: DatabaseError) {
                trySend(Result.failure(err.toException()))
            }
        }
        userRef.addValueEventListener(listener)
        awaitClose { userRef.removeEventListener(listener) }
    }
}
