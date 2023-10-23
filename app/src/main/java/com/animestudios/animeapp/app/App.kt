package com.animestudios.animeapp.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDex
import androidx.work.*
import com.animestudios.animeapp.initializeNetwork
import com.animestudios.animeapp.worker.NotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
@SuppressLint("StaticFieldLeak")
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(mFTActivityLifecycleCallbacks)
        initializeNetwork(baseContext)
        createNotificationChannel()
        setupNotificationWorker()
    }

    private fun setupNotificationWorker() {
        // Check if there is an existing periodic work request with the specified tag
        val workInfos = WorkManager.getInstance(this).getWorkInfosByTag(TAG_PERIODIC_WORK_REQUEST)
        val hasExistingWorkRequest = workInfos.get().isNotEmpty()

        // Schedule a new periodic work request only if there is no existing one
        if (!hasExistingWorkRequest) {
            val work = createPeriodicWorkerRequest(
                Frequency(
                    repeatInterval = 10,
                    repeatIntervalTimeUnit = TimeUnit.MINUTES
                )
            )

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                TAG_PERIODIC_WORK_REQUEST,
                ExistingPeriodicWorkPolicy.KEEP,
                work
            )
        }
    }

    private fun createPeriodicWorkerRequest(
        frequency: Frequency
    ): PeriodicWorkRequest {
        val constraints = getWorkerConstrains()

        return PeriodicWorkRequestBuilder<NotificationWorker>(
            frequency.repeatInterval,
            frequency.repeatIntervalTimeUnit
        ).apply {
            setConstraints(constraints)
            addTag(TAG_PERIODIC_WORK_REQUEST)
        }.build()
    }

    private fun getWorkerConstrains() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(false)
        .build()
    private fun createNotificationChannel() {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SozoNotifications"
            val descriptionText = "Sozo notifications for anime airing"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(SOZO_NOTIFICATIONS_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    init {
        instance = this
    }


    val mFTActivityLifecycleCallbacks = FTActivityLifecycleCallbacks()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    inner class FTActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        var currentActivity: Activity? = null
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
        override fun onActivityStarted(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityResumed(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityPaused(p0: Activity) {}
        override fun onActivityStopped(p0: Activity) {}
        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
        override fun onActivityDestroyed(p0: Activity) {}
    }


    companion object {
        const val SOZO_NOTIFICATIONS_CHANNEL_ID = "SOZO_NOTIFICATIONS_CHANNEL_ID"
        const val TAG_PERIODIC_WORK_REQUEST = "periodic_work_request"
        private var instance: App? = null
        var context: Context? = null
        fun currentContext(): Context? {
            return instance?.mFTActivityLifecycleCallbacks?.currentActivity ?: context
        }

        fun currentActivity(): Activity? {
            return instance?.mFTActivityLifecycleCallbacks?.currentActivity
        }
    }

    override fun getWorkManagerConfiguration()= Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .setMinimumLoggingLevel(Log.DEBUG)
        .build()
}