package com.animestudios.animeapp.anilist.api.intercepter

import android.util.Log
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.readData
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

        val selectedAccountType = readData<Int>("selectedAccount") ?: 1

        when (selectedAccountType) {
            1 -> {
                val token = Anilist.token ?: return chain.proceed(originalRequest)
                Log.d("GGG", "intercept:selected 1 ")
                /* Adding the header to the request. */
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
                val response = chain.proceed(newRequest)
                return response
            }
            2 -> {
                val token = Anilist.token2 ?: return chain.proceed(originalRequest)
                Log.d("GGG", "intercept:selected 2 ")
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
                val response = chain.proceed(newRequest)
                return response
            }
            else -> {
                Log.d("GGG", "intercept:selected 3 ")
                val token = Anilist.token3 ?: return chain.proceed(originalRequest)
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


    }
}
