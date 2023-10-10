package com.animestudios.animeapp.viewmodel

interface GenresViewModel {
    var doneListener: (() -> Unit)?
    suspend fun loadGenres(genre: ArrayList<String>, listener: (Pair<String, String>) -> Unit)
}