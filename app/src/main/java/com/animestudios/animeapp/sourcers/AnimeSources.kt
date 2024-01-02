package com.animestudios.animeapp.sourcers

import com.animestudios.animeapp.parsers.*
import com.animestudios.animeapp.tools.Lazier
import com.animestudios.animeapp.tools.lazyList

object AnimeSources : WatchSources() {
    override val list: List<Lazier<BaseParser>> = lazyList(
        "Gogo" to ::Gogo,
    )
}

object HAnimeSources : WatchSources() {
    val aList: List<Lazier<BaseParser>> = lazyList(
        "HentaiMama" to ::HentaiMama,
    )

    override val list = listOf(aList, AnimeSources.list).flatten()
}
