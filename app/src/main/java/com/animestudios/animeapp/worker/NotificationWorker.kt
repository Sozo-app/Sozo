package com.animestudios.animeapp.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import com.animestudios.animeapp.mapper.convert
import com.animestudios.animeapp.model.PagingDataItem
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.ui.activity.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
 private   val   aniListGraphQlClient: AniListClient,
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
            println("Hatolik Bo`ldi : ${e.message}")
            Result.failure()
        }
    }

    private fun isNotificationIdStored(id: Int?): Boolean =
        readData<Boolean>(id.toString()) ?: false

    private fun storeNotificationId(id: Int?) = saveData(id.toString(), true)
    private fun buildPendingIntent(notification: com.animestudios.animeapp.model.Notification): PendingIntent {
        val args = bundleOf(
            "animeDetails" to notification.media,
            if (notification.episode != null) {
                "desiredPosition" to notification.episode
            } else {
                "desiredPosition" to -1
            }
        )

        return NavDeepLinkBuilder(applicationContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.app_graph)
            .setDestination(R.id.mainScreen)
            .setArguments(args)
            .createPendingIntent()
    }


    private fun showNotification(notification: com.animestudios.animeapp.model.Notification) {
        val pendingIntent = buildPendingIntent(notification)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("New Notification")
            .setContentText(notification.getFormattedNotification())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        sendNotification(builder)
    }

    private fun sendNotification(builder: NotificationCompat.Builder) {
        NotificationManagerCompat.from(applicationContext).apply {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private suspend fun fetchLatestNotification(): com.animestudios.animeapp.model.Notification? {
        val response = aniListGraphQlClient.getNotifications(1).data?.convert()
        println("Is workkingggg ")
        return response?.flatMap {
            when (it) {
                is PagingDataItem.NotificationItem -> listOf(it.notification)
                else -> emptyList()
            }
        }?.maxByOrNull { it.id ?: -1 }
    }

    companion object {
        private const val TAG = "NotificationWorker"
        private const val CHANNEL_ID = "ANIMITY_NOTIFICATIONS_CHANNEL_ID"
        private const val NOTIFICATION_ID = 0x1
    }

}