package com.animestudios.animeapp.ui.screen.browse.page.allanime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.databinding.FragmentAllAnimeBinding
import com.animestudios.animeapp.others.ProgressAdapter
import com.animestudios.animeapp.ui.screen.browse.page.allanime.adapter.AllAnimePageAdapter
import com.animestudios.animeapp.viewmodel.imp.AllAnimeViewModelImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AllAnimeFragment : Fragment() {

    private var _binding: FragmentAllAnimeBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<AllAnimeViewModelImp>()
    private lateinit var allAnimePageAdapter: AllAnimePageAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private var style = 0
    lateinit var result: SearchResults
    private lateinit var progressAdapter: ProgressAdapter
    private var screenWidth: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = com.animestudios.animeapp.databinding.FragmentAllAnimeBinding.inflate(
            inflater,
            container,
            false
        )
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenWidth = resources.displayMetrics.run { widthPixels / density }
        initUIWithLocalData()
        result = model.searchResults
        initProgress()
        allAnimePageAdapter =
            AllAnimePageAdapter(model.searchResults.results, true, requireActivity())
        concatAdapter = ConcatAdapter(allAnimePageAdapter, progressAdapter)
        binding.allAnimeRv.adapter = concatAdapter
//        val gridSize = (screenWidth / 124f).toInt()
//        val gridLayoutManager = GridLayoutManager(requireContext(), gridSize)
//        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return when (position) {
//                    0                           -> gridSize
//                    concatAdapter.itemCount - 1 -> gridSize
//                    else                        -> when (style) {
//                        0    -> 1
//                        else -> gridSize
//                    }
//                }
//            }
//        }
//        binding.allAnimeRv.layoutManager=gridLayoutManager
        initPagination()
        observerLoadData()
    }

    private fun initUIWithLocalData() {
        if (model.notSet) {
            model.notSet = false
            model.searchResults = SearchResults(
                "ANIME",
                isAdult = false,
                onList = false,
                results = mutableListOf(),
                hasNextPage = true,
                sort = Anilist.sortBy.get("Score")
            )
        }
    }


    private fun observerLoadData() {
        model.result.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                }
                Resource.Loading -> {
                }
                is Resource.Success -> {
                    val it = it.data
                    if (it != null) {
                        model.searchResults.apply {
                            onList = it.onList
                            isAdult = it.isAdult
                            perPage = it.perPage
                            search = it.search
                            sort = it.sort
                            genres = it.genres
                            excludedGenres = it.excludedGenres
                            excludedTags = it.excludedTags
                            tags = it.tags
                            season = it.season
                            seasonYear = it.seasonYear
                            format = it.format
                            page = it.page
                            hasNextPage = it.hasNextPage
                        }

                        val prev = model.searchResults.results.size
                        model.searchResults.results.addAll(it.results)
                        allAnimePageAdapter.notifyItemRangeInserted(
                            prev,
                            it.results.size
                        )
                        progressAdapter.bar?.visibility =
                            if (it.hasNextPage) View.VISIBLE else View.GONE

                    }
                }
            }
        }
    }

    private fun initPagination() {
        binding.allAnimeRv.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(v: RecyclerView, dx: Int, dy: Int) {
                if (!v.canScrollVertically(1)) {
                    if (model.searchResults.hasNextPage && model.searchResults.results.isNotEmpty() && !loading) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            model.loadNextPage(model.searchResults)
                        }
                    }
                }
                super.onScrolled(v, dx, dy)
            }
        })
    }

    private fun initProgress() {
        val notSet = model.notSet
        progressAdapter = ProgressAdapter(searched = model.searched)

        progressAdapter.ready.observe(viewLifecycleOwner) {
            if (it == true) {
                if (!notSet) {
                    if (!model.searched) {
                        model.searched = true
                    }
                }
                loadData()
            }
        }

    }


    private var searchTimer = Timer()
    private var loading = false

    fun loadData() {
        val size = model.searchResults.results.size
        model.searchResults.results.clear()
        requireActivity().runOnUiThread {
            allAnimePageAdapter.notifyItemRangeRemoved(0, size)
        }
        progressAdapter.bar?.visibility = View.VISIBLE

        searchTimer.cancel()
        searchTimer.purge()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                lifecycleScope.launch(Dispatchers.IO) {
                    loading = true
                    model.loadAllAnimeList()
                    loading = false
                }
            }
        }
        searchTimer = Timer()
        searchTimer.schedule(timerTask, 500)
    }


    override fun onResume() {
        super.onResume()
        result.page = 0
    }


}