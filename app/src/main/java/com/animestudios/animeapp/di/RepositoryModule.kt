package com.animestudios.animeapp.di

import com.animestudios.animeapp.anilist.repo.NotificationRepository
import com.animestudios.animeapp.anilist.repo.ReviewRepository
import com.animestudios.animeapp.anilist.repo.imp.NotificationRepositoryImp
import com.animestudios.animeapp.anilist.repo.imp.ReviewRepositoryImpl
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

    @Binds
    abstract fun bindReviewRepository(repository: ReviewRepositoryImpl): ReviewRepository


}