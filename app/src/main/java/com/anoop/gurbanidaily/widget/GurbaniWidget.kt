package com.anoop.gurbanidaily.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.anoop.gurbanidaily.MainActivity
import com.anoop.gurbanidaily.R
import com.anoop.gurbanidaily.data.ShabadPicker

class GurbaniWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, GurbaniWidget::class.java))
            for (id in ids) updateWidget(context, mgr, id, shuffle = true)
        }
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        shuffle: Boolean = false
    ) {
        val shabad = if (shuffle) ShabadPicker.randomShabad() else ShabadPicker.shabadForToday()
        val views = RemoteViews(context.packageName, R.layout.widget_gurbani)
        views.setTextViewText(R.id.widget_gurmukhi, shabad.gurmukhi)
        views.setTextViewText(R.id.widget_meaning, shabad.meaning)
        views.setTextViewText(R.id.widget_source, shabad.source)

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, openApp)

        val refresh = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, GurbaniWidget::class.java).apply { action = ACTION_REFRESH },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refresh)

        manager.updateAppWidget(widgetId, views)
    }

    companion object {
        const val ACTION_REFRESH = "com.anoop.gurbanidaily.WIDGET_REFRESH"
    }
}
