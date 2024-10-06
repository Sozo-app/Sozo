package com.animestudios.animeapp.ui.screen.search

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.response.SearchResults
import com.animestudios.animeapp.databinding.SearchScreenBinding
import com.animestudios.animeapp.others.ProgressAdapter
import com.animestudios.animeapp.tools.SearchType
import com.animestudios.animeapp.ui.screen.search.adapter.SearchAdapter
import com.animestudios.animeapp.ui.screen.search.adapter.SearchItemAdapter
import com.animestudios.animeapp.viewmodel.imp.SearchViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class SearchScreen : Fragment() {
    private val scope = lifecycleScope
    var lastSearchedText = ""
    var style: Int = 1
    private var screenWidth: Float = 0f

    private lateinit var mediaAdaptor: SearchItemAdapter
    private lateinit var progressAdapter: ProgressAdapter

    private lateinit var concatAdapter: ConcatAdapter
    lateinit var isMicClickedListener: (String) -> Unit
    private lateinit var speechRecognizer: SpeechRecognizer

    lateinit var result: SearchResults
    lateinit var updateChips: (() -> Unit)
    var type: SearchType = SearchType.ANIME
    val model: SearchViewModelImp by viewModels()

    private var _binding: SearchScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun setIsMicClickedListener(isMicClickedListener: (String) -> Unit) {
        this.isMicClickedListener = isMicClickedListener
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
        style = readData<Int>("searchStyle") ?: 0
        if (model.notSet) {
            model.notSet = false
            model.searchResults = SearchResults(
                "ANIME",
                isAdult = false,
                onList = false,
                genres = requireActivity().intent.getStringExtra("genre")
                    ?.let { mutableListOf(it) },
                tags = requireActivity().intent.getStringExtra("tag")?.let { mutableListOf(it) },
                sort = requireActivity().intent.getStringExtra("sortBy"),

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
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireActivity())

        binding.micSearch.setOnClickListener {
            setUpVoiceMicSearch()
        }
//        hideNavigation()

        checkSearchType(headerAdaptor)
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
                headerAdaptor.delayedSaveText(lastSearchedText)

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


    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }

    }

    private fun checkSearchType(headerAdaptor: SearchAdapter) {
        when (type) {
            SearchType.ANIME -> {
                concatAdapter = ConcatAdapter(headerAdaptor, mediaAdaptor, progressAdapter)
            }
            SearchType.CHARACTER -> TODO()
            SearchType.STAFF -> TODO()
            SearchType.USER -> TODO()
        }
    }


    fun recycler() {
        mediaAdaptor.type = style
        mediaAdaptor.notifyDataSetChanged()
    }


    private fun setUpVoiceMicSearch() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission();
        }
        // Initialize SpeechRecognizer
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // Called when the speech recognition service is ready for user input
            }

            override fun onBeginningOfSpeech() {
                // Called when the user starts speaking
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                // Called when the user stops speaking
            }

            override fun onResults(results: Bundle?) {
                // Called when speech recognition results are available
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    // Handle the recognized text as needed
                    model.searchResults.search=recognizedText
                    progressAdapter.notifyDataSetChanged()
                    search()
                    snackString(recognizedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                TODO("Not yet implemented")
            }

            // Implement other methods as needed

            override fun onError(error: Int) {
                // Called when there is an error in speech recognition
            }
        })
        speechRecognizer.startListening(requireActivity().intent)
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
                if (requireActivity().intent.getBooleanExtra("search", false)) search()

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

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                isMicClickedListener.invoke(result?.get(0)?.toString().toString())
                snackString(result?.get(0)?.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                snackString("Permission Granted")
            }
        }
    }
}