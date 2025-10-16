package com.jun.todayseoul.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * 위젯을 3시간마다 새로고침하도록 스케줄링하는 WorkManager 작업.
 */
class TodaySeoulWidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        TodaySeoulWidgetProvider.requestUpdate(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "TodaySeoulWidgetRefreshWorker"
        private const val REFRESH_INTERVAL_HOURS = 3L

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<TodaySeoulWidgetRefreshWorker>(
                REFRESH_INTERVAL_HOURS,
                TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
