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
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.setSlideUp
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreen : Fragment() {
    private val model: MainViewModelImp by viewModels()
    private var _binding: SplashScreenBinding? = null
    private val binding get() = _binding!!
    private var uiSettings = UISettings()

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
        val uiSettings = readData<UISettings>("ui_settings") ?: uiSettings

        if (uiSettings!!.layoutAnimations) {
            binding.splashImage.startAnimation(setSlideUp(UISettings()))
        }
        lifecycleScope.launch(Dispatchers.IO) {
            model.getGenresAndTags(requireActivity())
        }
        lifecycleScope.launch {
            delay(2000)
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