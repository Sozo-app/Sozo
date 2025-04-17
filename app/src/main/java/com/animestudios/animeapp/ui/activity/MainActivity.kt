package com.animestudios.animeapp.ui.activity

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.ActivityMainBinding
import com.animestudios.animeapp.initActivity
import com.animestudios.animeapp.services.BatteryCheckService
import com.animestudios.animeapp.services.receiver.BatteryReceiver
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import com.animestudios.animeapp.widget.ThemeManager
import com.animestudios.animeapp.worker.NotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var host: NavHostFragment
    private var load = false
    private val model by viewModels<MainViewModelImp>()
    private lateinit var graph: NavGraph
    private val REQUEST_NOTIF = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeManager(this).applyTheme() //This project made  by Single Activity
        requestNotificationPermissionIfNeeded()
        scheduleNotificationWorker()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = BatteryReceiver()
        registerReceiver(receiver, filter)
        host = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        graph = host.navController.navInflater.inflate(R.navigation.app_graph)
        initActivity(this)
        model.getGenresAndTags(this)
        if (!load && Anilist.token != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val id = intent.extras?.getInt("mediaId", 0)
                val isMAL = intent.extras?.getBoolean("mal") ?: false
                val cont = intent.extras?.getBoolean("continue") ?: false
                if (id != null && id != 0) {
                    val media = withContext(Dispatchers.IO) {
                        Anilist.getMedia(id, isMAL)
                    }
                    if (media != null) {
                        media.cameFromContinue = cont
                        startActivity(
                            Intent(this@MainActivity, DetailActivity::class.java)
                                .putExtra("media", media as Serializable)
                        )
                    } else {
                        snackString("Seems like that wasn't found on Anilist.")
                    }
                }
                delay(500)
            }
            load = true
        }


    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIF
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIF) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                snackString("Notification permission granted")
            }
        }
    }

    private fun scheduleNotificationWorker() {
        val workManager = WorkManager.getInstance(this)
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.HOURS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "SozoNotificationCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        val stopServiceIntent = Intent(this, BatteryCheckService::class.java)
        stopService(stopServiceIntent) // Service ni to'xtatish
        super.onDestroy()
    }
}