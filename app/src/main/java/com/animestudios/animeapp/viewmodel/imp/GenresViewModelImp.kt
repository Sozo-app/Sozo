package com.animestudios.animeapp.viewmodel.imp

import androidx.lifecycle.ViewModel
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.api.imp.AniListQueriesImp
import com.animestudios.animeapp.anilist.repo.imp.AniListRepositoryImp
import com.animestudios.animeapp.viewmodel.GenresViewModel

class GenresViewModelImp : ViewModel(),GenresViewModel {
    val query = AniListRepositoryImp()

    override var doneListener: (() -> Unit)? = null
    var genres: MutableMap<String, String>? = null
    var done = false
    override suspend fun loadGenres(
        genre: ArrayList<String>,
        listener: (Pair<String, String>) -> Unit
    ) {
        if (genres == null) {
            genres = mutableMapOf()
            query.getGenres(genre)  {
                genres!![it.first] = it.second
                listener.invoke(it)
                if (genres!!.size == genre.size) {
                    done = true
                    doneListener?.invoke()
                }
            }
        }
    }
}