package com.animestudios.animeapp.di

import android.content.Context
import android.content.SharedPreferences
import com.animestudios.animeapp.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PersistenceModule {
    private const val SHARED_PREFERENCES_NAME = BuildConfig.APPLICATION_ID + ".LocalStorage"


    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
}