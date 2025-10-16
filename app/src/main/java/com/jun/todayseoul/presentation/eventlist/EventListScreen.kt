package com.jun.todayseoul.presentation.eventlist

// 행사 목록 화면의 전체 레이아웃과 상태 처리를 담당하는 Compose UI.

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jun.todayseoul.R
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    uiState: EventListUiState,
    onSelectDate: (LocalDate) -> Unit,
    onSearchQueryChange: (String?) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneId = ZoneId.of("Asia/Seoul")
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var searchFieldVisible by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable(uiState.filter.searchQuery) {
        mutableStateOf(uiState.filter.searchQuery.orEmpty())
    }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val dateDisplay = remember(uiState.filter.date) {
        displayDateFormatter.format(uiState.filter.date)
    }

    if (showDatePicker) {
        val initialMillis = uiState.filter.date.toEpochMillis(zoneId)
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        val selectionColor = Color(0xFF63A1F2)
        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Surface(
                    shape = RectangleShape,
                    tonalElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.padding(0.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = DatePickerDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.background,
                                selectedDayContainerColor = selectionColor,
                                selectedDayContentColor = Color.White,
                                dayInSelectionRangeContainerColor = selectionColor.copy(alpha = 0.2f),
                                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface,
                                todayDateBorderColor = selectionColor
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            TextButton(
                                shape = RectangleShape,
                                onClick = { showDatePicker = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    containerColor = Color.DarkGray
                                )
                            ) {
                                Text(text = "취소")
                            }
                            TextButton(
                                onClick = {
                                    val selectedDate = datePickerState.selectedDateMillis?.toLocalDate(zoneId)
                                    if (selectedDate != null) {
                                        onSelectDate(selectedDate)
                                        showDatePicker = false
                                    }
                                },
                                shape = RectangleShape,
                                enabled = datePickerState.selectedDateMillis != null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    containerColor = selectionColor
                                )
                            ) {
                                Text(text = "선택")
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(searchFieldVisible) {
        if (searchFieldVisible) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (searchFieldVisible) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { value ->
                                searchQuery = value
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = { Text(text = "제목 또는 위치 검색") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(onSearch = {
                                onSearchQueryChange(searchQuery.ifBlank { null })
                                focusManager.clearFocus()
                            }),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        onSearchQueryChange(null)
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "검색어 지우기"
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        Text(text = "오늘은뭐하지", fontWeight = Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    if (searchFieldVisible) {
                        IconButton(onClick = {
                            onSearchQueryChange(searchQuery.ifBlank { null })
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "검색 실행"
                            )
                        }
                        IconButton(onClick = {
                            searchFieldVisible = false
                            searchQuery = uiState.filter.searchQuery.orEmpty()
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "검색 닫기"
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            searchFieldVisible = true
                            searchQuery = uiState.filter.searchQuery.orEmpty()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "검색",
                                tint = if (searchQuery.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    LocalContentColor.current
                                }
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            EventListBannerAd(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
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
                onDateClick = { showDatePicker = true }
            )

            EventListContent(
                screenState = uiState.screenState,
                onRetry = onRetry,
                onLoadMore = onLoadMore,
                onOpenLink = onOpenLink
            )
        }
    }
}

@Composable
private fun EventListBannerAd(
    modifier: Modifier = Modifier
) {
    val adUnitId = stringResource(id = R.string.admob_banner_ad_unit_id)
    val adRequest = remember { AdRequest.Builder().build() }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(adRequest)
            }
        }
    )
}

private fun LocalDate.toEpochMillis(zoneId: ZoneId): Long =
    this.atStartOfDay(zoneId).toInstant().toEpochMilli()

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

private val displayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREA)

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventListScreenPreview() {
    TodaySeoulTheme {
        EventListScreen(
            uiState = EventListUiState(
                filter = EventFilter(
                    date = LocalDate.of(2024, 10, 13),
                    searchQuery = "서울숲"
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
                            place = "서울숲 야외무대",
                            imageUrl = "https://picsum.photos/seed/screen-preview/800/1200"
                        )
                    ),
                    isLoadingMore = false,
                    hasMoreData = true
                )
            ),
            onSelectDate = {},
            onSearchQueryChange = {},
            onRetry = {},
            onLoadMore = {},
            onOpenLink = {}
        )
    }
}
