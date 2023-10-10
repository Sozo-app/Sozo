package com.animestudios.animeapp.ui.screen.search.dialog.tab.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.FilterSingleClickScreenForGenreBinding
import com.animestudios.animeapp.ui.screen.search.dialog.SearchFilterBottomDialog

class FilterSingleClickScreenForGenre(
    val defaultItem: ArrayList<String>
) : Fragment() {
    private var _binding: FilterSingleClickScreenForGenreBinding? = null
    private val binding get() = _binding!!
    private lateinit var listener: ((ArrayList<String>) -> Unit)
    fun singleItemClickListener(itemListener: ((ArrayList<String>) -> Unit)) {
        listener = itemListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FilterSingleClickScreenForGenreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            binding.singleItemClickList.layoutManager =
                GridLayoutManager(binding.root.context, 1, RecyclerView.HORIZONTAL, false)
            if (defaultItem.isNotEmpty()) {
                val position =
                    if (Anilist.genres!!.indexOf(defaultItem.get(defaultItem.lastIndex)) == -1) 0 else Anilist.genres!!.indexOf(
                        defaultItem.get(0)
                    )
                singleItemClickList.scrollToPosition(position)
            }

            binding.singleItemClickList.adapter =
                SearchFilterBottomDialog.FilterChipAdapter(Anilist.genres ?: listOf()) { chip ->
                    val genre = chip.text.toString()
                    chip.isChecked = defaultItem.contains(genre)
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            chip.isCloseIconVisible = false
                            defaultItem.add(genre)
                            listener.invoke(defaultItem)
                        } else
                            defaultItem.remove(genre)
                        listener.invoke(defaultItem)
                    }

                }

        }
    }
}