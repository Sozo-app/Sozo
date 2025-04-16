package com.animestudios.animeapp.ui.screen.splash

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.SplashScreenBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.setSlideUp
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.ui.update.UpdateBottomDialog
import com.animestudios.animeapp.ui.update.UpdateNoticeBottomDialog
import com.animestudios.animeapp.utils.logError
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import com.animestudios.animeapp.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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
        model.checkForAppUpdate()
        val animation = setSlideUp(UISettings())
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {

                lifecycleScope.launch {
                    binding.progressDataGet.visible()
                    delay(2000)
                    model.isUpdateAvailableLiveData.observe(viewLifecycleOwner) {
                        if (it) {
                            model.getAppUpdateInfo()
                            model.getAppUpdateInfo.observe(viewLifecycleOwner) {
                                binding.progressDataGet.gone()
                                snackString("New Update Available")
                                val dialog = UpdateBottomDialog(it)
                                dialog.show(parentFragmentManager, "dialog")
                                dialog.isCancelable = false
                                dialog.setOnDownloadedCallback {
                                    snackString("Download Completed")
                                    dialog.dismiss()
                                    val alertDialog = UpdateNoticeBottomDialog()
                                    alertDialog.show(parentFragmentManager, "dialog")
                                    alertDialog.setUnderstandBtnClickCallback {
                                        alertDialog.dismiss()
                                        openDownloadsFolder(requireContext())
                                        closeApp()
                                    }
                                }

                            }
                        } else {
                            binding.progressDataGet.gone()
                            if (Anilist.token == null) {
                                findNavController().navigate(
                                    R.id.loginScreen,
                                    null,
                                    NavOptions.Builder().setPopUpTo(R.id.splashScreen, true)
                                        .build()
                                )
                            }
                            if (Anilist.token != null) {
                                findNavController().navigate(
                                    R.id.mainScreen,
                                    null,
                                    NavOptions.Builder().setPopUpTo(R.id.splashScreen, true)
                                        .build()
                                )
                            }
                        }
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })

        binding.splashImage.startAnimation(animation)
        lifecycleScope.launch(Dispatchers.IO) {
            model.getGenresAndTags(requireActivity())
        }


    }

    private fun openDownloadsFolder(context: Context) {
        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun closeApp() {
        requireActivity().finishAffinity()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}