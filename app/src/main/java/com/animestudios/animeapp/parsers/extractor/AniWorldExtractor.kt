package com.animestudios.animeapp.parsers.extractor

import com.animestudios.animeapp.parsers.*
import com.animestudios.animeapp.tools.getJsoup
import java.util.regex.Matcher
import java.util.regex.Pattern

class AniWorldExtractor(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val videoList = arrayListOf<Video>()
        val document = getJsoup(server.embed.url, server.embed.headers)
        val script = document.select("script").html()
        var url: String? = null
        if (script.contains("window.location.href = '")) {
            val startIndex =
                script.indexOf("window.location.href = '") + "window.location.href = '".length
            val endIndex = script.indexOf("';", startIndex)
            url = script.substring(startIndex, endIndex)
        }
        val newDoc = getJsoup(url ?: "")

        var link  = ""
        val regex = "https?://[^\\s'\")]+\\.m3u8[^\\s'\")]+"
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(newDoc.html())
        while (matcher.find()) {
            val foundUrl = matcher.group()
            link = foundUrl
            println("Olingan link: $foundUrl")
        }
        videoList.add(Video(null, VideoType.M3U8, link))
        return VideoContainer(videoList)
    }

}