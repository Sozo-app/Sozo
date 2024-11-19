package com.animestudios.animeapp.parsers

import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.tools.getJsoup

class Anirulz : AnimeParser() {

    override val name = "AnimeRulz"
    override val saveName = "hiddenleaf"
    override val hostUrl = "https://hiddenleaf.to"
    override val malSyncBackupName = "anirulz"
    override val isDubAvailableSeparately = true


    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> {
        val newQuery= getJsoup(animeLink)
        print("ANimE Link :${animeLink}")
        return emptyList()
    }

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        print("EPISODE LINK :"+episodeLink)
        return emptyList()
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        return null
    }

    override suspend fun search(query: String): List<ShowResponse> {
        print("WATCH ID:" + Anilist.animePlayId)
        print("WATCH ANIME TITLE" + Anilist.titlePlay)

        return arrayListOf(
            ShowResponse(
                Anilist.titlePlay,
                "$hostUrl/${replaceStr(Anilist.titlePlay)}-${Anilist.animePlayId}",
                coverUrl = Anilist.notFoundImg
            )
        )
    }

    private fun replaceStr(string: String): String {
        val formattedString = string.lowercase().replace(" ", "-")
        println(formattedString) // Output: shine-post
        return formattedString
    }
}