package com.jun.todayseoul.presentation.eventlist.component

// 로딩/성공/빈 상태에 따라 다른 리스트 UI를 보여주는 컴포넌트.

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jun.todayseoul.core.theme.TodaySeoulTheme
import com.jun.todayseoul.domain.model.Event
import com.jun.todayseoul.presentation.eventlist.EventListUiState
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventListContent(
    screenState: EventListUiState.ScreenState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (screenState) {
        EventListUiState.ScreenState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EventListUiState.ScreenState.Success -> {
            val successState = screenState
            val listState = rememberLazyGridState()
            val currentSuccessState by rememberUpdatedState(successState)

            LaunchedEffect(listState) {
                snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                    .distinctUntilChanged()
                    .collect { lastVisibleIndex ->
                        val state = currentSuccessState
                        if (
                            lastVisibleIndex != null &&
                            state.hasMoreData &&
                            !state.isLoadingMore
                        ) {
                            val triggerIndex = (state.events.size - LOAD_MORE_THRESHOLD).coerceAtLeast(0)
                            if (lastVisibleIndex >= triggerIndex) {
                                onLoadMore()
                            }
                        }
                    }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = successState.events,
                    key = { event ->
                        buildString {
                            append(event.title.orEmpty())
                            append("|")
                            append(event.orgLink.orEmpty())
                            append("|")
                            append(event.date.orEmpty())
                        }
                    }
                ) { event ->
                    EventListItem(
                        event = event,
                        onOpenLink = onOpenLink,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (successState.isLoadingMore) {
                    item(
                        key = "loading_more_indicator",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        EventListUiState.ScreenState.Empty -> {
            EmptyState(
                message = "오늘 예정된 행사가 없어요.",
                actionLabel = "새로고침",
                onActionClick = onRetry,
                modifier = modifier.fillMaxSize()
            )
        }

        is EventListUiState.ScreenState.Error -> {
            EmptyState(
                message = screenState.message ?: "행사 정보를 불러오는 중 문제가 발생했어요.",
                actionLabel = "다시 시도",
                onActionClick = onRetry,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clearAndSetSemantics { }
            )
            Button(onClick = onActionClick) {
                Text(text = actionLabel)
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 5

@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventListContentLoadingPreview() {
    TodaySeoulTheme {
        EventListContent(
            screenState = EventListUiState.ScreenState.Loading,
            onRetry = {},
            onLoadMore = {},
            onOpenLink = {}
        )
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventListContentSuccessPreview() {
    TodaySeoulTheme {
        EventListContent(
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
                    )
                ),
                isLoadingMore = true,
                hasMoreData = true
            ),
            onRetry = {},
            onLoadMore = {},
            onOpenLink = {}
        )
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun EventListContentErrorPreview() {
    TodaySeoulTheme {
        EventListContent(
            screenState = EventListUiState.ScreenState.Error(message = "네트워크 오류가 발생했어요."),
            onRetry = {},
            onLoadMore = {},
            onOpenLink = {}
        )
    }
}
