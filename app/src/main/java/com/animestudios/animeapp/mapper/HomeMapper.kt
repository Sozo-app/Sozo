package com.animestudios.animeapp.mapper

import com.animestudios.animeapp.EMPTY_VALUE_STRING

class HomeMapper {
}
fun Int.formatDurationForItem(): String {
    return if (this != null) {
        val hours = this / 60
        val minutes = this % 60
        if (hours > 0) {
            "$hours : $minutes"
        } else if (minutes == 0) {
            "$EMPTY_VALUE_STRING "
        } else {
            "$minutes"
        }
    } else {
        EMPTY_VALUE_STRING
    }
}

fun Int.formatDuration(): String {
    return if (this != null) {
        val hours = this / 60
        val minutes = this % 60
        if (hours > 0) {
            "$hours hr $minutes minute"
        } else if (minutes == 0) {
            "$EMPTY_VALUE_STRING "
        } else {
            "$minutes minute"
        }
    } else {
        EMPTY_VALUE_STRING
    }
}
