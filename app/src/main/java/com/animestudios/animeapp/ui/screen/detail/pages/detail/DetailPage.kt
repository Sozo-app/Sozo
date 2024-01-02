package com.animestudios.animeapp.ui.screen.detail.pages.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.DetailPageBinding
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailPage : Fragment() {

    private val model by activityViewModels<DetailsViewModelImpl>()
    private var _binding: DetailPageBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DetailPageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}