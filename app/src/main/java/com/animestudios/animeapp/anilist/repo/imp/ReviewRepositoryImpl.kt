package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.ReviewRepository
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.type.ReviewSort
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(private val client: AniListClient) :
    ReviewRepository {
    override fun getReview(reviewSort: ReviewSort) = flow<List<Review>> {
        val reviewList = mutableListOf<Review>()
        val response = client.getReview(reviewSort).data?.Page?.reviews

        if (response != null) {
            reviewList.addAll(response.map { it.convert() })
            emit(reviewList)
        }


    }
}