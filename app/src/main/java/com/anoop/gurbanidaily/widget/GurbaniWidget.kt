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
import com.anoop.gurbanidaily.data.DailyQuote

class GurbaniWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) render(context, appWidgetManager, id)
        // Fire a background refresh — WorkManager will run DailyQuoteWorker
        WidgetRefreshScheduler.scheduleNow(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH, ACTION_RENDER -> {
                val mgr = AppWidgetManager.getInstance(context)
                val ids = mgr.getAppWidgetIds(ComponentName(context, GurbaniWidget::class.java))
                for (id in ids) render(context, mgr, id)
                if (intent.action == ACTION_REFRESH) {
                    WidgetRefreshScheduler.forceRefresh(context)
                }
            }
        }
    }

    private fun render(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val shabad = DailyQuote.readCachedShabad(context)
        val english = shabad?.allEnglish?.trim().orEmpty().ifBlank {
            "Tap ↻ to load today's shabad from Sri Guru Granth Sahib Ji."
        }
        val source = shabad?.sourceLabel.orEmpty().ifBlank { "Daily Gurbani" }

        val views = RemoteViews(context.packageName, R.layout.widget_gurbani)
        views.setTextViewText(R.id.widget_english, english)
        views.setTextViewText(R.id.widget_source, source)

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
        const val ACTION_RENDER = "com.anoop.gurbanidaily.WIDGET_RENDER"
    }
}
