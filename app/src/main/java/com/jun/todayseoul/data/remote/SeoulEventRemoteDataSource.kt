package com.jun.todayseoul.data.remote

import com.jun.todayseoul.BuildConfig
import com.jun.todayseoul.data.remote.api.CulturalEventService
import com.jun.todayseoul.data.remote.dto.toDomainEvents
import com.jun.todayseoul.domain.model.Event
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SeoulEventRemoteDataSource(
    private val service: CulturalEventService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EventRemoteDataSource {

    override suspend fun fetchEvents(start: Int, end: Int): List<Event> = withContext(dispatcher) {
        require(start > 0 && end >= start) {
            "start($start) must be >= 1 and end($end) must be >= start."
        }

        val apiKey = BuildConfig.SEOUL_OPEN_API_KEY
        if (apiKey.isBlank()) {
            error("SEOUL_OPEN_API_KEY is missing. Add it to local.properties.")
        }

        val response = service.fetchEvents(
            apiKey = apiKey,
            start = start,
            end = end
        )

        response.info?.result?.let { result ->
            val code = result.code
            if (!code.isNullOrBlank() && code != SUCCESS_CODE) {
                val message = result.message.orEmpty()
                error("Seoul Open API error ($code): $message")
            }
        }

        response.info?.rows.orEmpty().toDomainEvents()
    }

    companion object {
        private const val SUCCESS_CODE = "INFO-000"
    }
}
