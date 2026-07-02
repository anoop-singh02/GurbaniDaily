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
import com.anoop.gurbanidaily.data.DailyQuote

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

        val slotLabel = inputData.getString(ReminderScheduler.KEY_SLOT_LABEL)
            ?: ctx.getString(R.string.reminder_title)

        // Pull today's shabad from BaniDB (cached if already fetched today)
        val shabad = DailyQuote.getForToday(ctx).getOrNull()
            ?: DailyQuote.readCachedShabad(ctx)
            ?: return Result.retry()

        val english = shabad.allEnglish.trim().ifBlank { "Today's shabad is ready." }
        val source = shabad.sourceLabel

        val openApp = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(ctx, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle(slotLabel)
            .setContentText(english.take(120))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$english\n\n— $source")
            )
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notifId = 1000 + (slotLabel.hashCode() and 0x0FFF)
        NotificationManagerCompat.from(ctx).notify(notifId, notif)
        return Result.success()
    }
}
