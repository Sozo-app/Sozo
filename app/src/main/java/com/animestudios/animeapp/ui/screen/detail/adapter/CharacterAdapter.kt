package com.animestudios.animeapp.ui.screen.detail.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.CharacterItemBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.media.Character
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.ui.screen.detail.pages.cast.CastScreen
import com.animestudios.animeapp.ui.screen.detail.pages.cast.detail.CastDetailScreen
import java.io.Serializable


class CharacterAdapter(
    private val fragment: CastScreen,
    private val list: ArrayList<Character>
) : RecyclerView.Adapter<CharacterAdapter.CharacterVh>() {
    inner class CharacterVh(val binding: CharacterItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(character: Character) {
            binding.apply {

                title.text = character.name
                titleRomanji.text = character.native
                textView3.text = character.role
                characterPic.loadImage(character.image)
                root.setOnClickListener {
                    val char = list[bindingAdapterPosition]
                    binding.root.context.startActivity(
                        Intent(
                            binding.root.context,
                            CastDetailScreen::class.java
                        ).putExtra("character", char as Serializable),

                    ) }
                setAnimation(fragment.requireContext(), binding.root, fragment.uiSettings)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterVh {
        return CharacterVh(
            CharacterItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CharacterVh, position: Int) {
        holder.onBind(list.get((position)))
    }

    override fun getItemCount(): Int {
        return list.size

    }
}