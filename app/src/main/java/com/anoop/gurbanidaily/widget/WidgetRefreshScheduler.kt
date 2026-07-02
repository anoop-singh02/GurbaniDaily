package com.anoop.gurbanidaily.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anoop.gurbanidaily.data.DailyQuote
import java.util.concurrent.TimeUnit

/** Schedules a daily worker that fetches a new random shabad and refreshes the widget. */
object WidgetRefreshScheduler {

    private const val PERIODIC = "daily_quote_periodic"
    private const val ONCE = "daily_quote_once"

    fun scheduleDaily(context: Context) {
        val periodic = PeriodicWorkRequestBuilder<DailyQuoteWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic
        )
    }

    /** Kick off an immediate fetch (e.g. app just opened, we want fresh data). */
    fun scheduleNow(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONCE, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DailyQuoteWorker>().build()
        )
    }

    /** Manual refresh — force a NEW shabad even if today's cache exists. */
    fun forceRefresh(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONCE, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DailyQuoteWorker>().build()
        )
    }
}

class DailyQuoteWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val fetched = DailyQuote.getForToday(ctx)
        if (fetched.isSuccess) {
            // Trigger widget to redraw
            val intent = android.content.Intent(ctx, GurbaniWidget::class.java).apply {
                action = GurbaniWidget.ACTION_RENDER
            }
            ctx.sendBroadcast(intent)
        }
        return Result.success()
    }
}
