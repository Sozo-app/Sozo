package com.animestudios.animeapp.ui.screen.detail.pages.relation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.RelationScreenBinding

class RelationScreen : Fragment(){
    private var _binding:RelationScreenBinding?=null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding =RelationScreenBinding.inflate(inflater,container,false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}