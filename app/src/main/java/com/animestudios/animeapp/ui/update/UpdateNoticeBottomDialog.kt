package com.animestudios.animeapp.ui.update

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.animestudios.animeapp.BottomSheetDialogFragment
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.databinding.AlertUpdateProblemSheetBinding
import com.animestudios.animeapp.databinding.UpdateBottomSheetBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.others.SpoilerPlugin
import com.animestudios.animeapp.visible
import io.noties.markwon.Markwon
import java.io.File

class UpdateNoticeBottomDialog : BottomSheetDialogFragment() {

    private var _binding: AlertUpdateProblemSheetBinding? = null
    private val binding get() = _binding!!

    private var understandBtnClickCallback: (() -> Unit)? = null
    fun setUnderstandBtnClickCallback(callback: () -> Unit) {
        understandBtnClickCallback = callback
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AlertUpdateProblemSheetBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.okBtn.setOnClickListener {
            understandBtnClickCallback?.invoke()
        }

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
