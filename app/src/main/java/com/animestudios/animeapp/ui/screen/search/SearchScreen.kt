package com.animestudios.animeapp.ui.screen.search

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.databinding.SearchScreenBinding
import com.animestudios.animeapp.navBarHeight
import com.animestudios.animeapp.others.ProgressAdapter
import com.animestudios.animeapp.px
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.statusBarHeight
import com.animestudios.animeapp.ui.screen.search.adapter.SearchAdapter
import com.animestudios.animeapp.ui.screen.search.adapter.SearchItemAdapter
import com.animestudios.animeapp.viewmodel.imp.SearchViewModelImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class SearchScreen : Fragment() {
    private val scope = lifecycleScope

    var style: Int = 1
    private var screenWidth: Float = 0f

    private lateinit var mediaAdaptor: SearchItemAdapter
    private lateinit var progressAdapter: ProgressAdapter

    private lateinit var concatAdapter: ConcatAdapter

    lateinit var result: SearchResults
    lateinit var updateChips: (() -> Unit)
    val model: SearchViewModelImp by viewModels()

    private var _binding: SearchScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = SearchScreenBinding.inflate(inflater, container, false)
        screenWidth = resources.displayMetrics.run { widthPixels / density }
        return _binding?.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchRecyclerView.updatePaddingRelative(
            top = statusBarHeight,
            bottom = navBarHeight + 80f.px
        )
        style = readData<Int>("searchStyle") ?: 1
        if (model.notSet) {
            model.notSet = false
            model.searchResults = SearchResults(
                "ANIME",
                isAdult = false,
                onList = false,
                results = mutableListOf(),
                hasNextPage = false
            )
        }
        mediaAdaptor = SearchItemAdapter(
            style,
            model.searchResults.results,
            requireActivity(),
            matchParent = true
        )

        val headerAdaptor = SearchAdapter(this)
        result = model.searchResults
        initProgress(headerAdaptor)
        val gridSize = (screenWidth / 124f).toInt()
        val gridLayoutManager = GridLayoutManager(requireContext(), gridSize)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (position) {
                    0 -> gridSize
                    concatAdapter.itemCount - 1 -> gridSize
                    else -> when (style) {
                        0 -> 1
                        else -> gridSize
                    }
                }
            }
        }
//        hideNavigation()

        concatAdapter = ConcatAdapter(headerAdaptor, mediaAdaptor, progressAdapter)
        binding.searchRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(v: RecyclerView, dx: Int, dy: Int) {
                if (!v.canScrollVertically(1)) {
                    if (model.searchResults.hasNextPage && model.searchResults.results.isNotEmpty() && !loading) {
                        scope.launch(Dispatchers.IO) {
                            model.loadNextPage(model.searchResults)
                        }
                    }
                }
                super.onScrolled(v, dx, dy)
            }
        })

        binding.searchRecyclerView.layoutManager = gridLayoutManager
        binding.searchRecyclerView.adapter = concatAdapter

        model.result.observe(viewLifecycleOwner) {
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
                mediaAdaptor.notifyItemRangeInserted(prev, it.results.size)
                progressAdapter.bar?.visibility = if (it.hasNextPage) View.VISIBLE else View.GONE
            }
        }
        var visible = false
        fun animate() {
            val start = if (visible) 0f else 1f
            val end = if (!visible) 0f else 1f
            ObjectAnimator.ofFloat(binding.animePageScrollTop, "scaleX", start, end).apply {
                duration = 300
                interpolator = OvershootInterpolator(2f)
                start()
            }
            ObjectAnimator.ofFloat(binding.animePageScrollTop, "scaleY", start, end).apply {
                duration = 300
                interpolator = OvershootInterpolator(2f)
                start()
            }
        }

        binding.searchRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(v: RecyclerView, dx: Int, dy: Int) {
                if (!v.canScrollVertically(1)) {
                    if (model.searchResults.hasNextPage && model.searchResults.results.isNotEmpty() && !loading) {
                        scope.launch(Dispatchers.IO) {
                            loading = true
                            model.loadNextPage(model.searchResults)
                        }
                    }
                }
                if (gridLayoutManager.findFirstVisibleItemPosition() > 1 && !visible) {
                    binding.animePageScrollTop.visibility = View.VISIBLE
                    visible = true
                    animate()
                }

                if (!v.canScrollVertically(-1)) {
                    visible = false
                    animate()
                    scope.launch {
                        delay(300)
                        binding.animePageScrollTop.visibility = View.GONE
                    }
                }

                super.onScrolled(v, dx, dy)
            }
        })
        binding.animePageScrollTop.setOnClickListener {
            binding.searchRecyclerView.scrollToPosition(4)
            binding.searchRecyclerView.smoothScrollToPosition(0)
        }


    }


    fun recycler() {
        mediaAdaptor.type = style
        mediaAdaptor.notifyDataSetChanged()
    }

    private fun initProgress(headerAdaptor: SearchAdapter) {
        val notSet = model.notSet
        progressAdapter = ProgressAdapter(searched = model.searched)

        progressAdapter.ready.observe(viewLifecycleOwner) {
            if (it == true) {
                if (!notSet) {
                    if (!model.searched) {
                        model.searched = true
                        headerAdaptor.search?.run()
                    }
                } else
                    headerAdaptor.requestFocus?.run()

            }
        }

    }


    private var searchTimer = Timer()
    private var loading = false
    fun search() {
        val size = model.searchResults.results.size
        model.searchResults.results.clear()
        requireActivity().runOnUiThread {
            mediaAdaptor.notifyItemRangeRemoved(0, size)
        }

        progressAdapter.bar?.visibility = View.VISIBLE

        searchTimer.cancel()
        searchTimer.purge()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                scope.launch(Dispatchers.IO) {
                    loading = true
                    model.loadSearch(result)
                    loading = false
                }
            }
        }
        searchTimer = Timer()
        searchTimer.schedule(timerTask, 500)
    }

    var state: Parcelable? = null
    override fun onPause() {
        super.onPause()
        state = binding.searchRecyclerView.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        binding.searchRecyclerView.layoutManager?.onRestoreInstanceState(state)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}