package com.animestudios.animeapp.ui.screen.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.ProfileCategoryItemBinding
import com.animestudios.animeapp.model.ProfileCategoryModel

class ProfileAdapter : RecyclerView.Adapter<ProfileAdapter.ProfileVh>() {
    private val list = ArrayList<ProfileCategoryModel>()


    inner class ProfileVh(val binding: ProfileCategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: ProfileCategoryModel) {
            binding.apply {
                binding.shapeableImageView.setImageResource(data.image)
                binding.textView5.text = data.title
                binding.description.text = data.description
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileVh {
        return ProfileVh(
            ProfileCategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun submitList(newList: ArrayList<ProfileCategoryModel>) {
        list.clear()
        list.addAll(newList)
    }

    override fun onBindViewHolder(holder: ProfileVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }

}