package com.animestudios.animeapp.di

import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val database =
            FirebaseDatabase.getInstance("https://sozo-app-a36e6-default-rtdb.asia-southeast1.firebasedatabase.app")
        return database
    }
}
