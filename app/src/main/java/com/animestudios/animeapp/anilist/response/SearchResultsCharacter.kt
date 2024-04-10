package com.animestudios.animeapp.anilist.response

import com.animestudios.animeapp.anilist.api.common.Anilist.genres
import com.animestudios.animeapp.anilist.api.common.Anilist.tags
import com.animestudios.animeapp.media.Character
import com.animestudios.animeapp.media.Media
import java.io.Serializable

data class SearchResultsCharacter(
    var perPage: Int? = null,
    var search: String? = null,
    var page: Int = 1,
    var results: MutableList<com.animestudios.animeapp.anilist.response.Character>,
    var hasNextPage: Boolean,
) : Serializable {
}