package com.animestudios.animeapp.ui.screen.search.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.ItemChipBinding
import com.animestudios.animeapp.databinding.ItemHistorySearchBinding
import com.animestudios.animeapp.databinding.ItemSearchHeaderBinding
import com.animestudios.animeapp.tools.SearchState
import com.animestudios.animeapp.ui.screen.search.SearchScreen
import com.animestudios.animeapp.ui.screen.search.dialog.SearchFilterBottomDialog


class SearchAdapter(private val activity: SearchScreen) :
    RecyclerView.Adapter<SearchAdapter.SearchHeaderViewHolder>() {
    private val itemViewType = 6969
    var search: Runnable? = null
    private var isGenereClicked = false
    private var searchState = SearchState.EMPTY_SEARCH
    var requestFocus: Runnable? = null
    private var textWatcher: TextWatcher? = null
    lateinit var binding2: ItemSearchHeaderBinding
    lateinit var itemHistoryListener: (String) -> Unit


    fun setItemHistoryListenerChip(itemHistoryListener: (String) -> Unit) {
        this.itemHistoryListener = itemHistoryListener
    }

    var historyList = readData<ArrayList<String>>("historyListNewList") ?: arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHeaderViewHolder {
        val binding =
            ItemSearchHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchHeaderViewHolder(binding)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SearchHeaderViewHolder, position: Int) {
        val binding = holder.binding
        val imm: InputMethodManager = activity.requireActivity()
            .getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        when (activity.style) {
            0 -> {
                binding.searchResultGrid.alpha = 1f
                binding.searchResultList.alpha = 0.33f
            }
            1 -> {
                binding.searchResultList.alpha = 1f
                binding.searchResultGrid.alpha = 0.33f
            }
        }




        binding.searchBarText.hint = "Search" + activity.result.type.toFirstUpperCase()
        val adult = activity.result.isAdult
        val listOnly = activity.result.onList
        reloadSearchHistory()
        binding.searchClear.setOnClickListener {
            saveData("historyListNewList", arrayListOf<String>())
            binding.parentTags.removeAllViews()
            binding.historyTitleContainer.gone()
            binding.genreContainer.visible()
        }

        binding.searchBarText.removeTextChangedListener(textWatcher)
        binding.searchBarText.setText(activity.result.search)

        binding.searchChipRecycler.adapter = SearchChipAdapter(activity).also {
            activity.updateChips = {
                it.update()
                isGenereClicked = true
                reloadSearchHistory()

            }
            it.setChipItemClickListener {
                reloadSearchHistory()
            }
        }
        val chipLayoutManager =
            LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)

        binding.searchChipRecycler.layoutManager = chipLayoutManager
        binding.searchChipRecycler.scrollToPosition(1)

        binding.searchFilter.setOnClickListener {
            val bottomSheetFragment = SearchFilterBottomDialog()
            bottomSheetFragment.activity = activity
            bottomSheetFragment.show(activity.childFragmentManager, "hash_tag")
            it.preventTwoClick()
        }


        fun searchTitle() {
            reloadSearchHistory()
            activity.result.apply {
                search =
                    if (binding.searchBarText.text.toString() != "") binding.searchBarText.text.toString() else null
                activity.lastSearchedText =
                    if (binding.searchBarText.text.toString() != "") binding.searchBarText.text.toString() else ""
                onList = listOnly
                isAdult = adult
            }
            if (binding.searchBarText.text.isNotEmpty()) {
                activity.search()
            }
        }
        activity.setIsMicClickedListener {
            binding.searchBarText.setText(it)
            searchTitle()
        }


        textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                searchTitle()
            }
        }
        binding.searchBarText.addTextChangedListener(textWatcher)
        binding.searchBarText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (binding.searchBarText.text.isNotEmpty()) {
                        searchTitle()
                        reloadSearchHistory()
                        binding.searchBarText.clearFocus()
                        imm.hideSoftInputFromWindow(binding.searchBarText.windowToken, 0)
                    }

                    true
                }
                else -> false
            }
        }

        binding.searchResultGrid.setOnClickListener {
            it.alpha = 1f
            binding.searchResultList.alpha = 0.33f
            activity.style = 0
            saveData("searchStyle", 0)
            activity.recycler()
        }
        binding.searchResultList.setOnClickListener {
            it.alpha = 1f
            binding.searchResultGrid.alpha = 0.33f
            activity.style = 1
            saveData("searchStyle", 1)
            activity.recycler()
        }


        setItemHistoryListenerChip {
            searchTitle()
        }

        search = Runnable { searchTitle() }
        requestFocus = Runnable { binding.searchBarText.requestFocus() }

    }

    private fun hideSearchHistory(isHide: Boolean) {
        if (isHide) {
            binding2.historyContainer.gone()
            binding2.historyTitleContainer.gone()
            binding2.genreContainer.visible()
        } else if (historyList.isEmpty()) {
            binding2.historyContainer.gone()
            binding2.historyTitleContainer.gone()
            binding2.genreContainer.visible()
        } else {
            binding2.historyContainer.visible()
            binding2.historyTitleContainer.visible()
            binding2.genreContainer.gone()

        }
    }

    fun reloadSearchHistory() {
        val binding = binding2
        historyList = readData("historyListNewList") ?: arrayListOf()

        if (historyList.isNotEmpty()) {

            if (binding.searchBarText.text.isEmpty()) {
                binding.historyContainer.visible()
                binding.historyTitleContainer.visible()

                if (isGenereClicked) {
                    hideSearchHistory(true)
                } else {
                    hideSearchHistory(false)
                }

                var count = 0
                val parentView = binding.parentTags
                parentView.removeAllViews();
                historyList.onEach {
                    val chipView = ItemHistorySearchBinding.inflate(
                        LayoutInflater.from(activity.requireContext()),
                        parentView,
                        false
                    )
                    if (count >= 7) return@onEach
                    chipView.root.text = it
                    chipView.root.setOnClickListener {
                        binding.searchBarText.setText(chipView.root.text)

                        itemHistoryListener.invoke(chipView.root.text.toString())
                    }
                    chipView.root.setOnCloseIconClickListener {
                        historyList.remove(chipView.root.text.toString())
                        saveData("historyListNewList", historyList)
                        reloadSearchHistory()
                    }
                    parentView.addView(chipView.root)
                    count++

                }

            } else {
                binding.historyContainer.gone()
                binding.historyTitleContainer.gone()
                binding.genreContainer.visible()

            }

        } else {
            binding.historyContainer.gone()
            binding.historyTitleContainer.gone()
            binding.genreContainer.visible()
        }
    }

    //Save Last History Query

    fun delayedSaveText(enteredText: String) {
        historyList = readData("historyListNewList") ?: arrayListOf()
        if (enteredText == binding2.searchBarText.text.toString() && enteredText.isNotEmpty()) {
            val history = binding2.searchBarText.text.toString()
            if (!historyList.contains(history)) {
                historyList.add(history)
                println(historyList.toString())
                saveData("historyListNewList", historyList)
                reloadSearchHistory()
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = 1

    inner class SearchHeaderViewHolder(val binding: com.animestudios.animeapp.databinding.ItemSearchHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding2 = binding
        }
    }

    override fun getItemViewType(position: Int): Int {
        return itemViewType
    }


    class SearchChipAdapter(val activity: SearchScreen) :
        RecyclerView.Adapter<SearchChipAdapter.SearchChipViewHolder>() {
        lateinit var chipItemClickListener: () -> Unit

        @JvmName("setChipItemClickListener1")
        fun setChipItemClickListener(listener: () -> Unit) {
            chipItemClickListener = listener
        }

        private var chips = activity.result.toChipList()

        inner class SearchChipViewHolder(val binding: ItemChipBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): SearchChipViewHolder {
            val binding =
                ItemChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SearchChipViewHolder(binding)
        }


        override fun onBindViewHolder(holder: SearchChipViewHolder, position: Int) {
            val chip = chips[position]
            holder.binding.root.apply {
                text = chip.text
                setOnClickListener {
                    activity.result.removeChip(chip)
                    update()
                    chipItemClickListener.invoke()
                    activity.search()
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun update() {

            chips = activity.result.toChipList()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = chips.size
    }
}