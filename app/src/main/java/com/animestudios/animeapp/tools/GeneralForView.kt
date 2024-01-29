package com.animestudios.animeapp.tools

import com.animestudios.animeapp.type.MediaListStatus

fun MediaListStatus.getColor(): String {
    return when (this) {
        MediaListStatus.CURRENT -> "#68D639"
        MediaListStatus.PLANNING -> "#02A9FF"
        MediaListStatus.COMPLETED, MediaListStatus.REPEATING -> "#9256F3"
        MediaListStatus.DROPPED -> "#F779A4"
        MediaListStatus.PAUSED -> "#E85D75"
        else -> "#68D639"
    }
}
