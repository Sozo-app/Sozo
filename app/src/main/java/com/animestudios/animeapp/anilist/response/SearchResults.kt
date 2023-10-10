package com.animestudios.animeapp.anilist.response

import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.currContext
import com.animestudios.animeapp.media.Media
import java.io.Serializable

data class SearchResults(
    val type: String,
    var isAdult: Boolean,
    var onList: Boolean? = null,
    var perPage: Int? = null,
    var search: String? = null,
    var sort: String? = null,
    var genres: MutableList<String>? = null,
    var excludedGenres: MutableList<String>? = null,
    var tags: MutableList<String>? = null,
    var excludedTags: MutableList<String>? = null,
    var format: String? = null,
    var seasonYear: Int? = null,
    var season: String? = null,
    var page: Int = 1,
    var results: MutableList<Media>,
    var hasNextPage: Boolean,
) : Serializable {
    fun toChipList(): List<SearchChip> {
        val list = mutableListOf<SearchChip>()
        sort?.let {
            list.add(SearchChip("SORT", "Sort : $it"))
        }
        format?.let {
            list.add(SearchChip("FORMAT", "Format : $it"))
        }
        season?.let {
            list.add(SearchChip("SEASON", it))
        }
        seasonYear?.let {
            list.add(SearchChip("SEASON_YEAR", it.toString()))
        }
        genres?.forEach {
            list.add(SearchChip("GENRE", it))
        }
        excludedGenres?.forEach {
            list.add(SearchChip("EXCLUDED_GENRE", "Not $it"))
        }
        tags?.forEach {
            list.add(SearchChip("TAG", it))
        }
        excludedTags?.forEach {
            list.add(SearchChip("EXCLUDED_TAG", "Not $it"))
        }
        return list
    }

    fun removeChip(chip: SearchChip) {
        when (chip.type) {
            "SORT"           -> sort = null
            "FORMAT"         -> format = null
            "SEASON"         -> season = null
            "SEASON_YEAR"    -> seasonYear = null
            "GENRE"          -> genres?.remove(chip.text)
            "EXCLUDED_GENRE" -> excludedGenres?.remove(chip.text)
            "TAG"            -> tags?.remove(chip.text)
            "EXCLUDED_TAG"   -> excludedTags?.remove(chip.text)
        }
    }

    data class SearchChip(
        val type: String,
        val text: String
    )
}