package com.animestudios.animeapp.tools

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.model.Message

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: Message)
}