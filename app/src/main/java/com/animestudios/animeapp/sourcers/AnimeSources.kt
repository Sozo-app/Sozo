package com.animestudios.animeapp.sourcers

import com.animestudios.animeapp.parsers.AnimePahe
import com.animestudios.animeapp.parsers.BaseParser
import com.animestudios.animeapp.parsers.Gogo
import com.animestudios.animeapp.parsers.HentaiMama
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.tools.Lazier
import com.animestudios.animeapp.tools.lazyList

object AnimeSources : WatchSources() {
    override val list: List<Lazier<BaseParser>> = lazyList(
        when (readData("selectedSource") ?: "GOGO") {
            "GOGO" -> "Gogo" to ::Gogo
            "pahe" -> "Animepahe" to ::AnimePahe
            else -> "Gogo" to ::Gogo
        }
    )
}

object HAnimeSources : WatchSources() {
    val aList: List<Lazier<BaseParser>> = lazyList(
        "HentaiMama" to ::HentaiMama,
    )

    override val list = listOf(aList, AnimeSources.list).flatten()
}
