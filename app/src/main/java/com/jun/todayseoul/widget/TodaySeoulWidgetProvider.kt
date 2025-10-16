package com.jun.todayseoul.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.jun.todayseoul.MainActivity
import com.jun.todayseoul.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * 홈 화면 위젯의 엔트리 포인트. 오늘 진행 중인 행사를 조회해 최상단 이벤트 정보를 보여준다.
 */
class TodaySeoulWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, TodaySeoulWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            updateWidgets(context, manager, ids)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        scope.coroutineContext.cancelChildren()
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return

        appWidgetIds.forEach { appWidgetId ->
            scope.launch {
                val dataResult = widgetRepository.fetchTodayEvents()
                val remoteViews = if (dataResult.isSuccess) {
                    val data = dataResult.getOrNull()
                    val poster = loadPosterBitmap(context, data?.topEvent?.imageUrl)
                    buildContentRemoteViews(context, data, poster)
                } else {
                    buildErrorRemoteViews(context)
                }
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            }
        }
    }

    private fun buildContentRemoteViews(
        context: Context,
        data: TodayWidgetData?,
        poster: Bitmap?
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_todayseoul)

        val eventCount = data?.eventCount ?: 0
        views.setTextViewText(R.id.text_pick_label, "PICK")

        val hasPoster = poster != null
        if (hasPoster) {
            views.setViewVisibility(R.id.image_event_poster, View.VISIBLE)
            views.setImageViewBitmap(R.id.image_event_poster, poster)
            views.setViewVisibility(R.id.text_empty, View.GONE)
        } else {
            val topEventExists = data?.topEvent != null
            if (topEventExists) {
                views.setViewVisibility(R.id.image_event_poster, View.VISIBLE)
                views.setImageViewResource(R.id.image_event_poster, R.drawable.ic_widget_poster_placeholder)
                views.setViewVisibility(R.id.text_empty, View.GONE)
            } else {
                views.setViewVisibility(R.id.image_event_poster, View.GONE)
                views.setViewVisibility(R.id.text_empty, View.VISIBLE)
            }
        }

        val clickIntent = createWidgetPendingIntent(context)
        views.setOnClickPendingIntent(R.id.widget_root, clickIntent)

        return views
    }

    private fun buildErrorRemoteViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_todayseoul)
        views.setTextViewText(R.id.text_pick_label, "PICK")
        views.setViewVisibility(R.id.image_event_poster, View.GONE)
        views.setViewVisibility(R.id.text_empty, View.VISIBLE)
        views.setTextViewText(R.id.text_empty, "잠시 후 다시 시도해주세요")

        val clickIntent = createWidgetPendingIntent(context)
        views.setOnClickPendingIntent(R.id.widget_root, clickIntent)

        return views
    }

    private suspend fun loadPosterBitmap(context: Context, imageUrl: String?): Bitmap? {
        if (imageUrl.isNullOrBlank()) return null

        return runCatching {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(144, 192)
                .allowHardware(false)
                .build()

            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                result.drawable.toBitmap()
            } else {
                null
            }
        }.getOrNull()
    }

    private fun createWidgetPendingIntent(context: Context): PendingIntent {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
            .getPendingIntent(
                CLICK_REQUEST_CODE,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }

    companion object {
        private const val CLICK_REQUEST_CODE = 1001
        private const val ACTION_REFRESH = "com.jun.todayseoul.widget.action.REFRESH"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val widgetRepository = TodayEventsWidgetRepository()

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, TodaySeoulWidgetProvider::class.java)
            val appWidgetIds = manager.getAppWidgetIds(component)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, TodaySeoulWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                    `package` = context.packageName
                }
                context.sendBroadcast(intent)
            }
        }

        fun requestManualRefresh(context: Context) {
            val intent = Intent(context, TodaySeoulWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
                `package` = context.packageName
            }
            context.sendBroadcast(intent)
        }
    }
}
