package com.jun.todayseoul.data.repository

import com.jun.todayseoul.data.remote.EventRemoteDataSource
import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.repository.EventRepository

class DefaultEventRepository(
    private val remoteDataSource: EventRemoteDataSource
) : EventRepository {

    override suspend fun fetchEvents(
        start: Int,
        end: Int
    ): Result<List<Event>> = runCatching {
        remoteDataSource.fetchEvents(start, end)
    }
}
