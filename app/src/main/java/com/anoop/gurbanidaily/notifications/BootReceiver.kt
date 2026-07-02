package com.anoop.gurbanidaily.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anoop.gurbanidaily.data.ReminderSlot
import com.anoop.gurbanidaily.data.UserPrefs
import com.anoop.gurbanidaily.widget.WidgetRefreshScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext
                val prefs = UserPrefs(app)
                ReminderSlot.entries.forEach { slot ->
                    val state = prefs.reminder(slot).first()
                    if (state.enabled) {
                        ReminderScheduler.schedule(app, slot, state.hour, state.minute)
                    }
                }
                // Reschedule the daily widget worker + sangrand checker so they
                // survive reboots and package replaces.
                WidgetRefreshScheduler.scheduleDaily(app)
                SangrandScheduler.scheduleDaily(app)
            } finally {
                pending.finish()
            }
        }
    }
}
