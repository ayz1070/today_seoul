package com.jun.todayseoul.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.jun.todayseoul.BuildConfig
import com.jun.todayseoul.data.remote.api.CulturalEventService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object SeoulOpenApiClient {
    private const val BASE_URL = "http://openapi.seoul.go.kr:8088/"
    private const val TIMEOUT_SECONDS = 10L

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor())
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    fun retrofit(client: OkHttpClient = okHttpClient()): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun culturalEventService(retrofit: Retrofit = retrofit()): CulturalEventService =
        retrofit.create(CulturalEventService::class.java)
}
