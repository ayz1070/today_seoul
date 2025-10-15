package com.jun.todayseoul.domain.usecase

import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.model.EventFilter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Filters and sorts events based on the selected date and an optional location query.
 * - Date filtering tolerates loosely formatted date strings from the API.
 * - Location filtering checks both `guName` and `place`.
 * - Sorting prioritises events with explicit start times, then falls back to title order.
 */
class FilterEventsUseCase {

    operator fun invoke(
        events: List<Event>,
        filter: EventFilter
    ): List<Event> {
        val normalizedLocation = filter.locationQuery?.trim().orEmpty()

        return events
            .asSequence()
            .filter { it.matchesDate(filter.date) }
            .filter { it.matchesLocation(normalizedLocation) }
            .sortedWith(eventComparator)
            .toList()
    }

    private fun Event.matchesLocation(query: String): Boolean {
        if (query.isEmpty()) return true
        val loweredQuery = query.lowercase(Locale.getDefault())
        return listOfNotNull(guName, place).any { location ->
            location?.lowercase(Locale.getDefault())?.contains(loweredQuery) == true
        }
    }

    private fun Event.matchesDate(targetDate: LocalDate): Boolean {
        val dateText = date?.trim().orEmpty()
        if (dateText.isEmpty()) return false

        if (dateContainsFormattedDate(dateText, targetDate)) {
            return true
        }

        val digitsOnly = dateText.filter { it.isDigit() }
        if (digitsOnly.length >= DATE_DIGIT_LENGTH) {
            val targetDigits = targetDate.format(DateTimeFormatter.BASIC_ISO_DATE)
            if (digitsOnly.contains(targetDigits)) {
                return true
            }

            val matches = eightDigitRegex.findAll(digitsOnly).map { it.value }.toList()
            if (matches.size >= 2) {
                val start = matches.first().toLocalDateOrNull() ?: return false
                val end = matches.last().toLocalDateOrNull() ?: return false
                val normalizedEnd = if (end.isBefore(start)) end.plusYears(1) else end
                if (!targetDate.isBefore(start) && !targetDate.isAfter(normalizedEnd)) {
                    return true
                }
            }
        }

        return false
    }

    private fun dateContainsFormattedDate(dateText: String, targetDate: LocalDate): Boolean {
        return targetDateFormats(targetDate).any { formatted ->
            dateText.contains(formatted)
        }
    }

    private fun targetDateFormats(date: LocalDate): List<String> = listOf(
        date.format(DateTimeFormatter.ISO_DATE),
        date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
        date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
        date.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        date.format(DateTimeFormatter.ofPattern("MM-dd")),
        date.format(DateTimeFormatter.ofPattern("MM/dd")),
        date.format(DateTimeFormatter.ofPattern("MM.dd")),
        date.format(koreanFullDateFormatter),
        date.format(koreanMonthDayFormatter)
    )

    private fun String.toLocalDateOrNull(): LocalDate? = runCatching {
        LocalDate.parse(this, DateTimeFormatter.BASIC_ISO_DATE)
    }.getOrNull()

    private val eventComparator = Comparator<Event> { first, second ->
        val firstTime = first.extractStartTime()
        val secondTime = second.extractStartTime()

        when {
            firstTime != null && secondTime != null -> {
                val timeComparison = firstTime.compareTo(secondTime)
                if (timeComparison != 0) {
                    timeComparison
                } else {
                    first.title.orEmpty().compareTo(second.title.orEmpty())
                }
            }

            firstTime != null -> -1
            secondTime != null -> 1
            else -> first.title.orEmpty().compareTo(second.title.orEmpty())
        }
    }

    private fun Event.extractStartTime(): LocalTime? {
        val dateText = date ?: return null
        val matchResult = timeRegex.find(dateText) ?: return null
        val hour = matchResult.groupValues.getOrNull(1)?.padStart(2, '0') ?: return null
        val minute = matchResult.groupValues.getOrNull(2) ?: return null
        val normalized = "$hour:$minute"
        return runCatching {
            LocalTime.parse(normalized, timeFormatter)
        }.getOrNull()
    }

    private companion object {
        private const val DATE_DIGIT_LENGTH = 8

        private val timeRegex = Regex("(\\d{1,2}):(\\d{2})")
        private val eightDigitRegex = Regex("(\\d{8})")
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val koreanFullDateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
        private val koreanMonthDayFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
    }
}
