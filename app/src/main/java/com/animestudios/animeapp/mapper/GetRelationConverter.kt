package com.animestudios.animeapp.mapper

import com.animestudios.animeapp.GetRelationByIdQuery
import com.animestudios.animeapp.model.RelationData

fun GetRelationByIdQuery.Data.convert():ArrayList<RelationData>{
    val list =ArrayList<RelationData>()
    this.Media?.relations?.nodes?.onEach {
        list.add(RelationData(it?.id?:0, title = it?.title!!, rank = it?.averageScore?:0, coverImage = it.coverImage!!))
    }
    return list
}