package com.anoop.gurbanidaily

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.anoop.gurbanidaily.data.UserPrefs
import com.anoop.gurbanidaily.notifications.AutoUpdateChecker
import com.anoop.gurbanidaily.notifications.ReminderScheduler
import com.anoop.gurbanidaily.notifications.SangrandScheduler
import com.anoop.gurbanidaily.widget.WidgetRefreshScheduler

class GurbaniApp : Application() {

    val prefs: UserPrefs by lazy { UserPrefs(this) }

    override fun onCreate() {
        super.onCreate()
        createChannels()
        WidgetRefreshScheduler.scheduleDaily(this)
        WidgetRefreshScheduler.scheduleNow(this)
        SangrandScheduler.scheduleDaily(this)
        AutoUpdateChecker.scheduleWeekly(this)
    }

    private fun createChannels() {
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(
            NotificationChannel(
                ReminderScheduler.CHANNEL_ID,
                getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = getString(R.string.reminder_channel_desc) }
        )
        mgr.createNotificationChannel(
            NotificationChannel(
                SangrandScheduler.CHANNEL_ID,
                getString(R.string.sangrand_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = getString(R.string.sangrand_channel_desc) }
        )
        mgr.createNotificationChannel(
            NotificationChannel(
                AutoUpdateChecker.CHANNEL_ID,
                getString(R.string.updates_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.updates_channel_desc) }
        )
    }
}
