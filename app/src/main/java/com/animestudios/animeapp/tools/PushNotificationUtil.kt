package com.animestudios.animeapp.tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.animestudios.animeapp.R

object PushNotificationUtil {

    private const val CHANNEL_ID = "SOZO_NOTIFICATIONS_CHANNEL_ID"
    private const val MERGED_NOTIFICATION_ID = 0x1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getNotificationManager(context)
            val name = "SozoNotification"
            val descriptionText = "Sozo Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createPushNotificationWithDefaultId(context: Context, message: String) {
        createPushNotification(context, MERGED_NOTIFICATION_ID, message)
    }

    fun createPushNotification(context: Context, id: Int, message: String) {
        val notificationManager = getNotificationManager(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.basic_color))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        createNotificationChannel(context)

        notificationManager.notify(id, builder.build())
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}