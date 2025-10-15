package com.jun.todayseoul.presentation.eventlist.component

// 단일 행사 카드를 그려주는 재사용 가능한 리스트 아이템.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jun.todayseoul.core.theme.TodaySeoulTheme
import com.jun.todayseoul.domain.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListItem(
    event: Event,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val canOpenLink = event.orgLink?.isNotBlank() == true
    val location = event.guName ?: event.place
    val contextDescription = buildString {
        append(event.title.orEmpty())
        event.date?.takeIf { it.isNotBlank() }?.let {
            append(", 일정: ")
            append(it)
        }
        location?.takeIf { it.isNotBlank() }?.let {
            append(", 위치: ")
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
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clearAndSetSemantics {
                    if (contextDescription.isNotBlank()) {
                        contentDescription = contextDescription
                    }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = event.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            event.codeName?.takeIf { it.isNotBlank() }?.let { code ->
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            event.date?.takeIf { it.isNotBlank() }?.let { dateText ->
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            location?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "장소: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            event.useFee?.takeIf { it.isNotBlank() }?.let { fee ->
                Text(
                    text = "이용료: $fee",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (canOpenLink) {
                Text(
                    text = "상세 보기",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventListItemPreview() {
    TodaySeoulTheme {
        EventListItem(
            event = Event(
                title = "서울숲 산책 콘서트",
                codeName = "공연",
                date = "2024-10-13 17:00",
                useFee = "무료",
                orgLink = "https://example.com/event1",
                guName = "성동구",
                place = "서울숲 야외무대"
            ),
            onOpenLink = {}
        )
    }
}
