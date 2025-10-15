package com.jun.todayseoul.presentation.eventlist

import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.model.EventFilter
import com.jun.todayseoul.domain.repository.EventRepository
import com.jun.todayseoul.domain.repository.FilterPreferencesRepository
import com.jun.todayseoul.domain.usecase.FilterEventsUseCase
import com.jun.todayseoul.testing.MainDispatcherRule
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private val zoneId: ZoneId = ZoneId.of("Asia/Seoul")
    private val baseDate: LocalDate = LocalDate.of(2024, 10, 13)
    private val testClock: Clock = Clock.fixed(baseDate.atStartOfDay(zoneId).toInstant(), zoneId)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Test
    fun `refreshEvents success updates state with filtered events`() = runTest(testDispatcher) {
        val initialFilter = EventFilter(date = baseDate)
        val eventRepository = FakeEventRepository(
            Result.success(
                listOf(
                    Event(title = "Matching Event", codeName = null, date = "2024-10-13 18:00", useFee = null, orgLink = null, guName = "강남구", place = null),
                    Event(title = "Other Day", codeName = null, date = "2024-10-14", useFee = null, orgLink = null, guName = "강남구", place = null)
                )
            )
        )
        val filterRepository = FakeFilterPreferencesRepository(initialFilter)

        val viewModel = createViewModel(eventRepository, filterRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.screenState is EventListUiState.ScreenState.Success)
        val events = (state.screenState as EventListUiState.ScreenState.Success).events
        assertEquals(listOf("Matching Event"), events.map { it.title })
    }

    @Test
    fun `refreshEvents failure exposes error state`() = runTest(testDispatcher) {
        val initialFilter = EventFilter(date = baseDate)
        val eventRepository = FakeEventRepository(Result.failure(IllegalStateException("Network down")))
        val filterRepository = FakeFilterPreferencesRepository(initialFilter)

        val viewModel = createViewModel(eventRepository, filterRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.screenState is EventListUiState.ScreenState.Error)
    }

    @Test
    fun `updating search query updates filtered events`() = runTest(testDispatcher) {
        val initialFilter = EventFilter(date = baseDate)
        val events = listOf(
            Event(title = "Seoul Forest", codeName = null, date = "2024-10-13 10:00", useFee = null, orgLink = null, guName = null, place = "서울숲"),
            Event(title = "City Hall", codeName = null, date = "2024-10-13 11:00", useFee = null, orgLink = null, guName = "중구", place = "시청")
        )
        val eventRepository = FakeEventRepository(Result.success(events))
        val filterRepository = FakeFilterPreferencesRepository(initialFilter)

        val viewModel = createViewModel(eventRepository, filterRepository)
        advanceUntilIdle()

        viewModel.updateSearchQuery("서울숲")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.screenState is EventListUiState.ScreenState.Success)
        val filteredEvents = (state.screenState as EventListUiState.ScreenState.Success).events
        assertEquals(listOf("Seoul Forest"), filteredEvents.map { it.title })
    }

    private fun createViewModel(
        eventRepository: EventRepository,
        filterPreferencesRepository: FilterPreferencesRepository
    ): EventListViewModel = EventListViewModel(
        eventRepository = eventRepository,
        filterPreferencesRepository = filterPreferencesRepository,
        filterEventsUseCase = FilterEventsUseCase(),
        clock = testClock,
        ioDispatcher = testDispatcher
    )

    private class FakeEventRepository(
        private var result: Result<List<Event>>
    ) : EventRepository {
        override suspend fun fetchEvents(start: Int, end: Int): Result<List<Event>> = result
    }

    private class FakeFilterPreferencesRepository(
        initial: EventFilter
    ) : FilterPreferencesRepository {
        private val state = MutableStateFlow(initial)

        override val filterFlow: Flow<EventFilter> = state

        override suspend fun updateDate(date: LocalDate) {
            state.update { current -> current.copy(date = date) }
        }

        override suspend fun updateSearchQuery(query: String?) {
            state.update { current -> current.copy(searchQuery = query) }
        }
    }
}
