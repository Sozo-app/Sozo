    package com.animestudios.animeapp.anilist.response

    import kotlinx.serialization.SerialName
    import kotlinx.serialization.Serializable

    class Query{
        @Serializable
        data class Viewer(
            @SerialName("data")
            val data : Data?
        ){
            @Serializable
            data class Data(
                @SerialName("Viewer")
                val user: com.animestudios.animeapp.anilist.response.User?
            )
        }
        @Serializable
        data class Media(
            @SerialName("data")
            val data :  Data?
        ){
            @Serializable
            data class Data(
                @SerialName("Media")
                val media: com.animestudios.animeapp.anilist.response.Media?
            )
        }

        @Serializable
        data class Page(
            @SerialName("data")
            val data : Data?
        ){
            @Serializable
            data class Data(
                @SerialName("Page")
                val page : com.animestudios.animeapp.anilist.response.Page?
            )
        }
    //    data class AiringSchedule(
    //        val data : Data?
    //    ){
    //        data class Data(
    //            val AiringSchedule: com.animestudios.animeapp.anilist.response.AiringSchedule?
    //        )
    //    }

        @Serializable
        data class Character(
            @SerialName("data")
            val data :  Data?
        ){

            @Serializable
            data class Data(
                @SerialName("Character")
                val character: com.animestudios.animeapp.anilist.response.Character?
            )
        }

        @Serializable
        data class Studio(
            @SerialName("data")
            val data: Data?
        ){
            @Serializable
            data class Data(
                @SerialName("Studio")
                val studio: com.animestudios.animeapp.anilist.response.Studio?
            )
        }

    //    data class MediaList(
    //        val data: Data?
    //    ){
    //        data class Data(
    //            val MediaList: com.animestudios.animeapp.anilist.response.MediaList?
    //        )
    //    }

        @Serializable
        data class MediaListCollection(
            @SerialName("data")
            val data : Data?
        ){
            @Serializable
            data class Data(
                @SerialName("MediaListCollection")
                val mediaListCollection: com.animestudios.animeapp.anilist.response.MediaListCollection?
            )
        }

        @Serializable
        data class GenreCollection(
            @SerialName("data")
            val data: Data
        ){
            @Serializable
            data class Data(
                @SerialName("GenreCollection")
                val genreCollection: List<String>?
            )
        }

        @Serializable
        data class MediaTagCollection(
            @SerialName("data")
            val data: Data
        ){
            @Serializable
            data class Data(
                @SerialName("MediaTagCollection")
                val mediaTagCollection: List<MediaTag>?
            )
        }

        @Serializable
        data class User(
            @SerialName("data")
            val data: Data
        ){
            @Serializable
            data class Data(
                @SerialName("User")
                val user: com.animestudios.animeapp.anilist.response.User?
            )
        }
    }



    //data class WhaData(
    //    val Studio: Studio?,
    //
    //    // Follow query
    //    val Following: User?,
    //
    //    // Follow query
    //    val Follower: User?,
    //
    //    // Thread query
    //    val Thread: Thread?,
    //
    //    // Recommendation query
    //    val Recommendation: Recommendation?,
    //
    //    // Like query
    //    val Like: User?,

    //    // Review query
    //    val Review: Review?,
    //
    //    // Activity query
    //    val Activity: ActivityUnion?,
    //
    //    // Activity reply query
    //    val ActivityReply: ActivityReply?,

    //    // Comment query
    //    val ThreadComment: List<ThreadComment>?,

    //    // Notification query
    //    val Notification: NotificationUnion?,

    //    // Media Trend query
    //    val MediaTrend: MediaTrend?,

    //    // Provide AniList markdown to be converted to html (Requires auth)
    //    val Markdown: ParsedMarkdown?,

    //    // SiteStatistics: SiteStatistics
    //    val AniChartUser: AniChartUser?,
    //)
