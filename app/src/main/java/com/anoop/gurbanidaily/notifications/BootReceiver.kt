package com.anoop.gurbanidaily.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anoop.gurbanidaily.data.UserPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPrefs(context.applicationContext)
                if (prefs.reminderEnabled.first()) {
                    val hour = prefs.reminderHour.first()
                    val minute = prefs.reminderMinute.first()
                    ReminderScheduler.schedule(context.applicationContext, hour, minute)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
