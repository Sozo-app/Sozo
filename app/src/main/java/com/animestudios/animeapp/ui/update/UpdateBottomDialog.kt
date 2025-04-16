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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.animestudios.animeapp.BottomSheetDialogFragment
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.databinding.UpdateBottomSheetBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.others.CustomBottomDialog
import com.animestudios.animeapp.others.SpoilerPlugin
import com.animestudios.animeapp.utils.AppUpdate
import com.animestudios.animeapp.visible
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import java.io.File

class UpdateBottomDialog(private val appUpdate: AppUpdate) : BottomSheetDialogFragment() {

    private var _binding: UpdateBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_INSTALL_UNKNOWN_APPS = 1002
    private var downloadId: Long = -1L

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                binding.progressView1.visibility = View.GONE
                onDownloadedCallback?.invoke()
            }
        }
    }

    fun setMarkdown(string: String) {
        val markWon = Markwon.builder(App.currentContext()!!)
            .usePlugin(io.noties.markwon.html.HtmlPlugin.create { plugin ->
                plugin.excludeDefaults(
                    true
                )
            })
            .usePlugin(SpoilerPlugin())
            .build()
        markWon.setMarkdown(binding.markdownText, string)
    }

    private var onDownloadedCallback: (() -> Unit)? = null
    fun setOnDownloadedCallback(callback: () -> Unit) {
        onDownloadedCallback = callback
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UpdateBottomSheetBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setMarkdown(appUpdate.changeLog ?: "")
        binding.bottomSheerCustomTitle.text = "Please Do`nt Close The App !"
        binding.updateBtn.setOnClickListener {
            if (!appUpdate.appLink.isNullOrEmpty()) {
                if (hasStoragePermission(requireContext())) {
                    startDownload(appUpdate.appLink!!)
                } else {
                    openInstallUnknownAppsSettings()
                    requestStoragePermission()
                }
            } else {
                dismissAllowingStateLoss()
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(downloadReceiver)
        } catch (e: IllegalArgumentException) {
        }
    }

    private fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun openInstallUnknownAppsSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dismissAllowingStateLoss()
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivityForResult(intent, REQUEST_INSTALL_UNKNOWN_APPS)
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                appUpdate.appLink?.let { startDownload(it) }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @SuppressLint("WrongConstant", "InlinedApi")
    private fun startDownload(apkUrl: String) {
        binding.progressView1.visible()
        binding.updateBtn.gone()
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val apkFileName = "update_app.apk"
        val apkFile = File(downloadsDir, apkFileName)

        if (apkFile.exists()) {
            apkFile.delete()
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Downloading update apk...")
            setDescription("Your update is being downloaded.")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkFileName)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        }

        val downloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)
        binding.progressView1.visibility = View.VISIBLE

        trackDownloadProgress(downloadManager, downloadId)
    }

    private fun trackDownloadProgress(downloadManager: DownloadManager, downloadId: Long) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (_binding == null) return

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val bytesTotal = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
                        binding.progressView1.progress = progress.toFloat()
                        binding.progressView1.labelText = "$progress%"
                    }
                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )
                    cursor.close()

                    if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }
        handler.post(runnable)
    }


    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
