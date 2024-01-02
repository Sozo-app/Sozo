package com.animestudios.animeapp.ui.screen.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.CastPageItemBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.media.Character
import com.animestudios.animeapp.ui.screen.detail.pages.cast.CastScreen
import com.animestudios.animeapp.visible

class CastPageAdapter(
    private val fragment: CastScreen,
    private val list: ArrayList<Character>,
    private val supportList: ArrayList<Character>
) : RecyclerView.Adapter<CastPageAdapter.CastVh>() {
    inner class CastVh(val binding: CastPageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            binding.apply {
                if (
                    absoluteAdapterPosition == 0
                ) {
                    binding.textView2.text = "Main Characters"
                    val characterAdapter = CharacterAdapter(fragment, list!!)
                    binding.characterRv.adapter = characterAdapter
                } else {
                    binding.textView2.text = "Supporting Characters"
                    if (supportList.isEmpty()) {
                        binding.textView2.gone()
                    }else{
                        binding.textView2.visible()
                    }
                    val characterAdapter = CharacterAdapter(fragment, supportList!!)
                    binding.characterRv.adapter = characterAdapter
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastVh {
        return CastVh(
            CastPageItemBinding.inflate(
                LayoutInflater.from(
                    parent
                        .context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CastVh, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return 2
    }
}