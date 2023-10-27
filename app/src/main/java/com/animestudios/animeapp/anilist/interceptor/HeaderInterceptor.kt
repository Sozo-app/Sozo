package com.animestudios.animeapp.anilist.interceptor

import android.content.Context
import com.animestudios.animeapp.anilist.api.common.Anilist
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.helper.HttpConnection
import javax.inject.Inject

class HeaderInterceptor  constructor() :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request().newBuilder()
                .addHeader("Authorization", "Bearer ${Anilist.token!!}")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
        )
    }
}