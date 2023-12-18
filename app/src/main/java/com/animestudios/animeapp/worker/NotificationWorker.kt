package com.animestudios.animeapp.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.model.Notification
import com.animestudios.animeapp.model.PagingDataItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val aniListGraphQlClient: AniListClient,
    private val preferences: SharedPreferences
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val latestNotification = fetchLatestNotification()

            if (latestNotification != null && !isNotificationIdStored(latestNotification.id)) {
                Log.e(TAG, "Notifications received: $latestNotification")
                showNotification(latestNotification)
                storeNotificationId(latestNotification.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun fetchLatestNotification(): Notification? {
        val response = aniListGraphQlClient.getNotifications(1).data?.convert()

        return response?.flatMap {
            when (it) {
                is PagingDataItem.NotificationItem -> listOf(it.notification)
                else -> emptyList()
            }
        }?.maxByOrNull { it.id ?: -1 }
    }

    private fun showNotification(notification: Notification) {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(notification.getFormattedNotification())
            .setAutoCancel(true)

        sendNotification(builder)
    }


    private fun sendNotification(builder: NotificationCompat.Builder) {

        NotificationManagerCompat.from(applicationContext).apply {
            Log.e(TAG, "sendNotification: ${builder.toString()}")
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SozoNotifications"
            val descriptionText = "Sozonotifications for anime airing"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(App.SOZO_NOTIFICATIONS_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun isNotificationIdStored(id: Int?): Boolean =
        preferences.getBoolean(id.toString(), false)

    private fun storeNotificationId(id: Int?) = preferences.edit { putBoolean(id.toString(), true) }

    companion object {
        private const val TAG = "NotificationWorker"
        private const val CHANNEL_ID = "SOZO_NOTIFICATIONS_CHANNEL_ID"
        private const val NOTIFICATION_ID = 0x1
    }
}
