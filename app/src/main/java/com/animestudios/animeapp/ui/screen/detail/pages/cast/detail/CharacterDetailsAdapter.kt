package com.animestudios.animeapp.ui.screen.detail.pages.cast.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.ItemCharacterDetailBinding
import com.animestudios.animeapp.media.Character
import com.animestudios.animeapp.others.SpoilerPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin

class CharacterDetailsAdapter(private val character: Character, private val activity: Activity) :
    RecyclerView.Adapter<CharacterDetailsAdapter.GenreViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = com.animestudios.animeapp.databinding.ItemCharacterDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val binding = holder.binding
        val desc =
            (if (character.age != "null") "__Age:__ " + character.age else "") +
                    (if (character.dateOfBirth.toString() != "") "\n__Birthday:__ " + character.dateOfBirth.toString() else "") +
                    (if (character.gender != "null") "\n__Gender:__ " + character.gender else "") + "\n" + character.description

        binding.characterDesc.isTextSelectable
        val markWon = Markwon.builder(activity).usePlugin(SoftBreakAddsNewLinePlugin.create()).usePlugin(
            SpoilerPlugin()
        ).build()
        markWon.setMarkdown(binding.characterDesc, desc)

    }

    override fun getItemCount(): Int = 1
    inner class GenreViewHolder(val binding: ItemCharacterDetailBinding) : RecyclerView.ViewHolder(binding.root)
}