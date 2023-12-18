package com.animestudios.animeapp.anilist.api.intercepter

import com.animestudios.animeapp.anilist.api.common.Anilist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
) :
    Interceptor {

    /**
     * It intercepts the request and adds the bearer token to the header.
     *
     * @param chain Interceptor.Chain - This is the chain of interceptors that the request will go
     * through.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = Anilist.token ?: return chain.proceed(originalRequest)
        println("token :::::::::::::::::::::::$token")
        /* Adding the header to the request. */
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = chain.proceed(newRequest)
        return response
    }
}
