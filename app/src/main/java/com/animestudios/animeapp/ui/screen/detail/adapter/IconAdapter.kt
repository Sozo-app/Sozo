package com.animestudios.animeapp.ui.screen.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.IconItemBinding

class IconAdapter() : RecyclerView.Adapter<IconAdapter.IconVh>() {
    var list = ArrayList<Int>()

    inner class IconVh(var binding: IconItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(@DrawableRes id: Int) {
            binding.apply {
                root.setImageResource(id)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconVh {
        return IconVh(IconItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: IconVh, position: Int) {
        holder.onBind(list.get(position))
    }


    fun submitList(newList: ArrayList<Int>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}