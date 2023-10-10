package com.animestudios.animeapp.ui.screen.browse.page.genre.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.GenresItemBinding
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.settings.UISettings

class GenreAdapter(private val big: Boolean = false) :
    RecyclerView.Adapter<GenreAdapter.GenreVh>() {
    var genres = mutableMapOf<String, String>()
    var pos = arrayListOf<String>()
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    inner class GenreVh(val binding: GenresItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val genre = genres[pos[bindingAdapterPosition]]
            binding.genreTitle.text = pos[bindingAdapterPosition]
            binding.genreImage.loadImage(genre)

        }
    }

    override fun getItemCount(): Int = genres.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreVh {
        val binding = GenresItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreVh(binding)

    }

    override fun onBindViewHolder(holder: GenreVh, position: Int) {
        holder.onBind()
    }

    fun addGenre(genre: Pair<String, String>) {
        genres[genre.first] = genre.second
        pos.add(genre.first)
        notifyItemInserted(pos.size - 1)
    }
}