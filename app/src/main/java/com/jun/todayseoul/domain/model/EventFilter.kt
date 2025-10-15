package com.jun.todayseoul.domain.model

import java.time.LocalDate

data class EventFilter(
    val date: LocalDate,
    val searchQuery: String? = null
)
