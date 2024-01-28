package com.animestudios.animeapp.ui.screen.profile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.ThemeItemBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.model.ThemeModel
import com.animestudios.animeapp.selectedPosition
import com.animestudios.animeapp.visible

class ThemeAdapter : RecyclerView.Adapter<ThemeAdapter.ThemeVh>() {
    var list: ArrayList<ThemeModel> = ArrayList()
    lateinit var listener: (ThemeModel, Int) -> Unit

    fun setItemClickListener(listener: (ThemeModel, Int) -> Unit) {
        this.listener = listener
    }

    inner class ThemeVh(val binding: ThemeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: ThemeModel) {
            binding.apply {
                val theme = binding.root.context.getSharedPreferences("Sozo", Context.MODE_PRIVATE)
                    .getString("theme", "RED")!!

                if (data.title.equals(theme)) {
                    binding.selectedImg.visible()
                    selectedImg.setColorFilter(
                        binding.root.context.getResources().getColor(data.colorControlNormal)
                    );
                } else {
                    binding.selectedImg.gone()
                }

                binding.root.setOnClickListener {
                        listener.invoke(data, absoluteAdapterPosition)
                }

                binding.itemBg.setBackgroundColor(binding.root.context.getColor(data.colorControlNormal))
                binding.pieceTitle.setTextColor(binding.root.context.getColor(data.colorPrimary))
                binding.piece1.setCardBackgroundColor(binding.root.context.getColor(data.colorPrimary))
                binding.piece2.setCardBackgroundColor(binding.root.context.getColor(data.colorPrimary))
                binding.piece3.setCardBackgroundColor(binding.root.context.getColor(data.colorPrimary))
                binding.piece4.setCardBackgroundColor(binding.root.context.getColor(data.colorPrimary))
                binding.itemCard.setCardBackgroundColor(binding.root.context.getColor(data.colorPrimary))
                binding.itemBg.strokeColor = binding.root.context.getColor(data.colorPrimary)
                binding.bigPiece.setCardBackgroundColor(binding.root.context.getColor(data.colorControlNormal))
                binding.bigPiece2.setCardBackgroundColor(binding.root.context.getColor(data.colorControlNormal))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeAdapter.ThemeVh {
        return ThemeVh(ThemeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ThemeVh, position: Int) {
        holder.onBind(list[position])
    }

    fun submitList(newList: ArrayList<ThemeModel>) {
        list.clear()
        list.addAll(newList)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}