package com.animestudios.animeapp.ui.screen.search.adapter

import android.annotation.SuppressLint
import android.os.Handler
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
import com.animestudios.animeapp.ui.screen.search.SearchScreen
import com.animestudios.animeapp.ui.screen.search.dialog.SearchFilterBottomDialog
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext


class SearchAdapter(private val activity: SearchScreen) :
    RecyclerView.Adapter<SearchAdapter.SearchHeaderViewHolder>() {
    private val itemViewType = 6969
    var isAddedHistory = false
    var search: Runnable? = null
    var requestFocus: Runnable? = null
    private var textWatcher: TextWatcher? = null
    private var job: Job? = null // coroutine job
    private val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()

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

        binding.searchBarText.removeTextChangedListener(textWatcher)
        binding.searchBarText.setText(activity.result.search)


        binding.searchChipRecycler.adapter = SearchChipAdapter(activity).also {
            activity.updateChips = { it.update() }
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
            if (binding.searchBarText.text.isEmpty()) {
                binding.genreContainer.gone()

                binding.historyContainer.visible()
                binding.historyTitleContainer.visible()
                var count = 0
                val list = readData<ArrayList<String>>("historyList") ?: arrayListOf()
                val parentView = binding.parentTags
                parentView.removeAllViews();
                list.onEach {
                    val chipView = ItemHistorySearchBinding.inflate(
                        LayoutInflater.from(activity.requireContext()),
                        parentView,
                        false
                    )
                    if (count >= 7) return@onEach
                    chipView.root.text = it
                    chipView.root.setOnClickListener {
                        binding.searchBarText.setText(chipView.root.text)
                        searchTitle()
                    }
                    parentView.addView(chipView.root)
                    count++


                }
            } else {
                binding.genreContainer.visible()
                binding.historyContainer.gone()
                binding.historyTitleContainer.gone()
            }


            activity.result.apply {
                search =
                    if (binding.searchBarText.text.toString() != "") binding.searchBarText.text.toString() else null
                onList = listOnly
                isAdult = adult
            }
            if (binding.searchBarText.text.isNotEmpty()) {
                activity.search()
            }
        }
        binding.searchClear.setOnClickListener {
            saveData("historyList", arrayListOf<String>())
            binding.parentTags.removeAllViews()

        }

        fun delayedSaveText(enteredText: String) {
            // Coroutine boshlaymiz
            job = CoroutineScope(coroutineContext).launch {
                // 1.5 soniya (1500 millisekund) ichida matnni tekshirib, o'zgarmagan bo'lsa saqlash
                delay(1500)
                if (enteredText == binding.searchBarText.text.toString()) {
                    var historyList = readData<ArrayList<String>>("historyList")
                    val history = binding.searchBarText.text.toString()
                    if (historyList == null) historyList = ArrayList()
                    if (!historyList.contains(history)) {
                        historyList.add(history)
                        println(historyList.toString())
                        saveData("historyList", historyList)
                    }
                    searchTitle()
                }
            }
        }

        textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                delayedSaveText(s.toString())
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
                    var historyList = readData<ArrayList<String>>("historyList")
                    if (binding.searchBarText.text.isNotEmpty()) {
                        val history = binding.searchBarText.text.toString()
                        if (historyList == null) historyList = ArrayList()
                        if (!historyList!!.contains(history)) {
                            historyList!!.add(history)
                            println(historyList.toString())
                            saveData("historyList", historyList)
                        }
                        searchTitle()
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


        search = Runnable { searchTitle() }
        requestFocus = Runnable { binding.searchBarText.requestFocus() }

    }


    override fun getItemCount(): Int = 1

    inner class SearchHeaderViewHolder(val binding: com.animestudios.animeapp.databinding.ItemSearchHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return itemViewType
    }


    class SearchChipAdapter(val activity: SearchScreen) :
        RecyclerView.Adapter<SearchChipAdapter.SearchChipViewHolder>() {
        private var chips = activity.result.toChipList()

        inner class SearchChipViewHolder(val binding: ItemChipBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchChipViewHolder {
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