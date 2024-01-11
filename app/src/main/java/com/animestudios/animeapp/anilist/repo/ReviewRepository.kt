package com.animestudios.animeapp.anilist.repo

import com.animestudios.animeapp.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReview(perPage: Int, page: Int): Flow<List<Review>>
}