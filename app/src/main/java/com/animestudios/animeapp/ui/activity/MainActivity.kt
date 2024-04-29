package com.animestudios.animeapp.ui.activity

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.ActivityMainBinding
import com.animestudios.animeapp.initActivity
import com.animestudios.animeapp.services.BatteryCheckService
import com.animestudios.animeapp.services.receiver.BatteryReceiver
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import com.animestudios.animeapp.widget.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var host: NavHostFragment
    private var load = false
    private val model by viewModels<MainViewModelImp>()
    private lateinit var graph: NavGraph
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeManager(this).applyTheme() //This project made  by Single Activity

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


    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        val stopServiceIntent = Intent(this, BatteryCheckService::class.java)
        stopService(stopServiceIntent) // Service ni to'xtatish
        super.onDestroy()
    }
}