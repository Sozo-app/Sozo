package com.animestudios.animeapp.ui.screen.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.SplashScreenBinding
import com.animestudios.animeapp.setSlideIn
import com.animestudios.animeapp.setSlideUp
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.slideUp
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : Fragment() {
    private val model: MainViewModelImp by viewModels()
    private var _binding: SplashScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SplashScreenBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            model.getGenresAndTags(requireActivity())
        }
        lifecycleScope.launch {
            binding.splashImage.slideUp(1000, 0)
            delay(2000)
            binding.splashImage.startAnimation(setSlideUp(UISettings()))

            if (Anilist.token == null) {
                findNavController().navigate(
                    R.id.loginScreen,
                    null,
                    NavOptions.Builder().setPopUpTo(R.id.splashScreen, true).build()
                )
            }
            if (Anilist.token != null) {
                findNavController().navigate(
                    R.id.mainScreen,
                    null,
                    NavOptions.Builder().setPopUpTo(R.id.splashScreen, true).build()
                )
            }

        }
    }
}