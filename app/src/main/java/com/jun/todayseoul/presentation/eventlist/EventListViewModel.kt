package com.jun.todayseoul.presentation.eventlist

// 행사 데이터 로딩과 필터 처리 로직을 관리하는 ViewModel.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.model.EventFilter
import com.jun.todayseoul.domain.repository.EventRepository
import com.jun.todayseoul.domain.repository.FilterPreferencesRepository
import com.jun.todayseoul.domain.usecase.FilterEventsUseCase
import java.time.Clock
import java.time.ZoneId
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventListViewModel(
    private val eventRepository: EventRepository,
    private val filterPreferencesRepository: FilterPreferencesRepository,
    private val filterEventsUseCase: FilterEventsUseCase,
    private val clock: Clock = Clock.system(DEFAULT_ZONE_ID),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventListUiState.initial(clock))
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    private val cachedEvents = MutableStateFlow<List<Event>>(emptyList())
    private val loadedEvents = mutableListOf<Event>()
    private var hasLoadedInitial: Boolean = false
    private var isLoadingInitial: Boolean = false
    private var isLoadingMore: Boolean = false
    private var hasMoreData: Boolean = true
    private var nextStartIndex: Int = EventRepository.DEFAULT_START

    init {
        observeFilterPreferences()
        refreshEvents()
    }

    fun refreshEvents() {
        loadEvents(reset = true)
    }

    fun loadNextPage() {
        loadEvents(reset = false)
    }

    fun updateDate(date: LocalDate) {
        viewModelScope.launch(ioDispatcher) {
            filterPreferencesRepository.updateDate(date)
        }
    }

    fun updateSearchQuery(query: String?) {
        viewModelScope.launch(ioDispatcher) {
            filterPreferencesRepository.updateSearchQuery(query)
        }
    }

    private fun observeFilterPreferences() {
        viewModelScope.launch {
            filterPreferencesRepository.filterFlow.collect { filter ->
                applyFilter(filter, cachedEvents.value)
            }
        }
    }

    private fun applyFilter(filter: EventFilter, events: List<Event>) {
        if (!hasLoadedInitial && events.isEmpty()) {
            _uiState.update { state ->
                state.copy(filter = filter)
            }
            return
        }

        val filteredEvents = filterEventsUseCase(
            events = events,
            filter = filter,
            sort = false
        )
        val screenState = if (filteredEvents.isEmpty()) {
            EventListUiState.ScreenState.Empty
        } else {
            EventListUiState.ScreenState.Success(
                events = filteredEvents,
                isLoadingMore = isLoadingMore,
                hasMoreData = hasMoreData
            )
        }

        _uiState.update { state ->
            state.copy(
                filter = filter,
                screenState = screenState
            )
        }
    }

    private fun loadEvents(reset: Boolean) {
        if (reset) {
            if (isLoadingInitial) return
            isLoadingInitial = true
            isLoadingMore = false
            hasMoreData = true
            hasLoadedInitial = false
            nextStartIndex = EventRepository.DEFAULT_START
            loadedEvents.clear()
            cachedEvents.value = emptyList()
            _uiState.update { current ->
                current.copy(screenState = EventListUiState.ScreenState.Loading)
            }
        } else {
            if (isLoadingInitial || isLoadingMore || !hasMoreData) {
                return
            }
            isLoadingMore = true
            applyFilter(uiState.value.filter, cachedEvents.value)
        }

        val start = nextStartIndex
        val end = start + PAGE_SIZE - 1

        viewModelScope.launch(ioDispatcher) {
            eventRepository.fetchEvents(start = start, end = end)
                .onSuccess { events ->
                    if (reset) {
                        isLoadingInitial = false
                        hasLoadedInitial = true
                    } else {
                        isLoadingMore = false
                    }

                    if (events.isEmpty()) {
                        hasMoreData = false
                        if (loadedEvents.isEmpty()) {
                            _uiState.update { state ->
                                state.copy(
                                    screenState = EventListUiState.ScreenState.Empty
                                )
                            }
                        } else {
                            applyFilter(uiState.value.filter, cachedEvents.value)
                        }
                        return@onSuccess
                    }

                    loadedEvents.addAll(events)
                    cachedEvents.value = loadedEvents.toList()
                    nextStartIndex = end + 1
                    hasMoreData = events.size >= PAGE_SIZE
                    applyFilter(uiState.value.filter, cachedEvents.value)
                }
                .onFailure { throwable ->
                    if (reset) {
                        isLoadingInitial = false
                        hasLoadedInitial = false
                        _uiState.update { state ->
                            state.copy(
                                screenState = EventListUiState.ScreenState.Error(
                                    throwable.message?.takeIf { it.isNotBlank() }
                                )
                            )
                        }
                    } else {
                        isLoadingMore = false
                        hasMoreData = false
                        applyFilter(uiState.value.filter, cachedEvents.value)
                    }
                }
        }
    }

    companion object {
        private val DEFAULT_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        private const val PAGE_SIZE = 500
    }
}
