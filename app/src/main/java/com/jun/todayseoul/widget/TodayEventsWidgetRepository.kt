package com.jun.todayseoul.widget

import com.jun.todayseoul.core.network.SeoulOpenApiClient
import com.jun.todayseoul.data.remote.SeoulEventRemoteDataSource
import com.jun.todayseoul.data.repository.DefaultEventRepository
import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.model.EventFilter
import com.jun.todayseoul.domain.repository.EventRepository
import com.jun.todayseoul.domain.usecase.FilterEventsUseCase
import java.time.Clock
import java.time.LocalDate

/**
 * 위젯에서 사용할 오늘 날짜의 행사 데이터를 제공한다.
 */
class TodayEventsWidgetRepository(
    private val eventRepository: EventRepository = DefaultEventRepository(
        remoteDataSource = SeoulEventRemoteDataSource(
            service = SeoulOpenApiClient.culturalEventService()
        )
    ),
    private val filterEventsUseCase: FilterEventsUseCase = FilterEventsUseCase(),
    private val clock: Clock = Clock.systemDefaultZone()
) {

    suspend fun fetchTodayEvents(): Result<TodayWidgetData> {
        val today = LocalDate.now(clock)
        return eventRepository.fetchEvents()
            .mapCatching { events ->
                val filtered = filterEventsUseCase(
                    events = events,
                    filter = EventFilter(date = today)
                )
                val randomEvent = filtered.randomOrNull()

                TodayWidgetData(
                    eventCount = filtered.size,
                    topEvent = randomEvent?.toTopEvent()
                )
            }
    }

}

data class TodayWidgetData(
    val eventCount: Int,
    val topEvent: TopEvent?
)

data class TopEvent(
    val title: String,
    val location: String?,
    val imageUrl: String?
)

private fun Event.toTopEvent(): TopEvent {
    val location = listOfNotNull(
        guName?.takeIf { it.isNotBlank() },
        place?.takeIf { it.isNotBlank() }
    ).joinToString(" ").takeIf { it.isNotBlank() }

    return TopEvent(
        title = title?.takeIf { it.isNotBlank() } ?: "제목 미정",
        location = location,
        imageUrl = imageUrl
    )
}
