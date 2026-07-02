package com.anoop.gurbanidaily.widget

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anoop.gurbanidaily.data.DailyQuote
import java.util.concurrent.TimeUnit

object WidgetRefreshScheduler {

    private const val PERIODIC = "daily_quote_periodic"
    private const val ONCE = "daily_quote_once"
    const val KEY_FORCE = "force_new"

    fun scheduleDaily(context: Context) {
        val periodic = PeriodicWorkRequestBuilder<DailyQuoteWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic
        )
    }

    /** Ensure widget has a shabad — use today's cache if it exists. */
    fun scheduleNow(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONCE, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DailyQuoteWorker>().build()
        )
    }

    /** User tapped the widget's ↻ — force a brand-new shabad. */
    fun forceRefresh(context: Context) {
        val work = OneTimeWorkRequestBuilder<DailyQuoteWorker>()
            .setInputData(Data.Builder().putBoolean(KEY_FORCE, true).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONCE, ExistingWorkPolicy.REPLACE, work)
    }
}

class DailyQuoteWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val force = inputData.getBoolean(WidgetRefreshScheduler.KEY_FORCE, false)
        val fetched = if (force) DailyQuote.forceNew(ctx) else DailyQuote.getForToday(ctx)
        if (fetched.isSuccess) {
            val intent = Intent(ctx, GurbaniWidget::class.java).apply {
                action = GurbaniWidget.ACTION_RENDER
            }
            ctx.sendBroadcast(intent)
        }
        return Result.success()
    }
}
