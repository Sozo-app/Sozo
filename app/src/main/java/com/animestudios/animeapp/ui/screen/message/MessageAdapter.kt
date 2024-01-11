package com.animestudios.animeapp.ui.screen.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.ItemRecipientMessageBinding
import com.animestudios.animeapp.databinding.ItemSenderMessageBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.model.Message
import com.animestudios.animeapp.tools.BaseViewHolder


///Add MEssage Screen
class MessageAdapter(
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false,
    private val list: List<Message>
) : RecyclerView.Adapter<BaseViewHolder>() {

    inner class RecipientViewHolder(val binding: ItemRecipientMessageBinding) :
        BaseViewHolder(binding.root) {
        init {
//            itemView.setSafeOnClickListener { clicked(bindingAdapterPosition) }
//            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }


        override fun bind(item: Message) {
            binding.apply {
                messageTextView.text = item.message
                messageTimeTextView.text = item.convertUnixTimeToFormattedTime()
                avatarImageView.loadImage(item.recipient.avatarMedium)
                usernameTextView.text = item.recipient.name

            }
        }
    }


    inner class SenderViewHolder(val binding: ItemSenderMessageBinding) :
        BaseViewHolder(binding.root) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
//            itemView.setSafeOnClickListener { clicked(bindingAdapterPosition) }
//            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }


        override fun bind(item: Message) {
            binding.apply {
                messageTextView.text = item.message
                usernameTextView.text = item.messenger.name
                messageTimeTextView.text = item.convertUnixTimeToFormattedTime()
                binding.avatarImageView.loadImage(item.messenger.avatarMedium)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            1 -> {
                RecipientViewHolder(
                    ItemRecipientMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                SenderViewHolder(
                    ItemSenderMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        println(list.get(position).recipient.id)
        return if (list.get(position).recipient.id == Anilist.userid) {
            1
        } else {
            2
        }
    }

}