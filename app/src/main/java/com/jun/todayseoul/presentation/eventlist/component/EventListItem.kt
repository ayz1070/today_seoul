package com.jun.todayseoul.presentation.eventlist.component

// 단일 행사 카드를 그려주는 재사용 가능한 리스트 아이템.

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jun.todayseoul.core.theme.TodaySeoulTheme
import com.jun.todayseoul.domain.model.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListItem(
    event: Event,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val canOpenLink = event.orgLink?.isNotBlank() == true
    val locationLabel = listOfNotNull(
        event.guName?.takeIf { it.isNotBlank() },
        event.place?.takeIf { it.isNotBlank() }
    ).joinToString(" ").takeIf { it.isNotBlank() }

    val contextDescription = buildString {
        append(event.title.orEmpty())
        event.date?.takeIf { it.isNotBlank() }?.let {
            append(", 일정: ")
            append(it)
        }
        locationLabel?.let {
            append(", 위치: ")
            append(it)
        }
        event.useFee?.takeIf { it.isNotBlank() }?.let {
            append(", 이용료: ")
            append(it)
        }
    }

    Card(
        onClick = {
            if (canOpenLink) {
                event.orgLink?.let(onOpenLink)
            }
        },
        enabled = canOpenLink,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = RectangleShape,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    if (contextDescription.isNotBlank()) {
                        contentDescription = contextDescription
                    }
                },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PosterSection(
                title = event.title.orEmpty(),
                rawDate = event.date,
                locationLabel = locationLabel,
                useFee = event.useFee,
                imageUrl = event.imageUrl
            )
        }
    }
}

@Composable
private fun PosterSection(
    title: String,
    rawDate: String?,
    imageUrl: String?,
    locationLabel: String?,
    useFee: String?
) {
    val formattedDate = formatEventDate(rawDate)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title.ifBlank { "제목 미정" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            formattedDate?.let { date ->
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            locationLabel?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "위치: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    )
            }
            useFee?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "가격: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,

                    )
            }
        }
    }
}

private val outputDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yy.MM.dd", Locale.KOREA)

private val dateTimeInputFormatters: List<DateTimeFormatter> =
    listOf(
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy.MM.dd HH:mm",
        "yyyy/MM/dd HH:mm"
    ).map { pattern ->
        DateTimeFormatter.ofPattern(pattern, Locale.KOREA)
    }

private val dateInputFormatters: List<DateTimeFormatter> =
    listOf(
        "yyyy-MM-dd",
        "yyyy.MM.dd",
        "yyyy/MM/dd",
        "yyyy년 M월 d일",
        "yyyyMMdd"
    ).map { pattern ->
        DateTimeFormatter.ofPattern(pattern, Locale.KOREA)
    }

private val numericDateRegex = Regex("(\\d{4})[.\\-/년 ]?(\\d{1,2})[.\\-/월 ]?(\\d{1,2})")

private fun formatEventDate(raw: String?): String? {
    if (raw.isNullOrBlank()) return null

    val parts = raw.split("~").map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) return null

    val formattedParts = parts.map { part ->
        parseEventDatePart(part)?.let(outputDateFormatter::format) ?: part
    }

    return if (formattedParts.size > 1) {
        formattedParts.joinToString(" ~ ")
    } else {
        formattedParts.first()
    }
}

private fun parseEventDatePart(part: String): LocalDate? {
    val trimmed = part.trim()

    dateTimeInputFormatters.forEach { formatter ->
        runCatching {
            LocalDateTime.parse(trimmed, formatter).toLocalDate()
        }.onSuccess { return it }
    }

    dateInputFormatters.forEach { formatter ->
        runCatching {
            LocalDate.parse(trimmed, formatter)
        }.onSuccess { return it }
    }

    numericDateRegex.find(trimmed)?.let { match ->
        val (year, month, day) = match.destructured
        return runCatching {
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        }.getOrNull()
    }

    return runCatching { LocalDate.parse(trimmed) }.getOrNull()
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventListItemPreview() {
    TodaySeoulTheme {
        EventListItem(
            event = Event(
                title = "서울숲 산책 콘서트",
                codeName = "공연",
                date = "2025-11-11 ~ 2025-12-12",
                useFee = "무료",
                orgLink = "https://example.com/event1",
                guName = "성동구",
                place = "서울숲 야외무대",
                imageUrl = "https://picsum.photos/seed/preview/800/1200"
            ),
            onOpenLink = {}
        )
    }
}
