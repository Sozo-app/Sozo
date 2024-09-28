package com.animestudios.animeapp.parsers

import com.animestudios.animeapp.model.AnimePaheData
import com.animestudios.animeapp.model.animepahe.EpisodeData
import com.animestudios.animeapp.parsers.extractor.AnimePaheExtractor
import com.animestudios.animeapp.tools.client
import com.animestudios.animeapp.tools.getJsoup
import kotlinx.coroutines.*
import org.jsoup.select.Elements
import java.util.regex.Pattern

class AnimePahe : AnimeParser() {
    override val name = "AnimePahe"
    override val saveName = "anime_pahe_hu"
    override val hostUrl = "https://animepahe.ru/"
    override val malSyncBackupName = "animepahe"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> = coroutineScope {
        val list = mutableListOf<Episode>()

        // Birinchi sahifani olib, jami sahifalar sonini aniqlash
        println("https://animepahe.ru/api?m=release&id=$animeLink&sort=episode_asc&page=1")
        val totalPages = withContext(Dispatchers.IO) {
            client.get("https://animepahe.ru/api?m=release&id=$animeLink&sort=episode_asc&page=1")
                .parsed<EpisodeData>().last_page
        }

        // Barcha sahifalarni parallel yuklash va natijalarni yig'ish
        val allPagesData = (1..totalPages!!).map { page ->
            async(Dispatchers.IO) {
                client.get(
                    "https://animepahe.ru/api?m=release&id=$animeLink&sort=episode_asc&page=$page",
                    mapOf(
                        "dnt" to "1",
                        "Cookie" to "\n" +
                                "__ddg1_=Yhqnq62nxbM5uT9LNXBU; SERVERID=janna; latest=5633; ann-fakesite=0; res=720; aud=jpn; av1=0; dom3ic8zudi28v8lr6fgphwffqoz0j6c=33161aa3-e5ac-4f93-b315-e3165fddb0bf%3A3%3A1; sb_page_8966b6c0380845137e2f0bc664baf7be=3; sb_count_8966b6c0380845137e2f0bc664baf7be=3; sb_onpage_8966b6c0380845137e2f0bc664baf7be=1; XSRF-TOKEN=eyJpdiI6InV2RGVHeUhMNkxFelAzOG16TnRXa2c9PSIsInZhbHVlIjoiWkQyWTJaODErMnNVREhRdnZ5L0pycG1Sd2hWZkRhcjB6alN6MDZwb3ppOEpTNFpscWljYmRkVHI0RDNDN0ZxYkZIZE5jSTF2SWpjckZSaHhYWkVRZmdHMGgreE1LMlNLZXpPUnREQ3hjQ0NiZ1RZNUEwQ1hXNkxjaEdKdVc3YnAiLCJtYWMiOiJhMDRkOWU3ZjkzZWNjZmMxYTUxNTI0YWIwOTE2NTcxYTUyYWI3NTM4YTgyMzJhYmYyZDc3YjA2NWVlMjBmMDNhIiwidGFnIjoiIn0%3D; laravel_session=eyJpdiI6IlVtQnJPL3habzNObUJmWVpkTEZTTEE9PSIsInZhbHVlIjoiR2ZMditvM0ZvYnVLajArWnVYZllFcEpOUGVXYk95bWRkdXdGcUVMZE9mT0ZvYmpPSEpoMDdNeC9MWjlxMnluVHd4djZ1TGcyOHJxbEdxd013K09wemJiZlcrZHhUZUN5YkJma3pkZXN4ZVZyU0RQY0pvSnc1WHpHTHlDUWpvTE0iLCJtYWMiOiIzZGVjYTM3N2ZiYzc3ODAyOWMyNjAwODU4NWU4YTY0NTgwNjVhNTVjZGM0NjZjM2QxOTM5MzJlZTcwNTEyYzM3IiwidGFnIjoiIn0%3D; __ddgid_=QTDZaHo3uDoGqGuR; __ddgmark_=a9WzMcAyP2KIzfHF; __ddg2_=nslKhTTMfCM10kKQ",
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"

                    )
                )
                    .parsed<EpisodeData>().data!!
            }
        }.awaitAll()

        // Epizodlarni yig'ish
        allPagesData.flatten().forEach { episodeData ->
            val num = episodeData.episode ?: 0
            val thumb = episodeData.snapshot
            val link = "$animeLink/${episodeData.session}"
            list.add(Episode(num.toString(), link, episodeData.title, thumb!!))
        }

        list
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

            if (provider.toString().equals("kwik")){
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
//        https://animepahe.ru/api?m=search&q=one%20piece
        val firstSpaceIndex = query.indexOf(" ")
        val formattedQuery = if (firstSpaceIndex != -1) {
            query.substring(0, firstSpaceIndex).replace(" ", "%")
        } else {
            query
        }

        val list = mutableListOf<ShowResponse>()
        println("${hostUrl}api?m=search&q=$formattedQuery")
        client.get(
            "${hostUrl}api?m=search&q=$query", mapOf(
                "dnt" to "1",
                "Cookie" to "\n" +
                        "__ddg1_=Yhqnq62nxbM5uT9LNXBU; SERVERID=janna; latest=5633; ann-fakesite=0; res=720; aud=jpn; av1=0; dom3ic8zudi28v8lr6fgphwffqoz0j6c=33161aa3-e5ac-4f93-b315-e3165fddb0bf%3A3%3A1; sb_page_8966b6c0380845137e2f0bc664baf7be=3; sb_count_8966b6c0380845137e2f0bc664baf7be=3; sb_onpage_8966b6c0380845137e2f0bc664baf7be=1; XSRF-TOKEN=eyJpdiI6InV2RGVHeUhMNkxFelAzOG16TnRXa2c9PSIsInZhbHVlIjoiWkQyWTJaODErMnNVREhRdnZ5L0pycG1Sd2hWZkRhcjB6alN6MDZwb3ppOEpTNFpscWljYmRkVHI0RDNDN0ZxYkZIZE5jSTF2SWpjckZSaHhYWkVRZmdHMGgreE1LMlNLZXpPUnREQ3hjQ0NiZ1RZNUEwQ1hXNkxjaEdKdVc3YnAiLCJtYWMiOiJhMDRkOWU3ZjkzZWNjZmMxYTUxNTI0YWIwOTE2NTcxYTUyYWI3NTM4YTgyMzJhYmYyZDc3YjA2NWVlMjBmMDNhIiwidGFnIjoiIn0%3D; laravel_session=eyJpdiI6IlVtQnJPL3habzNObUJmWVpkTEZTTEE9PSIsInZhbHVlIjoiR2ZMditvM0ZvYnVLajArWnVYZllFcEpOUGVXYk95bWRkdXdGcUVMZE9mT0ZvYmpPSEpoMDdNeC9MWjlxMnluVHd4djZ1TGcyOHJxbEdxd013K09wemJiZlcrZHhUZUN5YkJma3pkZXN4ZVZyU0RQY0pvSnc1WHpHTHlDUWpvTE0iLCJtYWMiOiIzZGVjYTM3N2ZiYzc3ODAyOWMyNjAwODU4NWU4YTY0NTgwNjVhNTVjZGM0NjZjM2QxOTM5MzJlZTcwNTEyYzM3IiwidGFnIjoiIn0%3D; __ddgid_=QTDZaHo3uDoGqGuR; __ddgmark_=a9WzMcAyP2KIzfHF; __ddg2_=nslKhTTMfCM10kKQ",
                "Referer" to "https://animepahe.ru//api?m=search&q=$formattedQuery",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
            )
        )
            .parsed<AnimePaheData>()
            .apply {
                println("DATA :: $this")
                data.onEach {
                    val link = it.session
                    val title = it.title.toString()
                    val cover = it.poster

                    list.add(ShowResponse(title, link ?: "", cover ?: ""))
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



