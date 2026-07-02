package com.anoop.gurbanidaily.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anoop.gurbanidaily.MainActivity
import com.anoop.gurbanidaily.R
import com.anoop.gurbanidaily.data.NanakshahiCalendar
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SangrandScheduler {

    const val CHANNEL_ID = "sangrand_notifications"
    private const val WORK_NAME = "sangrand_daily_check"

    /** Sets up a daily check that fires at ~8 AM local time. */
    fun scheduleDaily(context: Context) {
        val now = Calendar.getInstance()
        val eightAm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = eightAm.timeInMillis - now.timeInMillis
        val request = PeriodicWorkRequestBuilder<SangrandWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}

class SangrandWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val month = NanakshahiCalendar.sangrandStartingToday() ?: return Result.success()

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val openApp = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "${month.gurmukhi} · ${month.english} begins today"
        val body = "${month.greeting}\n\nHappy sangrand of ${month.english} (${month.gurmukhi})."

        val notif = NotificationCompat.Builder(ctx, SangrandScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle(title)
            .setContentText(month.greeting)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val id = 2000 + month.ordinal
        NotificationManagerCompat.from(ctx).notify(id, notif)
        return Result.success()
    }
}
