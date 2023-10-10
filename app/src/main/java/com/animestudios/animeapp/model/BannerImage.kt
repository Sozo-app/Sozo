package com.animestudios.animeapp.model

import java.io.Serializable

data class BannerImage(
    val urlImg: String?,
    val title: String?,
    val description:String,
    var time: Long,
) : Serializable {
    fun checkTime(): Boolean {
        return (System.currentTimeMillis() - time) >= (1000 * 60 * 60 * 6)
    }
}