package com.animestudios.animeapp.ui.screen.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.ThemeItemBinding
import com.animestudios.animeapp.model.ThemeModel

class ThemeAdapter : RecyclerView.Adapter<ThemeAdapter.ThemeVh>() {
    var list: ArrayList<ThemeModel> = ArrayList()

    inner class ThemeVh(val binding: ThemeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: ThemeModel) {
            binding.apply {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeVh {
        return ThemeVh(ThemeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ThemeVh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}