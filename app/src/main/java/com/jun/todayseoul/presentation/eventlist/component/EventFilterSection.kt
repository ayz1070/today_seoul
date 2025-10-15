package com.jun.todayseoul.presentation.eventlist.component

// 날짜, 지역 입력 필터 UI를 담당하는 컴포저블 모듈.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jun.todayseoul.core.theme.TodaySeoulTheme

@Composable
fun EventFilterSection(
    dateDisplay: String,
    onDateClick: () -> Unit,
    locationValue: String,
    onLocationChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "오늘 참여할 행사를 찾아보세요",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FilledTonalButton(
            onClick = onDateClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = dateDisplay)
        }
        OutlinedTextField(
            value = locationValue,
            onValueChange = onLocationChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("지역 검색 (구/장소)") },
            placeholder = { Text("예: 강남구 또는 서울숲") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onDone = { })
        )
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventFilterSectionPreview() {
    TodaySeoulTheme {
        EventFilterSection(
            dateDisplay = "2024년 10월 13일 (일)",
            onDateClick = {},
            locationValue = "강남",
            onLocationChange = {}
        )
    }
}
