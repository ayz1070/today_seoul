package com.jun.todayseoul.data.remote.api

import com.jun.todayseoul.data.remote.dto.CulturalEventResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CulturalEventService {
    @GET("{apiKey}/json/culturalEventInfo/{start}/{end}/")
    suspend fun fetchEvents(
        @Path("apiKey") apiKey: String,
        @Path("start") start: Int,
        @Path("end") end: Int
    ): CulturalEventResponseDto
}
