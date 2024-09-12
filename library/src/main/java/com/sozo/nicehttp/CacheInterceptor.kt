package com.sozo.nicehttp

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request()).newBuilder()
            .removeHeader("Cache-Control") // Remove site cache
            .removeHeader("Pragma") // Remove site cache
            .addHeader("Cache-Control", CacheControl.FORCE_CACHE.toString())
            .build()
    }
}