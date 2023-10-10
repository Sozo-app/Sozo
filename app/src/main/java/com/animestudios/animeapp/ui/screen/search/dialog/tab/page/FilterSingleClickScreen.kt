package com.animestudios.animeapp.ui.screen.search.dialog.tab.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.FilterSingleClickScreenBinding
import com.animestudios.animeapp.ui.screen.search.adapter.SingleSelectAdapter

class FilterSingleClickScreen(
    val list: MutableList<String>,
    val title: String,
    private val defaultItem: String
) : Fragment() {
    private var _binding: FilterSingleClickScreenBinding? = null
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
        _binding = FilterSingleClickScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            singleItemClickList.adapter = adapter
            adapter.setSelectedPosition(list.indexOf(defaultItem))
            binding.singleItemClickList.layoutManager =
                GridLayoutManager(binding.root.context, 1, RecyclerView.HORIZONTAL, false)
            val position =if (list.indexOf(defaultItem)==-1) 0 else list.indexOf(defaultItem)
            singleItemClickList.scrollToPosition(position )
            adapter.singleItemClickListener {
                listener.invoke(it)
            }
        }
    }
}