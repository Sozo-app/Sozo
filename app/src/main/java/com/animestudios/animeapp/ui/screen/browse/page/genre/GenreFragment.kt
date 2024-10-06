package com.animestudios.animeapp.ui.screen.browse.page.genre

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.FragmentGenreBinding
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.animationTransaction
import com.animestudios.animeapp.ui.screen.browse.page.genre.adapter.GenreAdapter
import com.animestudios.animeapp.viewmodel.imp.GenresViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GenreFragment : Fragment() {
    private var _binding: FragmentGenreBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<GenresViewModelImp>()
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = com.animestudios.animeapp.databinding.FragmentGenreBinding.inflate(
            inflater,
            container,
            false
        )
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val screenWidth = resources.displayMetrics.run { widthPixels / density }
            val adapter = GenreAdapter(true)

            adapter.setItemListener {
                val bundle = Bundle()
                bundle.putString("type", "ANIME")
                bundle.putString("genre", it)
                bundle.putString("sortBy", "Trending")
                bundle.putBoolean("search", true)
                findNavController().navigate(
                    R.id.searchScreen, bundle, animationTransaction().build()
                )
            }
            model.doneListener = {
                MainScope().launch {
                    binding.mediaInfoGenresProgressBar.visibility = View.GONE
                }
            }
            if (model.genres != null) {
                adapter.genres = model.genres!!
                adapter.pos = ArrayList(model.genres!!.keys)
                if (model.done)
                    model.doneListener?.invoke()
            }
            binding.mediaInfoGenresRecyclerView.adapter = adapter
            binding.mediaInfoGenresRecyclerView.layoutManager =
                GridLayoutManager(requireContext(), (screenWidth / 156f).toInt())
            lifecycleScope.launch(Dispatchers.IO) {
                model.loadGenres(Anilist.genres ?: readData("genres_list") ?: arrayListOf()) {
                    MainScope().launch {
                        adapter.addGenre(it)
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        val screenWidth = resources.displayMetrics.run { widthPixels / density }
        binding.mediaInfoGenresRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), (screenWidth / 156f).toInt())

    }
}