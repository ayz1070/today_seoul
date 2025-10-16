package com.jun.todayseoul.domain.repository

import com.jun.todayseoul.domain.model.Event

interface EventRepository {
    suspend fun fetchEvents(
        start: Int = DEFAULT_START,
        end: Int = DEFAULT_END
    ): Result<List<Event>>

    companion object {
        const val DEFAULT_START = 1
        const val DEFAULT_END = 500
    }
}
