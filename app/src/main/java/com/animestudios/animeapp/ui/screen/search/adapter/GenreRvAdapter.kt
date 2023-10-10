package com.animestudios.animeapp.ui.screen.search.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.animestudios.animeapp.databinding.ItemGenreForSearchBinding
import com.animestudios.animeapp.model.Genre
import com.animestudios.animeapp.model.getHexColor
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.setSafeOnClickListener
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.BaseRecyclerViewAdapter

class GenreRvAdapter(
    private val context: Context,
    list: List<Genre>,
    private val listener: GenreListener? = null
) : BaseRecyclerViewAdapter<Genre, ItemGenreForSearchBinding>(list) {
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemGenreForSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ItemGenreForSearchBinding) :
        ViewHolder(binding) {
        override fun bind(item: Genre, index: Int) {
            binding.genreText.text = item.name
            binding.genreCard.setCardBackgroundColor(Color.parseColor(item.getHexColor()))
            binding.genreCard.setStrokeColor(Color.parseColor(item.getHexColor()))
            binding.genreCard.strokeWidth=0
            binding.genreCard.radius=8f
            binding.genreCard.isEnabled = listener != null
            binding.genreCard.setSafeOnClickListener {
                listener?.getGenre(item)
            }
        }
    }

    interface GenreListener {
        fun getGenre(genre: Genre)
    }
}