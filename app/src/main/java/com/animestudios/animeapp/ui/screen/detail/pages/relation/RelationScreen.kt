package com.animestudios.animeapp.ui.screen.detail.pages.relation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.animestudios.animeapp.databinding.RelationScreenBinding
import com.animestudios.animeapp.invisible
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.ui.activity.DetailActivity
import com.animestudios.animeapp.ui.screen.detail.adapter.RelationsAdapter
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import com.animestudios.animeapp.visible
import java.io.Serializable

class RelationScreen : Fragment() {
    private val relationAdapter by lazy { RelationsAdapter() }
    private var _binding: RelationScreenBinding? = null
    private val binding get() = _binding!!
    private val model by activityViewModels<DetailsViewModelImpl>()
    private var screenWidth: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RelationScreenBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenWidth = resources.displayMetrics.run { widthPixels / density }
        super.onViewCreated(view, savedInstanceState)
        model.getMedia().observe(viewLifecycleOwner) {
            val media = it
            model.loadRelationsById(media.id)
            model.getRelations.observe(viewLifecycleOwner) {

                when (it) {
                    is Resource.Error -> {
                        binding.progressBar.invisible()
                        binding.relationRv.visible()
                    }
                    Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.relationRv.invisible()
                    }
                    is Resource.Success -> {
                        binding.progressBar.invisible()
                        binding.relationRv.visible()
                        binding.relationRv.layoutManager =
                            GridLayoutManager(requireContext(), (screenWidth / 124f).toInt())
                        binding.relationRv.adapter = relationAdapter
                        relationAdapter.submitList(it.data)
                        relationAdapter.setItemClickListener {
                            model.getMediaById(it.id)
                            model.getMediaData.observe(viewLifecycleOwner){
                                ContextCompat.startActivity(
                                    requireActivity(),
                                    Intent(activity, DetailActivity::class.java).putExtra(
                                        "media",
                                        it!! as Serializable
                                    ), null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}