package com.animestudios.animeapp.ui.screen.browse.page.genre.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.GenresItemBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.px
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.settings.UISettings

class GenreAdapter(private val big: Boolean = false) :
    RecyclerView.Adapter<GenreAdapter.GenreVh>() {
    var genres = mutableMapOf<String, String>()
    var pos = arrayListOf<String>()
    private lateinit var initItemListener: (String,) -> Unit
    fun setItemListener(listener: (String) -> Unit) {
        initItemListener = listener
    }

    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    inner class GenreVh(val binding: GenresItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            if (big) binding.genreCard.updateLayoutParams { height = 72f.px }
            val genre = genres[pos[bindingAdapterPosition]]
            binding.genreTitle.text = pos[bindingAdapterPosition]
            binding.genreImage.loadImage(genre)
            binding.root.setOnClickListener {
                initItemListener.invoke(pos[adapterPosition])
                if (pos[bindingAdapterPosition].lowercase() == "hentai") {
                    if (!Anilist.adult) Toast.makeText(
                        itemView.context,
                        "Turn on 18+ Content from your Anilist Settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //                ContextCompat.startActivity(
                //                    itemView.context,
                //                    Intent(itemView.context, SearchScreen::class.java)
                //                        .putExtra("type", type)
                //                        .putExtra("genre", pos[bindingAdapterPosition])
                //                        .putExtra("sortBy", "Trending")
                //                        .putExtra("search", true)
                //                        .also {
                //                            if (pos[bindingAdapterPosition].lowercase() == "hentai") {
                //                                if (!Anilist.adult) Toast.makeText(
                //                                    itemView.context,
                //                                    "Turn on 18+ Content from your Anilist Settings",
                //                                    Toast.LENGTH_SHORT
                //                                ).show()
                //                                it.putExtra("hentai", true)
                //                            }
                //                        },
                //                    null
                //                )
            }

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