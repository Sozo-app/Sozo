package com.animestudios.animeapp.ui.screen.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.animestudios.animeapp.databinding.ProfileScreenBinding
import com.animestudios.animeapp.loadProfileCategory
import com.animestudios.animeapp.ui.screen.profile.adapter.ProfileAdapter


class ProfileScreen : Fragment() {
    private val adapter = ProfileAdapter()

    private var _binding: com.animestudios.animeapp.databinding.FragmentProfileScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = com.animestudios.animeapp.databinding.FragmentProfileScreenBinding.inflate(inflater, container, false)
        return _binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            adapter.submitList(loadProfileCategory())
            profileRv.adapter = adapter

        }

    }

}