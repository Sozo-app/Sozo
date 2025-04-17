package com.animestudios.animeapp.parsers

import android.util.Log
import com.animestudios.animeapp.model.AnimePaheData
import com.animestudios.animeapp.model.animepahe.EpisodeData
import com.animestudios.animeapp.parsers.extractor.AnimePaheExtractor
import com.animestudios.animeapp.tools.client
import com.animestudios.animeapp.tools.getJsoup
import kotlinx.coroutines.*
import org.jsoup.select.Elements
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.regex.Pattern


class AnimePahe : AnimeParser() {
    override val name = "AnimePahe"
    override val saveName = "anime_pahe_hu"
    override val hostUrl = "https://animepahe.ru/"
    override val malSyncBackupName = "animepahe"
    override val isDubAvailableSeparately = false

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> = coroutineScope {
        var currentPage = 1
        val episodesList = mutableListOf<Episode>()
        var lastPage: Int

        while (true) {
            val response = withContext(Dispatchers.IO) {
                client.get(
                    "https://animepahe.ru/api?m=release&id=$animeLink&sort=episode_asc&page=$currentPage",
                    mapOf(
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36",
                        "Accept" to "application/json",
                        "cookie" to "__ddgid_=3CIIbMpZkAvDfTLO; __ddgmark_=01xWaCnkllOz8sRz; __ddg2_=oWnFR38NY6RYWtId; __ddg1_=sevVENmRLVKULNFteGnZ; ann-fakesite=0; __ddg9_=195.158.9.110; __ddg8_=paGVVpRmwg7Xsv1P; __ddg10_=1744825088; XSRF-TOKEN=eyJpdiI6ImNjUGd5WjFzOENPbW1xODA1K0dsK2c9PSIsInZhbHVlIjoiclcvREowb3NrNjlTK0QzNkhIOEhBWmYzUDdlZllXYVdJTDBBMHZFdlRocjN1ZXJ4MmRWVm9leXdxTFhFVHlTZkEvM0EwLzJybFJvdnA1T0RVaFZzcGMwK2VHOGV3eTRrdnRDMVZuazYxTkdPdXRDTlM4MHh2Q3ZLNmdzUWJpQUYiLCJtYWMiOiJiMGU4OTUwM2QxMmFmNTNhODQ5ZmIxMDAxZDQxZjZiZDU5ZGE0NjFiYTMwMzk2NWIxOTY1NWIwMjhjZmNmNGM3IiwidGFnIjoiIn0%3D; laravel_session=eyJpdiI6IkQ2Q2xuSzF3YXlKOHE1K0xDQzZ2NWc9PSIsInZhbHVlIjoiWmJUaEhTOTlmQlkxQmQ3OWQ5Q3paUTU2L093MUs1RGttYmFLMURXb2Q3dW02RUhReFhBeVVpc1lKSWdVUmRwZjVYbm04RUxzeVQvSExleDM3M092SU1tSlFLVjhQR20vamZ6SldhN05LS29nUURBN1dCSWQzVTNXOW9nZ1MvbXYiLCJtYWMiOiIxZjc5ZDM5NGIxNzdhNDFlY2I4OTc1ZjhkMjM1NjVlMmI3ZmQ4MjhmOGE4MzdmNmNmYjc0ZmY2NTQ2ZGEyN2UwIiwidGFnIjoiIn0%3D"
                    )
                ).parsed<EpisodeData>()
            }
            Log.d(
                "GGG",
                "loadEpisodes:https://animepahe.ru/api?m=release&id=$animeLink&sort=episode_asc&page=$currentPage "
            )

            lastPage = response.last_page ?: currentPage

            response.data?.forEach { episodeData ->
                val num = (episodeData.episode ?: 0).toString()
                val link = "$animeLink/${episodeData.session}"
                episodesList.add(Episode(num, link, episodeData.title, episodeData.snapshot ?: ""))
            }
            if (currentPage >= lastPage) {
                break
            }
            currentPage++
        }

        Log.d("GGG", "loadEpisodes: ${episodesList.size}")
        episodesList
    }

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        //https://animepahe.ru/play/cae07570-41c7-9162-3837-610c1a0ead7e/4490924323703b02a62e2f820dc5462502bce84d8d5dac408a78b06b2a279d0a
        val list = arrayListOf<VideoServer>()

