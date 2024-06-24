package com.animestudios.animeapp.ui.screen.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.RelationItemBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.model.RelationData
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.settings.UISettings

class RelationsAdapter : RecyclerView.Adapter<RelationsAdapter.RelationVh>() {

    private  lateinit var itemCLickListener:(RelationData)->Unit
    fun setItemClickListener(listener:(RelationData)->Unit){
        itemCLickListener=listener
    }

    private val list = ArrayList<RelationData>()

    inner class RelationVh(val binding: RelationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: RelationData) {
            binding.apply {

                setAnimation(
                    binding.root.context,
                    binding.root,
                    readData<UISettings>("ui_settings") ?: UISettings()
                )
                binding.root.setOnClickListener {
                    itemCLickListener.invoke(data)
                }
                binding.itemImg.loadImage(data.coverImage.large)
                binding.titleItem.text = data.title.userPreferred
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationVh {
        return RelationVh(
            RelationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun submitList(newList: ArrayList<RelationData>) {
        list.clear()
        list.addAll(newList)
    }

    override fun onBindViewHolder(holder: RelationVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}