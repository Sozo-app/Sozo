package com.animestudios.animeapp.others

import com.animestudios.animeapp.model.Source
import com.animestudios.animeapp.model.SourceDt
import com.animestudios.animeapp.model.SourceType

object SourceList {
    var engSources = arrayListOf(
        Source("ANIMEPAHE", "https://animepahe.ru"),
        Source("HIANIME", "https://hianime.tv"),
        Source("KAIDO", "https://kaido.to"),
        Source("YUGEN", "https://yugenanime.tv"),
        Source("SUDACHI", "https://sudatchi.com"),
        Source("ZORO", "zorotv.link"),
    )

    var indiaSources = arrayListOf(
        Source("ANIWORLD", "https://anime-world.in"),
        Source("ANIRULZ", "https://animerulz.net"),
    )

    var germanSources = arrayListOf(
        Source("ANIWORLD", "https://aniworld.to"),
        Source("ANIWORLD", "https://aniworld.to"),
    )
    var otherSources = arrayListOf(
        Source("StreamWish", "awish.pro")
    )

    var sourceList = arrayListOf(
        SourceDt(SourceType.ENGLISH, engSources),
        SourceDt(
            SourceType.INDIA,
            indiaSources
        ),
        SourceDt(
            SourceType.GERMAN,
            germanSources
        ),
        SourceDt(SourceType.OTHER, otherSources)
    )
}