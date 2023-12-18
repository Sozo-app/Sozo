package com.animestudios.animeapp.ui.screen.detail.pages.episodes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.animestudios.animeapp.databinding.EpisodeScreenBinding


class EpisodeScreen : Fragment() {

    private var _binding: EpisodeScreenBinding?=null

    private val binding get() =_binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        _binding = EpisodeScreenBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}