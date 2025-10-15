package com.jun.todayseoul.domain.model

import java.time.LocalDate

data class EventFilter(
    val date: LocalDate,
    val locationQuery: String? = null
)
