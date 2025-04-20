package com.animestudios.animeapp.ui.screen.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.ChatListItemBinding
import com.animestudios.animeapp.model.ChatListItem
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val onClick: (ChatListItem) -> Unit
) : ListAdapter<ChatListItem, ChatListAdapter.ChatVH>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatVH(
        ChatListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatVH(private val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun Long.toTimeString(): String {
            val nowCal = Calendar.getInstance()
            val msgCal = Calendar.getInstance().apply { timeInMillis = this@toTimeString }
            return if (nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
                && nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
            } else {
                SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(this))
            }
        }

        fun bind(item: ChatListItem) {
            binding.textName.text = item.otherName
            binding.textLastMessage.text = item.lastText
            binding.textTime.text =
                if (item.otherOnline) "Online" else item.lastTimestamp.toTimeString()
            binding.verifedBadge.isVisible =item.otherUserId == 6136028
            binding.unreadBadgeContainer.isVisible = !item.lastWasRead
            binding.unreadBadge.text = if (!item.lastWasRead) "1" else ""

            binding.viewOnline.isVisible = item.otherOnline

            Glide.with(binding.imgAvatar)
                .load(item.otherAvatar)
                .circleCrop()
                .into(binding.imgAvatar)

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ChatListItem>() {
            override fun areItemsTheSame(a: ChatListItem, b: ChatListItem) = a.chatId == b.chatId
            override fun areContentsTheSame(a: ChatListItem, b: ChatListItem) = a == b
        }
    }
}
