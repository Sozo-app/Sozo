package com.sozo.nicehttp

import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


val Response.cookies: Map<String, String>
    get() = this.headers.getCookies("set-cookie")

val Request.cookies: Map<String, String>
    get() = this.headers.getCookies("Cookie")

class NiceResponse(
    val okhttpResponse: Response,
    val parser: ResponseParser?
) {
    /** Lazy, initialized on use. Returns empty string on null. */
    val text by lazy { okhttpResponse.body.string() }
    val url by lazy { okhttpResponse.request.url.toString() }
    val cookies by lazy { okhttpResponse.cookies }
    val body by lazy { okhttpResponse.body }

    /** Return code */
    val code = okhttpResponse.code
    val headers = okhttpResponse.headers

    /** Size, as reported by Content-Length */
    val size by lazy {
        (okhttpResponse.headers["Content-Length"]
            ?: okhttpResponse.headers["content-length"])?.toLongOrNull()
    }

    val isSuccessful = okhttpResponse.isSuccessful

    /** As parsed by Jsoup.parse(text) */
    val document: Document by lazy { Jsoup.parse(text) }

    /** Same as using mapper.readValue<T>() */
    inline fun <reified T : Any> parsed(): T {
        return parser!!.parse(this.text, T::class)
    }

    /** Same as using try { mapper.readValue<T>() } */
    inline fun <reified T : Any> parsedSafe(): T? {
        return try {
            return parser!!.parseSafe(this.text, T::class)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Only prints the return body */
    override fun toString(): String {
        return text
    }
}