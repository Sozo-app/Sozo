package com.animestudios.animeapp.ui.screen.search.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.BottomSheetSearchFilterBinding
import com.animestudios.animeapp.databinding.ItemChipBinding
import com.animestudios.animeapp.loadFilterTab
import com.animestudios.animeapp.ui.screen.search.SearchScreen
import com.animestudios.animeapp.ui.screen.search.adapter.FilterTabAdapter
import com.animestudios.animeapp.ui.screen.search.adapter.SingleSelectAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator


class SearchFilterBottomDialog(
) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetSearchFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var selectedGenres = mutableListOf<String>()
    lateinit var activity: SearchScreen
    private var exGenres = mutableListOf<String>()
    private var selectedTags = mutableListOf<String>()
    private var selectedFormat = ""
    private var selectedSort = ""
    private var selectedYears = -1
    private var exTags = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        selectedGenres = activity.result.genres ?: mutableListOf()
        exGenres = activity.result.excludedGenres ?: mutableListOf()
        selectedTags = activity.result.tags ?: mutableListOf()
        selectedYears = activity.result.seasonYear ?: -1
        selectedSort = activity.result.sort ?: ""
        selectedFormat = activity.result.format ?: ""
        exTags = activity.result.excludedTags ?: mutableListOf()
        println(selectedSort)
        println(selectedYears)
        binding.searchFilterApply.setOnClickListener {
            activity.result.apply {

                genres = selectedGenres
                sort = selectedSort.ifBlank { null }


                if (selectedYears != -1) {
                    activity.result.seasonYear = selectedYears
                }
                tags = selectedTags

                format = selectedFormat.ifBlank { null }
                excludedGenres = exGenres
                excludedTags = exTags
            }
            activity.updateChips.invoke()
            activity.search()
            dismiss()
        }
        binding.searchFilterCancel.setOnClickListener {
            dismiss()
        }
        val newList: MutableList<String> = java.util.ArrayList()
        Anilist.years.onEach {
            newList.add(it.toString())
        }
        val adapter = SingleSelectAdapter(newList)
        binding.searchFilterFormat.adapter = adapter


        adapter.setSelectedPosition(newList.indexOf(selectedYears.toString()))
        val position = if (newList.indexOf(selectedYears.toString()) == -1) 0 else newList.indexOf(
            selectedYears.toString()
        )
        binding.searchFilterFormat.scrollToPosition(position)
        adapter.singleItemClickListener { selectedYears = it.toInt() }

        binding.searchFilterTags.adapter =
            FilterChipAdapter(Anilist.tags?.get(activity.result.isAdult) ?: listOf())
            { chip ->
                val tag = chip.text.toString()
                chip.isChecked = selectedTags.contains(tag)
                chip.isCloseIconVisible = exTags.contains(tag)
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        chip.isCloseIconVisible = false
                        exTags.remove(tag)
                        selectedTags.add(tag)
                    } else
                        selectedTags.remove(tag)
                }
                chip.setOnLongClickListener {
                    chip.isChecked = false
                    chip.isCloseIconVisible = true
                    exTags.add(tag)
                }
            }
        setTab()

    }


    class FilterChipAdapter(val list: List<String>, private val perform: ((Chip) -> Unit)) :
        RecyclerView.Adapter<FilterChipAdapter.SearchChipViewHolder>() {
        inner class SearchChipViewHolder(val binding: ItemChipBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchChipViewHolder {
            val binding =
                ItemChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SearchChipViewHolder(binding)
        }


        override fun onBindViewHolder(holder: SearchChipViewHolder, position: Int) {
            val title = list[position]
            holder.setIsRecyclable(false)
            holder.binding.root.apply {
                text = title
                isCheckable = true
                perform.invoke(this)
            }
        }

        override fun getItemCount(): Int = list.size
    }

    private fun setTab() {


        binding.apply {

            val tabList = loadFilterTab(
                selectedGenres.toMutableList() as ArrayList<String>,
                selectedSort!!,
                selectedFormat.toString()
            )

            val adapter = FilterTabAdapter(
                tabList,
                requireActivity()
            )
            adapter
                .singleItemClickListener {
                    selectedGenres = it
                }
            adapter.singleYearListener {
                selectedFormat = it
            }
            adapter.singleItemSortListener {
                selectedSort = it
            }
            viewPager.adapter = adapter
            viewPager.isUserInputEnabled = false

            TabLayoutMediator(filterType, viewPager) { _, _ ->
            }.attach()
            for (i in 0 until binding.filterType.tabCount) {
                binding.filterType.getTabAt(i)?.let { TooltipCompat.setTooltipText(it.view, null) }
            }
            binding.apply {
                val tabCount = filterType.tabCount
                for (i in 0 until tabCount) {
                    val tab = filterType.getTabAt(i)
                    tab!!.text = tabList.get(i).title
                }

            }

        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        fun newInstance() = SearchFilterBottomDialog()
    }

}