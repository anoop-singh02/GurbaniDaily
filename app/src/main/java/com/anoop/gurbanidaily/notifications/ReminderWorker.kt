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
import androidx.work.WorkerParameters
import com.anoop.gurbanidaily.MainActivity
import com.anoop.gurbanidaily.R
import com.anoop.gurbanidaily.data.ShabadPicker

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }
        val shabad = ShabadPicker.shabadForToday()
        val openApp = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(ctx, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle(ctx.getString(R.string.reminder_title))
            .setContentText(shabad.meaning.take(100))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${shabad.gurmukhi}\n\n${shabad.meaning}\n\n— ${shabad.source}")
            )
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(ctx).notify(1001, notif)
        return Result.success()
    }
}
