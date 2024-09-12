package com.sozo.nicehttp

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class Session(
    client: OkHttpClient
) : Requests() {
    init {
        this.baseClient = client
            .newBuilder()
            .cookieJar(CustomCookieJar())
            .build()
    }

    open inner class CustomCookieJar : CookieJar {
        var cookies = mapOf<String, Cookie>()

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return this.cookies.values.toList()
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            this.cookies += cookies.map { it.name to it }
        }
    }
}