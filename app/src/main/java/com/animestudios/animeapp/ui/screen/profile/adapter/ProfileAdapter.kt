package com.animestudios.animeapp.ui.screen.profile.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.ProfileCategoryItemBinding
import com.animestudios.animeapp.model.ProfileCategoryModel

class ProfileAdapter : RecyclerView.Adapter<ProfileAdapter.ProfileVh>() {
    private val list =ArrayList<ProfileCategoryModel>()


    inner class ProfileVh(val binding: ProfileCategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            binding.apply {
                binding.shapeableImageView.setImageResource()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileVh {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ProfileVh, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

}