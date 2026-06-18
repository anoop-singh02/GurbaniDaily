package com.anoop.gurbanidaily.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.anoop.gurbanidaily.data.ReminderSlot
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val CHANNEL_ID = "daily_shabad_reminder"
    const val KEY_SLOT_LABEL = "slot_label"
    private fun workName(slot: ReminderSlot) = "daily_shabad_reminder_${slot.key}"

    fun schedule(context: Context, slot: ReminderSlot, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_SLOT_LABEL to slot.label))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(workName(slot), ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context, slot: ReminderSlot) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(slot))
    }
}
