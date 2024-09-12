package com.sozo.nicehttp

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Used to implement your own json parser of choice :)
 * */
interface ResponseParser {
    /**
     * Parse Json based on response text and the type T from parsed<T>()
     * This function can throw errors.
     * */
    fun <T : Any> parse(text: String, kClass: KClass<T>): T

    /**
     * Same as parse() but when overridden use try catch and return null on failure.
     * */
    fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T?

}

val defaultParser = object : ResponseParser {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> parse(text: String, kClass: KClass<T>): T {
        return json.decodeFromString(kClass.serializer(), text)
    }

    override fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T? {
        return try {
            parse(text, kClass)
        } catch (e: Exception) {
            null
        }
    }

}

object RequestBodyTypes {
    const val JSON = "application/json;charset=utf-8"
    const val TEXT = "text/plain;charset=utf-8"
}

fun requestCreator(
    method: String,
    url: String,
    headers: Map<String, String> = emptyMap(),
    referer: String? = null,
    params: Map<String, String> = emptyMap(),
    cookies: Map<String, String> = emptyMap(),
    data: Map<String, String>? = null,
    files: List<NiceFile>? = null,
    json: String? = null,
    requestBody: RequestBody? = null,
    cacheTime: Int? = null,
    cacheUnit: TimeUnit? = null
): Request {
    return Request.Builder()
        .url(addParamsToUrl(url, params))
        .headers(getHeaders(headers, referer, cookies))
        .method(method, getData(method, data, files, json, requestBody))
        .apply {
            if (cacheTime != null && cacheUnit != null)
                this.cacheControl(getCache(cacheTime, cacheUnit))
        }
        .build()
}

/**
 * @param baseClient base okhttp client used for all requests. Use this to get cache.
 * @param defaultHeaders base headers present in all requests, will get overwritten by custom headers.
 * Includes the NiceHttp user agent by default.
 * @param defaultTimeOut default timeout in seconds.
 * @param responseParser used for parsing, eg response.parse<T>().
 * Will throw Exception if parse() is used and parser is not implemented!
 * */
open class Requests(
    var baseClient: OkHttpClient = OkHttpClient(),
    var defaultHeaders: Map<String, String> = mapOf("user-agent" to "NiceHttp"),
    var defaultReferer: String? = null,
    var defaultData: Map<String, String> = emptyMap(),
    var defaultCookies: Map<String, String> = emptyMap(),
    var defaultCacheTime: Int = 0,
    var defaultCacheTimeUnit: TimeUnit = TimeUnit.MINUTES,
    var defaultTimeOut: Long = 0L,
    var responseParser: ResponseParser? = defaultParser,
) {
    companion object {
        suspend inline fun Call.await(): Response {
            return suspendCancellableCoroutine { continuation ->
                val callback = ContinuationCallback(this, continuation)
                enqueue(callback)
                continuation.invokeOnCancellation(callback)
            }
        }
    }

    // Regretful copy and paste function args, but I am unsure how to do it otherwise
    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    open suspend fun custom(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: String? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true
    ): NiceResponse {
        val client = baseClient
            .newBuilder()
            .followRedirects(allowRedirects)
            .followSslRedirects(allowRedirects)
            .addNetworkInterceptor(CacheInterceptor())
            .callTimeout(timeout, TimeUnit.SECONDS)
        if (timeout > 0)
            client
                .callTimeout(timeout, TimeUnit.SECONDS)
        if (!verify) client.ignoreAllSSLErrors()

        if (interceptor != null) client.addInterceptor(interceptor)
        val request =
            requestCreator(
                method,
                url,
                defaultHeaders + headers,
                referer ?: defaultReferer,
                params,
                defaultCookies + cookies,
                data,
                files,
                json,
                requestBody,
                cacheTime,
                cacheUnit
            )
        val response =
            client.build().newCall(request).await()
        return NiceResponse(response, responseParser)
    }

    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    suspend fun get(
        url: String,
        headers: Map<String, String> = mapOf(),
        referer: String? = null,
        params: Map<String, String> = mapOf(),
        cookies: Map<String, String> = mapOf(),
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true
    ): NiceResponse {
        return custom(
            "GET", url, headers, referer, params, cookies, null, null, null, null,
            allowRedirects, cacheTime, cacheUnit, timeout, interceptor, verify
        )
    }

    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    suspend fun post(
        url: String,
        headers: Map<String, String> = mapOf(),
        referer: String? = null,
        params: Map<String, String> = mapOf(),
        cookies: Map<String, String> = mapOf(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: String? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true
    ): NiceResponse {
        return custom(
            "POST", url, headers, referer, params, cookies, data, files, json, requestBody,
            allowRedirects, cacheTime, cacheUnit, timeout, interceptor, verify,
        )
    }

    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    suspend fun put(
        url: String,
        headers: Map<String, String> = mapOf(),
        referer: String? = null,
        params: Map<String, String> = mapOf(),
        cookies: Map<String, String> = mapOf(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: String? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true
    ): NiceResponse {
        return custom(
            "PUT", url, headers, referer, params, cookies, data, files, json, requestBody,
            allowRedirects, cacheTime, cacheUnit, timeout, interceptor, verify
        )
    }

    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    suspend fun delete(
        url: String,
        headers: Map<String, String> = mapOf(),
        referer: String? = null,
        params: Map<String, String> = mapOf(),
        cookies: Map<String, String> = mapOf(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: String? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true
    ): NiceResponse {
        return custom(
            "DELETE", url, headers, referer, params, cookies, data, files, json, requestBody,
            allowRedirects, cacheTime, cacheUnit, timeout, interceptor, verify
        )
    }

    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    suspend fun head(
        url: String,
        headers: Map<String, String> = mapOf(),
        referer: String? = null,
        params: Map<String, String> = mapOf(),
        cookies: Map<String, String> = mapOf(),
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true,
        responseParser: ResponseParser? = this.responseParser
    ): NiceResponse {
        return custom(
            "HEAD", url, headers, referer, params, cookies, null, null, null, null,
            allowRedirects, cacheTime, cacheUnit, timeout, interceptor, verify
        )
    }

    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    suspend fun patch(
        url: String,
        headers: Map<String, String> = mapOf(),
        referer: String? = null,
        params: Map<String, String> = mapOf(),
        cookies: Map<String, String> = mapOf(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: String? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true
    ): NiceResponse {
        return custom(
            "PATCH", url, headers, referer, params, cookies, data, files, json, requestBody,
            allowRedirects, cacheTime, cacheUnit, timeout, interceptor, verify
        )
    }

    /**
     * @param cacheUnit defaults to minutes
     * @param verify false to ignore SSL errors
     * @param timeout timeout in seconds
     * */
    suspend fun options(
        url: String,
        headers: Map<String, String> = mapOf(),
        referer: String? = null,
        params: Map<String, String> = mapOf(),
        cookies: Map<String, String> = mapOf(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: String? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true
    ): NiceResponse {
        return custom(
            "OPTIONS", url, headers, referer, params, cookies, data, files, json, requestBody,
            allowRedirects, cacheTime, cacheUnit, timeout, interceptor, verify
        )
    }
}