package com.animestudios.animeapp.model

sealed interface NotificationType {
    object Airing : NotificationType
    data class Activity(val user: User) : NotificationType
    object Threads : NotificationType
    object Unknown : NotificationType
    data class Following(val user: User) : NotificationType
}
