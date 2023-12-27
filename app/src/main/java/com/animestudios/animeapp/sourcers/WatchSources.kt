package com.animestudios.animeapp.sourcers

import com.animestudios.animeapp.anilist.response.Episode
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.parsers.AnimeParser
import com.animestudios.animeapp.parsers.BaseParser
import com.animestudios.animeapp.parsers.ShowResponse
import com.animestudios.animeapp.tools.Lazier
import com.animestudios.animeapp.tools.tryWithSuspend

abstract class WatchSources : BaseSources() {

    override operator fun get(i: Int): AnimeParser {
        return (list.getOrNull(i) ?: list[0]).get.value as AnimeParser
    }

    suspend fun loadEpisodesFromMedia(i: Int, media: Media): MutableMap<String, Episode> {
        return tryWithSuspend(true) {
            val res = get(i).autoSearch(media) ?: return@tryWithSuspend mutableMapOf()
            loadEpisodes(i, res.link, res.extra)
        } ?: mutableMapOf()
    }

    suspend fun loadEpisodes(
        i: Int,
        showLink: String,
        extra: Map<String, String>?
    ): MutableMap<String, Episode> {
        val map = mutableMapOf<String, Episode>()
        val parser = get(i)
        tryWithSuspend(true) {
            parser.loadEpisodes(showLink, extra).forEach {
                map[it.number] = Episode(
                    it.number,
                    it.link,
                    it.title,
                    it.description,
                    it.thumbnail,
                    it.isFiller,
                    extra = it.extra
                )
            }
        }
        return map
    }

}


abstract class BaseSources {
    abstract val list: List<Lazier<BaseParser>>

    val names: List<String> get() = list.map { it.name }

    fun flushText() {
        list.forEach {
            if (it.get.isInitialized())
                it.get.value.showUserText = ""
        }
    }

    open operator fun get(i: Int): BaseParser {
        return list[i].get.value
    }

    fun saveResponse(i: Int, mediaId: Int, response: ShowResponse) {
        get(i).saveShowResponse(mediaId, response, true)
    }
}

