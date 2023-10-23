package com.animestudios.animeapp.services.receiver

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.animestudios.animeapp.currActivity
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.settings.UISettings
import com.irozon.sneaker.Sneaker

class BatteryReceiver : BroadcastReceiver() {
    private var isNotificationShown = false

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val isCharging = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            ) == BatteryManager.BATTERY_STATUS_CHARGING
            if (level <= 15 && !isCharging) {
                showNotification(level)
                val uiSettings = readData<UISettings>("ui_settings")?:UISettings()
                saveData("ui_settings", uiSettings.copy(layoutAnimations = false))
            } else if ((level > 15 || isCharging)) {
                val uiSettings = readData<UISettings>("ui_settings")?:UISettings()
                saveData("ui_settings", uiSettings.copy(layoutAnimations = true))
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "NewApi")
    private fun showNotification(level: Int) {
        if (level <= 15 && !isNotificationShown) {
            isNotificationShown = true
            Sneaker.with(currActivity()!!)
                .setTitle("Battery Low !!")
                .setMessage("Power Saving Mode Enabled !")
                .sneakWarning()
        } else if (level >= 15) {
            isNotificationShown = false
        }

    }


}
