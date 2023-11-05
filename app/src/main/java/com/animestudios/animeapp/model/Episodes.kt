package com.animestudios.animeapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Episodes(val title: String? = "", val thumbnail: String? = "") : Parcelable
