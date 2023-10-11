package com.animestudios.animeapp.services.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.core.app.NotificationCompat
import com.animestudios.animeapp.R
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.settings.UISettings

class BatteryReceiver : BroadcastReceiver() {
    private var isLowBattery = false
    private var lastBatteryLevel = 0

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val isCharging = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            ) == BatteryManager.BATTERY_STATUS_CHARGING
            if (level <= 15 && !isCharging && lastBatteryLevel == 0) {
                showNotification(context, level)
                val uiSettings = readData<UISettings>("ui_settings")
                saveData("ui_settings",uiSettings!!.copy(layoutAnimations = false))
                lastBatteryLevel += 1
                isLowBattery = true
            } else if ((level > 15 || isCharging) && isLowBattery) {
                cancelNotification(context)
                isLowBattery = false
                lastBatteryLevel -= 1
            }
            println("LAST BATTERY : " + lastBatteryLevel)
        }
    }

    private fun showNotification(context: Context, level: Int) {
        val channelId = "battery_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "sozo",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Sozo App")
            .setContentText("Power saving mode is enabled.")
            .setSmallIcon(R.drawable.logo)

        notificationManager.notify(1, notification.build())
    }

    private fun cancelNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1) // 1 - Notification ID
    }
}
