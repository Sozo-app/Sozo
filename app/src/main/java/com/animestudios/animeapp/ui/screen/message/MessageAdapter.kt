package com.animestudios.animeapp.ui.screen.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.ItemRecipientMessageBinding
import com.animestudios.animeapp.databinding.ItemSenderMessageBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.model.ChatMessage
import com.animestudios.animeapp.model.Message
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.tools.BaseViewHolder
import com.animestudios.animeapp.ui.screen.anime.AnimePageAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val currentUserId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    fun submitList(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].fromId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemSenderMessageBinding.inflate(inflater, parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemRecipientMessageBinding.inflate(inflater, parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is SentViewHolder -> holder.bind(msg)
            is ReceivedViewHolder -> holder.bind(msg)
        }
    }

    // --- Sent messages (right side) ---
    inner class SentViewHolder(
        private val binding: ItemSenderMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(msg: ChatMessage) = binding.run {
            messageTextView.text = msg.text
            timeTextView.text = msg.timestamp.toDateTime()
//            snackString(msg.isRead.toString())
            if (msg.isRead) {
                val tint = ContextCompat.getColor(root.context, R.color.basic_color)
                readReceiptView.setColorFilter(tint)
                readReceiptView.setImageResource(R.drawable.ic_done_all)
            }else {
                readReceiptView.setColorFilter(null)
                readReceiptView.setImageResource(R.drawable.ic_unread)
            }
        }
    }

    inner class ReceivedViewHolder(
        private val binding: ItemRecipientMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(msg: ChatMessage) = binding.run {
            avatarImageView.loadImage(msg.senderAvatar)
            usernameTxt.text = msg.senderName
            messageTextView.text = msg.text
            binding.timeTextView.text = msg.timestamp.toDateTime()
        }
    }

    // --- Timestamp formatting ---
    private fun Long.toDateTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(this))
    }
}
