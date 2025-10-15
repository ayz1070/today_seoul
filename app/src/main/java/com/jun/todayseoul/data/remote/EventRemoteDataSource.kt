package com.jun.todayseoul.data.remote

import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.repository.EventRepository

interface EventRemoteDataSource {
    suspend fun fetchEvents(
        start: Int = DEFAULT_START,
        end: Int = DEFAULT_END
    ): List<Event>

    companion object {
        const val DEFAULT_START: Int = EventRepository.DEFAULT_START
        const val DEFAULT_END: Int = EventRepository.DEFAULT_END
    }
}
