package com.animestudios.animeapp.model

import com.animestudios.animeapp.GetRelationByIdQuery
import com.animestudios.animeapp.anilist.response.MediaCoverImage

data class RelationData(val id: Int, val title: GetRelationByIdQuery.Title, val rank: Int,val coverImage: GetRelationByIdQuery.CoverImage,)