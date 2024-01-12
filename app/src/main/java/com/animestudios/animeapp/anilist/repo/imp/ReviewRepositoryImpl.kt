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
        val response = client.getReview(reviewSort)

        if (!response.hasErrors()) {
            reviewList.addAll(response.data?.Page?.reviews!!.map { it.convert() })
            emit(reviewList)
        } else {
            val response2 = client.getReview(reviewSort)
            reviewList.addAll(response2.data?.Page?.reviews!!.map { it.convert() })
            emit(reviewList)
        }


    }
}