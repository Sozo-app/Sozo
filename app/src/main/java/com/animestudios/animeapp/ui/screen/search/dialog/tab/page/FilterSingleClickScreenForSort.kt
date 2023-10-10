package com.animestudios.animeapp.ui.screen.search.dialog.tab.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.FilterSingleClickScreenForSortBinding
import com.animestudios.animeapp.ui.screen.search.adapter.SingleSelectAdapter

class FilterSingleClickScreenForSort(
    val list: MutableList<String>,
    val title: String,
    val defaultItem: String
) : Fragment() {
    private var _binding: FilterSingleClickScreenForSortBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy { SingleSelectAdapter(list) }
    private lateinit var listener: ((String) -> Unit)
    fun singleItemClickListener(itemListener: ((String) -> Unit)) {
        listener = itemListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FilterSingleClickScreenForSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            singleItemClickList.adapter = adapter
            adapter.setSelectedPosition(list.indexOf(defaultItem))
            val position =if (list.indexOf(defaultItem)==-1) 0 else list.indexOf(defaultItem)
            singleItemClickList.scrollToPosition(position )
            binding.singleItemClickList.layoutManager =
                GridLayoutManager(binding.root.context, 1, RecyclerView.HORIZONTAL, false)
            adapter.singleItemClickListener {
                listener.invoke(it)
            }
        }
    }
}