        val doc = getJsoup("https://animepahe.ru/play/$episodeLink")

        val scripts: Elements = doc.getElementsByTag("script")
        for (script in scripts) {
            val scriptContent: String = script.html()
            val session: String = extractValue(scriptContent, "session").toString()
            val provider: String = extractValue(scriptContent, "provider")
            val url: String = extractValue(scriptContent, "url")

            if (provider.toString().equals("kwik")) {
                // Natijalarni chop etish
                println("Session: $session")
                println("Provider: $provider")
                println("URL: $url")
                list.add(VideoServer(provider, url))
                break
            }

        }
        return list
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        return AnimePaheExtractor(server)
    }


    override suspend fun search(query: String): List<ShowResponse> {
        val list = mutableListOf<ShowResponse>()
           val requestSearch = client.get(
            "$hostUrl/api?m=search&q=${query}", mapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36",
                "Cookie" to "__ddgid_=3CIIbMpZkAvDfTLO; __ddgmark_=01xWaCnkllOz8sRz; __ddg2_=oWnFR38NY6RYWtId; __ddg1_=sevVENmRLVKULNFteGnZ; ann-fakesite=0; __ddg9_=195.158.9.110; __ddg8_=paGVVpRmwg7Xsv1P; __ddg10_=1744825088; XSRF-TOKEN=eyJpdiI6ImNjUGd5WjFzOENPbW1xODA1K0dsK2c9PSIsInZhbHVlIjoiclcvREowb3NrNjlTK0QzNkhIOEhBWmYzUDdlZllXYVdJTDBBMHZFdlRocjN1ZXJ4MmRWVm9leXdxTFhFVHlTZkEvM0EwLzJybFJvdnA1T0RVaFZzcGMwK2VHOGV3eTRrdnRDMVZuazYxTkdPdXRDTlM4MHh2Q3ZLNmdzUWJpQUYiLCJtYWMiOiJiMGU4OTUwM2QxMmFmNTNhODQ5ZmIxMDAxZDQxZjZiZDU5ZGE0NjFiYTMwMzk2NWIxOTY1NWIwMjhjZmNmNGM3IiwidGFnIjoiIn0%3D; laravel_session=eyJpdiI6IkQ2Q2xuSzF3YXlKOHE1K0xDQzZ2NWc9PSIsInZhbHVlIjoiWmJUaEhTOTlmQlkxQmQ3OWQ5Q3paUTU2L093MUs1RGttYmFLMURXb2Q3dW02RUhReFhBeVVpc1lKSWdVUmRwZjVYbm04RUxzeVQvSExleDM3M092SU1tSlFLVjhQR20vamZ6SldhN05LS29nUURBN1dCSWQzVTNXOW9nZ1MvbXYiLCJtYWMiOiIxZjc5ZDM5NGIxNzdhNDFlY2I4OTc1ZjhkMjM1NjVlMmI3ZmQ4MjhmOGE4MzdmNmNmYjc0ZmY2NTQ2ZGEyN2UwIiwidGFnIjoiIn0%3D"
            )
        ).parsed<AnimePaheData>()
        Log.d("GGG", "search:$hostUrl/api?m=search&q=${encode(query)} ")
        requestSearch.apply {
            data?.let {
                it.onEach {
                    val link = it.session
                    val title = it.title.toString()
                    val cover = it.poster

                    list.add(ShowResponse(title, link ?: "", cover ?: ""))
                }
            }
        }

        return list
    }

    fun extractValue(scriptContent: String, key: String): String {
        val regex = "$key = \"(.*?)\""
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(scriptContent)
        return if (matcher.find()) {
            matcher.group(1)
        } else ""
    }
}
