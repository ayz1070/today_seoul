package com.jun.todayseoul.presentation.eventlist

// EventListViewModel 생성에 필요한 의존성을 주입하는 Factory.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jun.todayseoul.domain.repository.EventRepository
import com.jun.todayseoul.domain.repository.FilterPreferencesRepository
import com.jun.todayseoul.domain.usecase.FilterEventsUseCase

class EventListViewModelFactory(
    private val eventRepository: EventRepository,
    private val filterPreferencesRepository: FilterPreferencesRepository,
    private val filterEventsUseCase: FilterEventsUseCase = FilterEventsUseCase()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventListViewModel::class.java)) {
            return EventListViewModel(
                eventRepository = eventRepository,
                filterPreferencesRepository = filterPreferencesRepository,
                filterEventsUseCase = filterEventsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
