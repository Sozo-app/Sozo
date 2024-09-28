package com.animestudios.animeapp.parsers.extractor

import com.animestudios.animeapp.parsers.*
import com.animestudios.animeapp.tools.getJsoup
import com.animestudios.animeapp.utils.JsUnpacker
import org.jsoup.select.Elements

class AnimePaheExtractor(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val scrapVideos = mutableListOf<Video>()
        val doc = getJsoup(
            server.embed.url, mapOf(
                "Referer" to "https://animepahe.ru/",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0",
                "Alt-Used" to "kwik.si",
                "Host" to "kwik.si",
                "Sec-Fetch-User" to "?1"
            )
        )
        println(doc)
        val scripts: Elements = doc.getElementsByTag("script")
        var evalContent: String? = null
        for (script in scripts) {
            val scriptContent = script.html()

            if (scriptContent.contains("eval(function(p,a,c,k,e,d){")) {
                println("Found eval function: \n$scriptContent")
                evalContent = scriptContent
                break
            }
        }

        val urlM3u8 = extractFileUrl(getAndUnpack(evalContent.toString())) ?: ""

        scrapVideos.add(Video(null, VideoType.M3U8, urlM3u8))
        return VideoContainer(scrapVideos)
    }


    private val packedRegex = Regex("""eval\(function\(p,a,c,k,e,.*\)\)""")
    fun getPacked(string: String): String? {
        return packedRegex.find(string)?.value
    }

    fun getAndUnpack(string: String): String {
        val packedText = getPacked(string)
        return JsUnpacker(packedText).unpack() ?: string
    }

    fun extractFileUrl(input: String): String? {
        val regex = Regex("https?://\\S+\\.m3u8")
        val matchResult = regex.find(input)
        return matchResult?.value // Agar topilgan bo'lsa, URL manzilini qaytaradi
    }
}

