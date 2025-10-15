package com.jun.todayseoul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.jun.todayseoul.core.network.SeoulOpenApiClient
import com.jun.todayseoul.data.preferences.DataStoreFilterPreferencesRepository
import com.jun.todayseoul.data.remote.SeoulEventRemoteDataSource
import com.jun.todayseoul.data.repository.DefaultEventRepository
import com.jun.todayseoul.presentation.eventlist.EventListRoute
import com.jun.todayseoul.presentation.eventlist.EventListViewModel
import com.jun.todayseoul.presentation.eventlist.EventListViewModelFactory
import com.jun.todayseoul.core.theme.TodaySeoulTheme

class MainActivity : ComponentActivity() {

    private val eventListViewModel: EventListViewModel by viewModels {
        EventListViewModelFactory(
            eventRepository = DefaultEventRepository(
                remoteDataSource = SeoulEventRemoteDataSource(
                    service = SeoulOpenApiClient.culturalEventService()
                )
            ),
            filterPreferencesRepository = DataStoreFilterPreferencesRepository(
                applicationContext
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodaySeoulTheme {
                EventListRoute(viewModel = eventListViewModel)
            }
        }
    }
}
