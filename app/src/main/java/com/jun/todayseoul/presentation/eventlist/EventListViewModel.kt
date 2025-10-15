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
    private var hasLoadedData: Boolean = false

    init {
        observeFilterPreferences()
        refreshEvents()
    }

    fun refreshEvents() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { current ->
                current.copy(screenState = EventListUiState.ScreenState.Loading)
            }

            val currentFilter = uiState.value.filter
            eventRepository.fetchEvents()
                .onSuccess { events ->
                    cachedEvents.value = events
                    hasLoadedData = true
                    if (events.isEmpty()) {
                        _uiState.update { state ->
                            state.copy(
                                filter = currentFilter,
                                screenState = EventListUiState.ScreenState.Empty
                            )
                        }
                    } else {
                        applyFilter(currentFilter, events)
                    }
                }
                .onFailure { throwable ->
                    hasLoadedData = false
                    _uiState.update { state ->
                        state.copy(
                            screenState = EventListUiState.ScreenState.Error(
                                throwable.message?.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
        }
    }

    fun updateDate(date: LocalDate) {
        viewModelScope.launch(ioDispatcher) {
            filterPreferencesRepository.updateDate(date)
        }
    }

    fun updateLocation(query: String?) {
        viewModelScope.launch(ioDispatcher) {
            filterPreferencesRepository.updateLocation(query)
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
        if (!hasLoadedData) {
            _uiState.update { state ->
                state.copy(filter = filter)
            }
            return
        }

        val filteredEvents = filterEventsUseCase(events, filter)
        val screenState = if (filteredEvents.isEmpty()) {
            EventListUiState.ScreenState.Empty
        } else {
            EventListUiState.ScreenState.Success(filteredEvents)
        }

        _uiState.update { state ->
            state.copy(
                filter = filter,
                screenState = screenState
            )
        }
    }

    companion object {
        private val DEFAULT_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
