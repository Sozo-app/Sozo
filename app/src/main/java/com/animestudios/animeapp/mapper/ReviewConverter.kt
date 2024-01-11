package com.animestudios.animeapp.mapper

import com.animestudios.animeapp.ReviewQuery
import com.animestudios.animeapp.model.*

fun ReviewQuery.Review?.convert(): Review {
    return Review(
        id = this?.id ?: 0,
        userId = this?.userId ?: 0,
        mediaId = this?.mediaId ?: 0,
        mediaType = this?.mediaType,
        summary = this?.summary.orEmpty(),
        body = this?.body.orEmpty(),
        rating = this?.rating ?: 0,
        ratingAmount = this?.ratingAmount ?: 0,
        score = this?.score ?: 0,
        user = User(
            id = this?.user?.id ?: 0,
            name = this?.user?.name.orEmpty(),
            avatar = UserAvatar(
                this?.user?.avatar?.large.orEmpty(),
                this?.user?.avatar?.medium.orEmpty()
            )
        ),
        aniListMedia = AniListMedia(
            idAniList = this?.media?.homeMedia?.id ?: 0,
            title = MediaTitle(userPreferred = this?.media?.homeMedia?.title?.userPreferred.orEmpty()),
            bannerImage = this?.media?.homeMedia?.bannerImage.orEmpty(),
            coverImage = MediaCoverImage(
                large = this?.media?.homeMedia?.coverImage?.large.orEmpty(),
                medium = this?.media?.homeMedia?.coverImage?.medium.orEmpty()
            )
        )
    )
}
