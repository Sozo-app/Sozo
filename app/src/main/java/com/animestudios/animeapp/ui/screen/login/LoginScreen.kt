package com.animestudios.animeapp.ui.screen.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.LoginScreenBinding


class LoginScreen : Fragment() {
    private var _binding: LoginScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginScreenBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.login.setOnClickListener {
            val data = arguments?.getInt("selected", 1)
            Anilist.selected = data?:1
            Anilist.loginIntent(requireActivity())

        }
    }

}