package com.animestudios.animeapp.anilist.repo

import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.type.ReviewSort
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReview(reviewSort: ReviewSort): Flow<List<Review>>
}