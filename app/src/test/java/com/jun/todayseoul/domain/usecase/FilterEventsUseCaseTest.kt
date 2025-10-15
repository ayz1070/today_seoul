package com.jun.todayseoul.domain.usecase

import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.model.EventFilter
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterEventsUseCaseTest {

    private val useCase = FilterEventsUseCase()
    private val targetDate = LocalDate.of(2024, 10, 13)

    @Test
    fun `filters events by exact date string`() {
        val events = listOf(
            createEvent(title = "Match", date = "2024-10-13 19:00"),
            createEvent(title = "No Match", date = "2024-10-12 19:00")
        )

        val result = useCase(events, EventFilter(date = targetDate))

        assertEquals(listOf("Match"), result.map { it.title })
    }

    @Test
    fun `filters events by date range`() {
        val events = listOf(
            createEvent(title = "In Range", date = "2024.10.10 ~ 2024.10.15"),
            createEvent(title = "Out Of Range", date = "2024.10.14 ~ 2024.10.15")
        )

        val result = useCase(events, EventFilter(date = targetDate))

        assertEquals(listOf("In Range"), result.map { it.title })
    }

    @Test
    fun `filters by search across title and location`() {
        val events = listOf(
            createEvent(title = "서울숲 걷기", date = "2024-10-13", guName = "성동구"),
            createEvent(title = "도심 산책", date = "2024-10-13", guName = null, place = "서울숲"),
            createEvent(title = "한강 러닝", date = "2024-10-13", guName = "서초구")
        )

        val result = useCase(events, EventFilter(date = targetDate, searchQuery = "서울"))

        assertEquals(listOf("서울숲 걷기", "도심 산책"), result.map { it.title })
    }

    @Test
    fun `sorts by start time then title`() {
        val events = listOf(
            createEvent(title = "Evening Show", date = "2024-10-13 20:00"),
            createEvent(title = "Morning Class", date = "2024-10-13 09:00"),
            createEvent(title = "No Time A", date = "2024-10-13"),
            createEvent(title = "No Time B", date = "2024-10-13")
        )

        val result = useCase(events, EventFilter(date = targetDate))

        assertEquals(listOf("Morning Class", "Evening Show", "No Time A", "No Time B"), result.map { it.title })
    }

    @Test
    fun `supports korean date formats`() {
        val events = listOf(
            createEvent(title = "Korean Format", date = "2024년 10월 13일 19:00"),
            createEvent(title = "Korean Month Day", date = "10월 14일 10:00")
        )

        val result = useCase(events, EventFilter(date = targetDate))

        assertEquals(listOf("Korean Format"), result.map { it.title })
    }

    private fun createEvent(
        title: String,
        date: String,
        guName: String? = null,
        place: String? = null
    ) = Event(
        title = title,
        codeName = null,
        date = date,
        useFee = null,
        orgLink = null,
        guName = guName,
        place = place
    )
}
