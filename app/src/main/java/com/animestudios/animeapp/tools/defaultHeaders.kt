package com.animestudios.animeapp.tools

import android.content.Context
import android.os.Build
import com.animestudios.animeapp.anilist.api.intercepter.HeaderInterceptor
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.toast
import com.sozo.nicehttp.Requests
import com.sozo.nicehttp.ResponseParser
import com.sozo.nicehttp.addGenericDns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

val defaultHeaders = mapOf(
    "User-Agent" to
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0"
                .format(Build.VERSION.RELEASE, Build.MODEL)
)
lateinit var cache: Cache

lateinit var okHttpClient: OkHttpClient
lateinit var client: Requests

fun initializeNetwork(context: Context) {
    val dns = readData<Int>("settings_dns")
    cache = Cache(
        File(context.cacheDir, "http_cache"),
        5 * 1024L * 1024L // 5 MiB
    )
    val cookieManager: CookieManager = CookieManager()
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    okHttpClient = OkHttpClient.Builder()

        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .followRedirects(true)
        .followSslRedirects(true)
        .apply {
            when (dns) {
                1 -> addGoogleDns()
                2 -> addCloudFlareDns()
                3 -> addAdGuardDns()
            }
        }
        .build()
//    okHttpClient.cookieJar = CookieJar { _, _ -> cookieManager }
//    okHttpClient.cookieJar =CookieJar.(cookieManager)
    client = Requests(
        okHttpClient,
        defaultHeaders,
        defaultCacheTime = 10,
        defaultCacheTimeUnit = TimeUnit.HOURS,
        responseParser = Mapper
    )
}

object Mapper : ResponseParser {

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

    inline fun <reified T> parse(text: String): T {
        return json.decodeFromString(text)
    }
}

fun <A, B> Collection<A>.asyncMap(f: suspend (A) -> B): List<B> = runBlocking {
    map { async { f(it) } }.map { it.await() }
}

fun <A, B> Collection<A>.asyncMapNotNull(f: suspend (A) -> B?): List<B> = runBlocking {
    map { async { f(it) } }.mapNotNull { it.await() }
}

fun logError(e: Throwable, post: Boolean = true, snackbar: Boolean = true) {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    val stackTrace: String = sw.toString()
    if (post) {
        if (snackbar)
            snackString(e.localizedMessage, null, stackTrace)
        else
            toast(e.localizedMessage)
    }
    e.printStackTrace()
}


suspend fun <T> tryWithSuspend(
    post: Boolean = false,
    snackbar: Boolean = true,
    call: suspend () -> T
): T? {
    return try {
        call.invoke()
    } catch (e: Throwable) {
        logError(e, post, snackbar)
        null
    } catch (e: CancellationException) {
        null
    }
}

/**
 * A url, which can also have headers
 * **/
data class FileUrl(
    val url: String,
    val headers: Map<String, String> = mapOf()
) : Serializable {
    companion object {
        operator fun get(url: String?, headers: Map<String, String> = mapOf()): FileUrl? {
            return FileUrl(url ?: return null, headers)
        }
    }
}

//Credits to leg
data class Lazier<T>(
    val lClass: KFunction<T>,
    val name: String
) {
    val get = lazy { lClass.call() }
}

fun <T> lazyList(vararg objects: Pair<String, KFunction<T>>): List<Lazier<T>> {
    return objects.map {
        Lazier(it.second, it.first)
    }
}

fun <T> T.printIt(pre: String = ""): T {
    println("$pre$this")
    return this
}


fun OkHttpClient.Builder.addGoogleDns() = (
        addGenericDns(
            "https://dns.google/dns-query",
            listOf(
                "8.8.4.4",
                "8.8.8.8"
            )
        ))

fun OkHttpClient.Builder.addCloudFlareDns() = (
        addGenericDns(
            "https://cloudflare-dns.com/dns-query",
            listOf(
                "1.1.1.1",
                "1.0.0.1",
                "2606:4700:4700::1111",
                "2606:4700:4700::1001"
            )
        ))

fun OkHttpClient.Builder.addAdGuardDns() = (
        addGenericDns(
            "https://dns.adguard.com/dns-query",
            listOf(
                // "Non-filtering"
                "94.140.14.140",
                "94.140.14.141",
            )
        ))

//@Suppress("BlockingMethodInNonBlockingContext")
//suspend fun webViewInterface(webViewDialog: WebViewBottomDialog): Map<String, String>? {
//    var map : Map<String,String>? = null
//
//    val latch = CountDownLatch(1)
//    webViewDialog.callback = {
//        map = it
//        latch.countDown()
//    }
//    val fragmentManager = (currContext() as FragmentActivity?)?.supportFragmentManager ?: return null
//    webViewDialog.show(fragmentManager, "web-view")
//    delay(0)
//    latch.await(2,TimeUnit.MINUTES)
//    return map
//}
//
//@Suppress("BlockingMethodInNonBlockingContext")
//suspend fun webViewInterface(type: String, url: FileUrl): Map<String, String>? {
//    val webViewDialog: WebViewBottomDialog = when (type) {
//        "Cloudflare" -> CloudFlare.newInstance(url)
//        else         -> return null
//    }
//    return webViewInterface(webViewDialog)
//}
//
//suspend fun webViewInterface(type: String, url: String): Map<String, String>? {
//    return webViewInterface(type,FileUrl(url))
//}