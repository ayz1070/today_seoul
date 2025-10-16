package com.jun.todayseoul

import android.app.Application
import com.jun.todayseoul.widget.TodaySeoulWidgetRefreshWorker

/**
 * 앱 전역 초기화를 담당하며 위젯 주기 갱신 작업을 등록한다.
 */
class TodaySeoulApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TodaySeoulWidgetRefreshWorker.schedule(this)
    }
}
