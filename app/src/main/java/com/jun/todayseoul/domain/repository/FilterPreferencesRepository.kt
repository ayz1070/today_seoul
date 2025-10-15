package com.jun.todayseoul.domain.repository

import com.jun.todayseoul.domain.model.EventFilter
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface FilterPreferencesRepository {
    val filterFlow: Flow<EventFilter>
    suspend fun updateDate(date: LocalDate)
    suspend fun updateSearchQuery(query: String?)
}
