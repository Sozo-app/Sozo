package com.animestudios.animeapp.tools

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun getJson(
    url: String,
    mapOfHeaders: Map<String, String>? = null
): JsonElement? {
    return JsonParser.parseString(get(url, mapOfHeaders))
}

fun postJson(
    url: String,
    mapOfHeaders: Map<String, String>? = null,
    payload: Map<String, String>? = null
): JsonElement? {
    val res = post(url, mapOfHeaders, payload)
    return JsonParser.parseString(res)
}

fun get(
    url: String,
    mapOfHeaders: Map<String, String>? = null
): String {
    val requestBuilder = Request.Builder().url(url)
    if (!mapOfHeaders.isNullOrEmpty()) {
        mapOfHeaders.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
    }
    return okHttpClient.newCall(requestBuilder.build())
        .execute().body!!.string()
}

fun post(
    url: String,
    mapOfHeaders: Map<String, String>? = null,
    payload: Map<String, String>? = null
): String {
    val requestBuilder = Request.Builder().url(url)

    if (!mapOfHeaders.isNullOrEmpty()) {
        mapOfHeaders.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
    }

    val requestBody = payload?.let {
        FormBody.Builder().apply {
            it.forEach { (key, value) ->
                add(key, value)
            }
        }.build()
    }

    if (requestBody != null) {
        requestBuilder.post(requestBody)
    }

    val response = okHttpClient.newCall(requestBuilder.build()).execute()
    return response.body?.string() ?: ""
}

fun getJsoup(
    url: String,
    mapOfHeaders: Map<String, String>? = null
): Document {
    return Jsoup.parse(get(url, mapOfHeaders))
}
