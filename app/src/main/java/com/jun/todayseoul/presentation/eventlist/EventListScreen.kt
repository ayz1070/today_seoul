package com.jun.todayseoul.presentation.eventlist

// 행사 목록 화면의 전체 레이아웃과 상태 처리를 담당하는 Compose UI.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jun.todayseoul.core.theme.TodaySeoulTheme
import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.domain.model.EventFilter
import com.jun.todayseoul.presentation.eventlist.component.EventFilterSection
import com.jun.todayseoul.presentation.eventlist.component.EventListContent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    // 화면에 표시할 전체 상태(필터 + 리스트 상태 등)
    uiState: EventListUiState,
    // 날짜 선택 후 ViewModel에 새 날짜를 전달하는 콜백
    onSelectDate: (LocalDate) -> Unit,
    // 지역 검색 입력 변화를 ViewModel에 전달하는 콜백 (빈 값은 null)
    onLocationChange: (String?) -> Unit,
    // 데이터 요청을 재시도할 때 호출되는 콜백
    onRetry: () -> Unit,
    // 행사 항목을 눌렀을 때 상세 링크를 여는 콜백
    onOpenLink: (String) -> Unit,
    // 외부에서 레이아웃 속성을 덧붙이기 위한 Modifier
    modifier: Modifier = Modifier
) {
    // 서울 기준의 날짜 계산 및 화면 상태를 저장하는 기본 값들
    val zoneId = ZoneId.of("Asia/Seoul")
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var locationQuery by rememberSaveable(uiState.filter.locationQuery) {
        mutableStateOf(uiState.filter.locationQuery.orEmpty())
    }

    // 필터에 선택된 날짜를 화면용 문자열로 변환
    val dateDisplay = remember(uiState.filter.date) {
        displayDateFormatter.format(uiState.filter.date)
    }

    // 날짜 선택 다이얼로그를 필요할 때만 구성
    if (showDatePicker) {
        val initialMillis = uiState.filter.date.toEpochMillis(zoneId)
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis?.toLocalDate(zoneId)
                        if (selectedDate != null) {
                            onSelectDate(selectedDate)
                            showDatePicker = false
                        }
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(text = "선택")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = "취소")
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    // 상단 앱바와 내용 영역을 가지는 기본 스캐폴드
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "오늘은뭐하지-서울") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            EventFilterSection(
                dateDisplay = dateDisplay,
                onDateClick = { showDatePicker = true },
                locationValue = locationQuery,
                onLocationChange = { newValue ->
                    locationQuery = newValue
                    onLocationChange(newValue.ifBlank { null })
                }
            )

            // 요청된 상태에 따라 행사 목록/로딩/오류 UI를 출력
            EventListContent(
                screenState = uiState.screenState,
                onRetry = onRetry,
                onOpenLink = onOpenLink
            )
        }
    }
}

private fun LocalDate.toEpochMillis(zoneId: ZoneId): Long =
    this.atStartOfDay(zoneId).toInstant().toEpochMilli()

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

private val displayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREA)


@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventListScreenPreview() {
    TodaySeoulTheme {
        EventListScreen(
            uiState = EventListUiState(
                filter = EventFilter(
                    date = LocalDate.of(2024, 10, 13),
                    locationQuery = "강남"
                ),
                screenState = EventListUiState.ScreenState.Success(
                    events = listOf(
                        Event(
                            title = "서울숲 산책 콘서트",
                            codeName = "공연",
                            date = "2024-10-13 17:00",
                            useFee = "무료",
                            orgLink = "https://example.com/event1",
                            guName = "성동구",
                            place = "서울숲 야외무대"
                        ),
                        Event(
                            title = "한강 요가 클래스",
                            codeName = "체험",
                            date = "2024-10-13 10:30",
                            useFee = "1인 10,000원",
                            orgLink = "https://example.com/event2",
                            guName = "영등포구",
                            place = "여의도한강공원"
                        )
                    )
                )
            ),
            onSelectDate = {},
            onLocationChange = {},
            onRetry = {},
            onOpenLink = {}
        )
    }
}
