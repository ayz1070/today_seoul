package com.jun.todayseoul.presentation.eventlist

// 행사 목록 화면에서 사용하는 UI 상태 모델과 화면 상태 정의.

import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.model.EventFilter
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

data class EventListUiState(
    val filter: EventFilter,
    val screenState: ScreenState
) {
    companion object {
        fun initial(clock: Clock = Clock.system(DEFAULT_ZONE_ID)): EventListUiState {
            val today = LocalDate.now(clock)
            return EventListUiState(
                filter = EventFilter(date = today),
                screenState = ScreenState.Loading
            )
        }

        private val DEFAULT_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Success(val events: List<Event>) : ScreenState
        data object Empty : ScreenState
        data class Error(val message: String?) : ScreenState
    }
}
