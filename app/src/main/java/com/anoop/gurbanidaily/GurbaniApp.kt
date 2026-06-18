package com.anoop.gurbanidaily

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.anoop.gurbanidaily.data.UserPrefs
import com.anoop.gurbanidaily.notifications.ReminderScheduler

class GurbaniApp : Application() {

    val prefs: UserPrefs by lazy { UserPrefs(this) }

    override fun onCreate() {
        super.onCreate()
        createReminderChannel()
    }

    private fun createReminderChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            ReminderScheduler.CHANNEL_ID,
            getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.reminder_channel_desc)
        }
        mgr.createNotificationChannel(channel)
    }
}
