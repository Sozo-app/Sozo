package com.animestudios.animeapp.ui.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnAttach
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.ActivityMainBinding
import com.animestudios.animeapp.initActivity
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            uiSettings = readData("ui_settings") ?: uiSettings

        }
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
}