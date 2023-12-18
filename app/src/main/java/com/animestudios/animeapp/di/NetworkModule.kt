package com.animestudios.animeapp.di

import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.intercepter.HeaderInterceptor
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.tools.Apollo
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module

object NetworkModule {
    @Provides
    @Singleton
    @Apollo
    fun provideOkHttpClient(
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .addInterceptor(HeaderInterceptor())
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()


    @Provides
    @Singleton
    fun provideApolloClient(
        @Apollo okHttpClient: OkHttpClient
    ): ApolloClient = ApolloClient.Builder()
        .serverUrl(Anilist.BASE_URL)
        .okHttpClient(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideAniListGraphQlClient(
        apolloClient: ApolloClient
    ): AniListClient = AniListClient(apolloClient)

}