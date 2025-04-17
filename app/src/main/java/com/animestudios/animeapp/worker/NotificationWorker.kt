package com.animestudios.animeapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
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
        try {
            fetchLatestNotification()?.let { notification ->
                if (notification.id != null && !isStored(notification.id)) {
                    showNotification(notification)
                    store(notification.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun fetchLatestNotification(): Notification? {
        val response = aniListGraphQlClient.getNotifications(1)
        val items = response
            ?.data
            ?.convert()
            ?.flatMap {
                (it as? PagingDataItem.NotificationItem)?.let { listOf(it.notification) } ?: emptyList()
            } ?: return null

        return items.maxByOrNull { it.id ?: -1 }
    }

    private fun showNotification(notification: Notification) {
        createChannelIfNeeded()
        val content = notification.getFormattedNotification()
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(content)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(applicationContext, R.color.basic_color))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID, builder.build())
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sozo Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Anime airing & activity alerts"
            }
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun isStored(id: Int) = preferences.getBoolean(id.toString(), false)
    private fun store(id: Int) {
        preferences.edit().putBoolean(id.toString(), true).apply()
    }

    companion object {
        private const val TAG = "NotificationWorker"
        private const val CHANNEL_ID = "SOZO_NOTIFICATIONS_CHANNEL_ID"
        private const val NOTIFICATION_ID = 0x21
    }
}
