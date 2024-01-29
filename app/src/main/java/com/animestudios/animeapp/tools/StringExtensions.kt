package com.animestudios.animeapp.tools

import com.animestudios.animeapp.type.MediaListStatus
import com.animestudios.animeapp.type.MediaType
import java.util.*


fun String.convertFromSnakeCase(toUpper: Boolean = false): String {
    val splitText = this.split("_")
    val jointText = splitText.joinToString(" ") {
        it.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    return if (toUpper) jointText.uppercase() else jointText
}


fun MediaListStatus.getString(mediaType: MediaType): String {
    return when (this) {
        MediaListStatus.CURRENT -> when (mediaType) {
            MediaType.ANIME -> "Watching"
            MediaType.MANGA -> "Reading"
            MediaType.UNKNOWN__ -> TODO()
        }
        MediaListStatus.REPEATING -> when (mediaType) {
            MediaType.ANIME -> "Rewatching"
            MediaType.MANGA -> "Rereading"
            MediaType.UNKNOWN__ -> TODO()
        }
        MediaListStatus.COMPLETED -> "Completed"
        MediaListStatus.PAUSED -> "Paused"
        MediaListStatus.DROPPED -> "Dropped"
        MediaListStatus.PLANNING -> "Planning"
        else -> this.name.convertFromSnakeCase()
    }
}
