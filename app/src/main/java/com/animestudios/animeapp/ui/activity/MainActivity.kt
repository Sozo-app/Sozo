package com.animestudios.animeapp.ui.activity

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.ActivityMainBinding
import com.animestudios.animeapp.initActivity
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.services.BatteryCheckService
import com.animestudios.animeapp.services.receiver.BatteryReceiver
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var host: NavHostFragment
    private lateinit var graph: NavGraph
    private val model: MainViewModelImp by viewModels()
    private var uiSettings = UISettings()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        host = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        graph = host.navController.navInflater.inflate(R.navigation.app_graph)
        binding.root.isMotionEventSplittingEnabled = false
        binding.root.doOnAttach {
            initActivity(this)

        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = BatteryReceiver()
        registerReceiver(receiver, filter)
        uiSettings = readData("ui_settings") ?: uiSettings


//        lifecycleScope.launchWhenStarted {
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                lifecycleScope.launch(Dispatchers.IO) {
//                    model.getGenresAndTags(this@MainActivity)
//                }
//
//
//                if (Anilist.token == null) {
//
//                    graph.setStartDestination(R.id.loginScreen)
//                    host.navController.graph = graph
//                }
//                if (Anilist.token != null) {
//                    graph.setStartDestination(R.id.mainScreen)
//                    host.navController.graph = graph
//                }
//            }
//ban
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//                graph.setStartDestination(R.id.splashScreen)
//                host.navController.graph = graph
//
//
//            }
//        }


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