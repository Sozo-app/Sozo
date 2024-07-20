package com.animestudios.animeapp.ui.screen.detail.adapter

import com.animestudios.animeapp.parsers.ShowResponse
import com.animestudios.animeapp.ui.screen.detail.dialog.SourceSearchDialogFragment
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import kotlinx.coroutines.CoroutineScope

class AnimeSourceAdapter(
    sources: List<ShowResponse>,
    val model: DetailsViewModelImpl,
    val i: Int,
    val id: Int,
    fragment: SourceSearchDialogFragment,
    scope: CoroutineScope
) : SourceAdapter(sources, fragment, scope) {

    override suspend fun onItemClick(source: ShowResponse) {
        model.overrideEpisodes(i, source, id)
    }
}