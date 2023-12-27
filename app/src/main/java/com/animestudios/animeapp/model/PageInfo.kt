package com.animestudios.animeapp.model

data class PageInfo(
    val total: Int = 0,
    val perPage: Int = 0,
    val currentPage: Int = 0,
    val lastPage: Int = 0,
    val hasNextPage: Boolean = false
)