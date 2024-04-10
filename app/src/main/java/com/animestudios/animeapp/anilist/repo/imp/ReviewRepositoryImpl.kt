package com.animestudios.animeapp.anilist.repo.imp

import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.anilist.repo.ReviewRepository
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.type.ReviewSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(private val client: AniListClient) :
    ReviewRepository {

    override fun getReview(reviewSort: ReviewSort): Flow<List<Review>> = flow {
        val reviewList = mutableListOf<Review>()
        val response = client.getReview(reviewSort)
        if (!response.hasErrors()) {
            reviewList.addAll(response.data?.Page?.reviews?.map { it.convert() } ?: emptyList())
        } else {
            emit(emptyList())
            throw IOException("Failed to fetch reviews. Errors: ${response.errors}")
        }
        emit(reviewList)
    }.catch { cause ->
        emit(emptyList())
    }
}
