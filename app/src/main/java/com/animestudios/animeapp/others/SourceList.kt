package com.animestudios.animeapp.others

import com.animestudios.animeapp.model.Source
import com.animestudios.animeapp.model.SourceDt
import com.animestudios.animeapp.model.SourceType

object SourceList {
    var engSources = arrayListOf(
        Source("HIANIME", "hianim"), //https://hianime.tv
        Source("KAIDO", "kaido"),//https://kaido.to
        Source("YUGEN", "yugen"),//https://yugenanime.tv
        Source("SUDACHI", "sudachi"),//https://sudatchi.com
        Source("ZORO", "zoro"),//zorotv.link
    )

    var indiaSources = arrayListOf(
        Source("ANIWORLD", "aniworld"),//https://anime-world.in
        Source("ANIRULZ", "anirulz"),//https://animerulz.net
    )

    var nativeSources = arrayListOf(
        Source("ANIMEPAHE", "pahe"),//https://animepahe.ru
        Source("ANIWATCH", "aniwatch"),//https://aniwatchtv.to/
    )
    var germanSources = arrayListOf(
        Source("ANIWORLD", "gerani"),//https://aniworld.to
    )
    var otherSources = arrayListOf(
        Source("StreamWish", "GOGO"),

        )

    var sourceList = arrayListOf(
        SourceDt(
            SourceType.GERMAN,
            germanSources
        ),
        SourceDt(
            SourceType.NATIVE,
            nativeSources
        ),

        )
}