package com.animestudios.animeapp.parsers.extractor

import com.animestudios.animeapp.parsers.VideoContainer
import com.animestudios.animeapp.parsers.VideoExtractor
import com.animestudios.animeapp.parsers.VideoServer

class HiAnimeExtractor(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        TODO("Not yet implemented")
    }
}