package com.animestudios.animeapp.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.animestudios.animeapp.anilist.apollo.client.AniListClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationWorkerFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val aniListClient: AniListClient,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            NotificationWorker::class.java.name -> {
                NotificationWorker(appContext, workerParameters,aniListClient)
            }

            else -> null
        }
    }
}
