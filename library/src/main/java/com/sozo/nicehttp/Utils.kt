package com.sozo.nicehttp


import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.InterruptedIOException
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resumeWithException

private val mustHaveBody = listOf("POST", "PUT")
private val cantHaveBody = listOf("GET", "HEAD")

const val TAG = "NiceHttp"

/**
 * Prioritizes:
 * 0. requestBody
 * 1. data (Map)
 * 2. json (Any or String)
 * 3. files (List which can include files or normal data, but encoded differently)
 *
 * @return null if method cannot have a body or if the parameters did not give a body.
 * */
fun getData(
    method: String,
    data: Map<String, String>?,
    files: List<NiceFile>?,
    json: String?,
    requestBody: RequestBody?
): RequestBody? {
    // Can't have a body (errors). Not possible with the normal commands, but is with custom()
    if (cantHaveBody.contains(method.uppercase())) return null
    if (requestBody != null) return requestBody

    val body = if (!data.isNullOrEmpty()) {

        val builder = FormBody.Builder()
        data.forEach {
            builder.addEncoded(it.key, it.value)
        }
        builder.build()

    } else if (json != null) {
        val type = if (json.startsWith("{")) RequestBodyTypes.JSON else RequestBodyTypes.TEXT
        json.toRequestBody(type.toMediaTypeOrNull())

    } else if (!files.isNullOrEmpty()) {

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        files.forEach {
            if (it.file != null)
                builder.addFormDataPart(
                    it.name,
                    it.fileName,
                    it.file.asRequestBody(it.fileType?.toMediaTypeOrNull())
                )
            else
                builder.addFormDataPart(it.name, it.fileName)
        }
        builder.build()

    } else {
        null
    }

    // Post must have a body!
    return body ?: if (mustHaveBody.contains(method.uppercase()))
        FormBody.Builder().build() else null
}

/**
 * Referer > Set headers > Set getCookies > Default headers > Default Cookies
 */
fun getHeaders(
    headers: Map<String, String>,
    referer: String?,
    cookie: Map<String, String>
): Headers {
    val refererMap = referer?.let { mapOf("referer" to it) } ?: mapOf()
    val cookieMap =
        if (cookie.isNotEmpty()) mapOf(
            "Cookie" to cookie.entries.joinToString(" ") {
                "${it.key}=${it.value};"
            }) else mapOf()
    val tempHeaders = (headers + cookieMap + refererMap)
    return tempHeaders.toHeaders()
}

// https://stackoverflow.com/a/59322754
fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    }

    val insecureSocketFactory = SSLContext.getInstance("SSL").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}


fun Headers.getCookies(cookieKey: String): Map<String, String> {
    // Get a list of cookie strings
    // set-cookie: name=value;
    // set-cookie: name2=value2;
    // ---->
    // [name=value, name2=value2]
    val cookieList =
        this.filter { it.first.equals(cookieKey, ignoreCase = true) }.map {
            it.second.substringBefore(";")
        }

    // [name=value, name2=value2] -----> mapOf(name to value, name2 to value2)
    return cookieList.associate {
        val split = it.split("=")
        (split.getOrNull(0)?.trim() ?: "") to (split.getOrNull(1)?.trim() ?: "")
    }.filter { it.key.isNotBlank() && it.value.isNotBlank() }
}


//Provides async-able Calls
class ContinuationCallback(
    private val call: Call,
    private val continuation: CancellableContinuation<Response>
) : Callback, CompletionHandler {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onResponse(call: Call, response: Response) {
        continuation.resume(response, null)
    }

    override fun onFailure(call: Call, e: IOException) {
        // Cannot throw exception on SocketException since that can lead to un-catchable crashes
        // when you exit an activity as a request
        println("Exception in FreshQL: ${e.javaClass.name} ${e.message}")
        if (call.isCanceled()) {
            // Must be able to throw errors, for example timeouts
            if (e is InterruptedIOException)
                continuation.cancel(e)
            else
                e.printStackTrace()
        } else {
            continuation.resumeWithException(e)
        }
    }

    override fun invoke(cause: Throwable?) {
        try {
            call.cancel()
        } catch (_: Throwable) {
        }
    }
}


// https://github.com, id=test -> https://github.com?id=test
internal fun appendUri(uri: String, appendQuery: String): String {
    val oldUri = URI(uri)
    return URI(
        oldUri.scheme,
        oldUri.authority,
        oldUri.path,
        if (oldUri.query == null) appendQuery else oldUri.query + "&" + appendQuery,
        oldUri.fragment
    ).toString()
}

// Can probably be done recursively
internal fun addParamsToUrl(url: String, params: Map<String, String?>): String {
    var appendedUrl = url
    params.forEach {
        it.value?.let { value ->
            appendedUrl = appendUri(appendedUrl, "${it.key}=${value}")
        }
    }
    return appendedUrl
}

internal fun getCache(cacheTime: Int, cacheUnit: TimeUnit): CacheControl {
    return CacheControl.Builder()
        .maxAge(cacheTime, cacheUnit)
        .build()
}
