package com.animestudios.animeapp.anilist.response

// 1) Umumiy ota-klass (sealed class) - hamma Notification typelari shundan meros oladi
sealed class AniNotification(
    open val id: Int,
    open val type: String,       // "FOLLOWING", "AIRING", "ACTIVITY_LIKE", ...
    open val createdAt: Long,    // Epoch time / Date Time
    open val context: String?    // Ba'zi typelarda bo'lishi mumkin, ba'zi typelarda bo'lmasligi mumkin
)

// 2) User model - universal bo‘lishi uchun alohida data class
data class AniUser(
    val id: Int,
    val name: String,
    val avatar: AniAvatar
)

// 3) Avatar model
data class AniAvatar(
    val large: String?,
    val medium: String?
)

// 4) Media model - query’dagi “...HomeMedia” qismini ifodalash uchun soddalashtirilgan misol
data class AniHomeMedia(
    val id: Int,
    val title: String?,          // soddalashtirib title: String? qildim
    val coverImage: String?      // soddalashtirib coverImage: String? qildim
    // ... o'zingizga kerakli boshqa maydonlar ...
)

// 5) Activity model - “activity” qismi uchun
sealed class AniActivity(
    open val id: Int,
    open val type: String,       // "LIST", "MESSAGE", "TEXT"
    open val createdAt: Long
) {
    data class ListActivity(
        override val id: Int,
        override val type: String,
        override val createdAt: Long,
        val status: String?,      // "Completed", "Watching", ...
        val progress: String?,    // "1", "Episode 2", ...
        val media: AniHomeMedia?
    ) : AniActivity(id, type, createdAt)

    data class MessageActivity(
        override val id: Int,
        override val type: String,
        override val createdAt: Long,
        val message: String?,
        val recipient: AniUser?
    ) : AniActivity(id, type, createdAt)

    data class TextActivity(
        override val id: Int,
        override val type: String,
        override val createdAt: Long,
        val text: String?
    ) : AniActivity(id, type, createdAt)
}

// 6) Endi turli xildagi Notification model’larini sealed class ichida branch qilamiz:

// FollowingNotification
data class FollowingNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,
    override val context: String?,
    val userId: Int,
    val user: AniUser
) : AniNotification(id, type, createdAt, context)

// AiringNotification
data class AiringNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,

    override val context: String?, // AiringNotification’da konteksti bo‘lmasa, null yuborish mumkin
    val episode: Int,
    val contexts: List<String?>,
    val media: AniHomeMedia
) : AniNotification(id, type, createdAt, context)

// ActivityLikeNotification
data class ActivityLikeNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,
    override val context: String?,
    val userId: Int,
    val user: AniUser,
    val activity: AniActivity
) : AniNotification(id, type, createdAt, context)

// ActivityMessageNotification
data class ActivityMessageNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,
    override val context: String?,
    val userId: Int,
    val message: String,
    val user: AniUser
) : AniNotification(id, type, createdAt, context)

// ActivityMentionNotification
data class ActivityMentionNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,
    override val context: String?,
    val userId: Int,
    val user: AniUser
) : AniNotification(id, type, createdAt, context)

// ActivityReplyNotification
data class ActivityReplyNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,
    override val context: String?,
    val userId: Int,
    val user: AniUser
) : AniNotification(id, type, createdAt, context)

// ThreadCommentMentionNotification
data class ThreadCommentMentionNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,
    override val context: String?,
    val userId: Int,
    val user: AniUser
) : AniNotification(id, type, createdAt, context)

// ThreadCommentReplyNotification
data class ThreadCommentReplyNotification(
    override val id: Int,
    override val type: String,
    override val createdAt: Long,
    override val context: String?,
    val userId: Int,
    val user: AniUser
) : AniNotification(id, type, createdAt, context)
