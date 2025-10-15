package com.jun.todayseoul.presentation.eventlist

// ViewModel과 Compose UI를 연결하는 화면 진입 지점.

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun EventListRoute(
    viewModel: EventListViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    EventListScreen(
        uiState = uiState,
        onSelectDate = viewModel::updateDate,
        onLocationChange = viewModel::updateLocation,
        onRetry = viewModel::refreshEvents,
        onOpenLink = { link ->
            runCatching { uriHandler.openUri(link) }
                .onFailure {
                    Toast.makeText(context, "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
        },
        modifier = modifier
    )
}
