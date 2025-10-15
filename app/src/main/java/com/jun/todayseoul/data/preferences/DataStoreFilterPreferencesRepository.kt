package com.jun.todayseoul.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.jun.todayseoul.domain.model.EventFilter
import com.jun.todayseoul.domain.repository.FilterPreferencesRepository
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

class DataStoreFilterPreferencesRepository(
    context: Context,
    private val clock: Clock = Clock.system(DEFAULT_ZONE_ID)
) : FilterPreferencesRepository {

    private val dataStore = PreferenceDataStoreFactory.createWithPath {
        context.preferencesDataStoreFile(DATA_STORE_NAME).absolutePath.toPath()
    }

    override val filterFlow: Flow<EventFilter> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            val dateString = preferences[DATE_KEY]
            val searchQuery = preferences[SEARCH_KEY] ?: preferences[LEGACY_LOCATION_KEY]
            EventFilter(
                date = dateString?.toLocalDateOrNull() ?: LocalDate.now(clock),
                searchQuery = searchQuery
            )
        }
        .distinctUntilChanged()

    override suspend fun updateDate(date: LocalDate) {
        dataStore.edit { preferences ->
            preferences[DATE_KEY] = date.format(DateTimeFormatter.ISO_DATE)
        }
    }

    override suspend fun updateSearchQuery(query: String?) {
        val normalized = query?.trim().takeUnless { it.isNullOrEmpty() }
        dataStore.edit { preferences ->
            if (normalized == null) {
                preferences.remove(SEARCH_KEY)
                preferences.remove(LEGACY_LOCATION_KEY)
            } else {
                preferences[SEARCH_KEY] = normalized
                preferences.remove(LEGACY_LOCATION_KEY)
            }
        }
    }

    private fun String.toLocalDateOrNull(): LocalDate? = runCatching {
        LocalDate.parse(this, DateTimeFormatter.ISO_DATE)
    }.getOrNull()

    private companion object {
        private const val DATA_STORE_NAME = "event_filter_preferences"
        private val DATE_KEY = stringPreferencesKey("selected_date")
        private val SEARCH_KEY = stringPreferencesKey("search_query")
        private val LEGACY_LOCATION_KEY = stringPreferencesKey("location_query")
        private val DEFAULT_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
