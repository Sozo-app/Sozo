package com.animestudios.animeapp.ui.screen.profile.page.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.animestudios.animeapp.databinding.SourcePageBinding
import com.animestudios.animeapp.others.SourceList
import com.animestudios.animeapp.ui.screen.profile.adapter.SourceHeaderAdapter

class SourcePage : Fragment() {

    private var _binding: SourcePageBinding? = null
    private val binding get() = _binding!!
    //

    private val adapter by lazy { SourceHeaderAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SourcePageBinding.inflate(inflater, container, false)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sourceHeaderRv.adapter = adapter
        adapter.submitList(SourceList.sourceList)
    }


}