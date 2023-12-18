package com.animestudios.animeapp.di

import com.animestudios.animeapp.anilist.repo.AniListRepository
import com.animestudios.animeapp.anilist.repo.NotificationRepository
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.anilist.repo.imp.NotificationRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(ViewModelComponent::class)
@OptIn(ExperimentalCoroutinesApi::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindFavoritesRepository(repository: NotificationRepositoryImp): NotificationRepository

}