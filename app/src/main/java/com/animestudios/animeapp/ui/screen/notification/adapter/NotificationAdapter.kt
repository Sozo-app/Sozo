package com.animestudios.animeapp.ui.screen.notification.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.response.ActivityLikeNotification
import com.animestudios.animeapp.anilist.response.ActivityMentionNotification
import com.animestudios.animeapp.anilist.response.ActivityMessageNotification
import com.animestudios.animeapp.anilist.response.ActivityReplyNotification
import com.animestudios.animeapp.anilist.response.AiringNotification
import com.animestudios.animeapp.anilist.response.AniNotification
import com.animestudios.animeapp.anilist.response.FollowingNotification
import com.animestudios.animeapp.anilist.response.ThreadCommentMentionNotification
import com.animestudios.animeapp.anilist.response.ThreadCommentReplyNotification
import com.animestudios.animeapp.databinding.ItemActivityLikeNotificationBinding
import com.animestudios.animeapp.databinding.ItemActivityMentionNotificationBinding
import com.animestudios.animeapp.databinding.ItemActivityMessageNotificationBinding
import com.animestudios.animeapp.databinding.ItemAiringNotificationBinding
import com.animestudios.animeapp.databinding.ItemFollowingNotificationBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.setColoredName
import com.animestudios.animeapp.setColoredNameFromImage
import com.apollographql.apollo3.api.not
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val notifications = mutableListOf<AniNotification>()

    companion object {
        private const val VIEW_TYPE_FOLLOWING = 0
        private const val VIEW_TYPE_AIRING = 1
        private const val VIEW_TYPE_LIKE = 2
        private const val VIEW_TYPE_MESSAGE = 3
        private const val VIEW_TYPE_MENTION = 4
        private const val VIEW_TYPE_REPLY = 5
        private const val VIEW_TYPE_THREAD_MENTION = 6
        private const val VIEW_TYPE_THREAD_REPLY = 7
    }

    fun setItems(newItems: List<AniNotification>) {
        notifications.clear()
        notifications.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setItemsForPaging(newItems: List<AniNotification>) {
        notifications.addAll(newItems)
        notifyItemInserted(notifications.size - newItems.size)
    }

    override fun getItemCount(): Int = notifications.size

    override fun getItemViewType(position: Int): Int {
        return when (notifications[position]) {
            is FollowingNotification -> VIEW_TYPE_FOLLOWING
            is AiringNotification -> VIEW_TYPE_AIRING
            is ActivityLikeNotification -> VIEW_TYPE_LIKE
            is ActivityMessageNotification -> VIEW_TYPE_MESSAGE
            is ActivityMentionNotification -> VIEW_TYPE_MENTION
            is ActivityReplyNotification -> VIEW_TYPE_REPLY
            is ThreadCommentMentionNotification -> VIEW_TYPE_THREAD_MENTION
            is ThreadCommentReplyNotification -> VIEW_TYPE_THREAD_REPLY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_FOLLOWING -> {
                val binding = ItemFollowingNotificationBinding.inflate(inflater, parent, false)
                FollowingViewHolder(binding)
            }

            VIEW_TYPE_AIRING -> {
                val binding = ItemAiringNotificationBinding.inflate(inflater, parent, false)
                AiringViewHolder(binding)
            }

            VIEW_TYPE_LIKE -> {
                val binding = ItemActivityLikeNotificationBinding.inflate(inflater, parent, false)
                ActivityLikeViewHolder(binding)
            }

            VIEW_TYPE_MESSAGE -> {
                val binding =
                    ItemActivityMessageNotificationBinding.inflate(inflater, parent, false)
                ActivityMessageViewHolder(binding)
            }

            VIEW_TYPE_MENTION -> {
                val binding =
                    ItemActivityMentionNotificationBinding.inflate(inflater, parent, false)
                ActivityMentionViewHolder(binding)
            }

            VIEW_TYPE_REPLY -> {
                val binding =
                    ItemActivityMentionNotificationBinding.inflate(inflater, parent, false)
                ActivityReplyViewHolder(binding)
            }

            VIEW_TYPE_THREAD_MENTION -> {
                val binding =
                    ItemActivityMentionNotificationBinding.inflate(inflater, parent, false)
                ThreadCommentMentionViewHolder(binding)
            }

            VIEW_TYPE_THREAD_REPLY -> {
                val binding =
                    ItemActivityMentionNotificationBinding.inflate(inflater, parent, false)
                ThreadCommentReplyViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = notifications[position]
        when (holder) {
            is FollowingViewHolder -> holder.bind(item as FollowingNotification)
            is AiringViewHolder -> holder.bind(item as AiringNotification)
            is ActivityLikeViewHolder -> holder.bind(item as ActivityLikeNotification)
            is ActivityMessageViewHolder -> holder.bind(item as ActivityMessageNotification)
            is ActivityMentionViewHolder -> holder.bind(item as ActivityMentionNotification)
            is ActivityReplyViewHolder -> holder.bind(item as ActivityReplyNotification)
            is ThreadCommentMentionViewHolder -> holder.bind(item as ThreadCommentMentionNotification)
            is ThreadCommentReplyViewHolder -> holder.bind(item as ThreadCommentReplyNotification)
        }
    }

    inner class FollowingViewHolder(
        private val binding: ItemFollowingNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: FollowingNotification) {
            binding.imgCover.loadImage(notification.user.avatar.medium)
            binding.tvNotificationTitle.text = "${notification.user.name} followed you"
            binding.tvNotificationTitle.setColoredName(
                notification.user.name,
                nameColorRes = R.color.md_theme_dark_7_primary
            )
        }
    }

    inner class AiringViewHolder(
        private val binding: ItemAiringNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AiringNotification) {
            binding.tvTime.text = notification.createdAt.toDateTime()
            binding.tvTitle.text = notification.media.title
            binding.imgCover.loadImage(notification.media.coverImage)
            binding.tvSubtitle.text =
                "Episode ${notification.episode} of ${notification.media.title} just aired"
        }
    }

    fun Long.toDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = Date(this)
        return sdf.format(date)
    }

    inner class ActivityLikeViewHolder(
        private val binding: ItemActivityLikeNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ActivityLikeNotification) {
            binding.imgUserAvatar.loadImage(notification.user.avatar.large)
            binding.tvLikeInfo.text = "${notification.user.name} liked your activity"
        }
    }

    // 4) ActivityMessage
    inner class ActivityMessageViewHolder(
        private val binding: ItemActivityMessageNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ActivityMessageNotification) {
            binding.imgUserAvatar.loadImage(notification.user.avatar.large)
            binding.tvMessageInfo.text =
                "${notification.user.name} sent you a message"
        }
    }

    inner class ActivityMentionViewHolder(
        private val binding: ItemActivityMentionNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ActivityMentionNotification) {
            binding.imgUserAvatar.loadImage(notification.user.avatar.large)
            binding.tvMentionInfo.text =
                "${notification.user.name} mentioned you: ${notification.context}"
        }
    }

    inner class ActivityReplyViewHolder(
        private val binding: ItemActivityMentionNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ActivityReplyNotification) {
            binding.imgUserAvatar.loadImage(notification.user.avatar.large)
            binding.tvMentionInfo.text =
                "${notification.user.name} replied to you: ${notification.context}"
        }
    }

    inner class ThreadCommentMentionViewHolder(
        private val binding: ItemActivityMentionNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ThreadCommentMentionNotification) {
            binding.imgUserAvatar.loadImage(notification.user.avatar.large)
            binding.tvMentionInfo.text =
                "${notification.user.name} mentioned you in a thread\n${notification.context}"
        }
    }

    inner class ThreadCommentReplyViewHolder(
        private val binding: ItemActivityMentionNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ThreadCommentReplyNotification) {
            binding.imgUserAvatar.loadImage(notification.user.avatar.large)
            binding.tvMentionInfo.text =
                "${notification.user.name} replied in a thread\n${notification.context}"
        }
    }
}
