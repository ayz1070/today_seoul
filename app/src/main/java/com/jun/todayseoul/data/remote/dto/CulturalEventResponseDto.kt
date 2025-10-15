package com.jun.todayseoul.data.remote.dto

import com.jun.todayseoul.domain.model.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CulturalEventResponseDto(
    @SerialName("culturalEventInfo") val info: CulturalEventInfoDto? = null
)

@Serializable
data class CulturalEventInfoDto(
    @SerialName("list_total_count") val totalCount: Int? = null,
    @SerialName("RESULT") val result: ApiResultDto? = null,
    @SerialName("row") val rows: List<CulturalEventDto>? = null
)

@Serializable
data class ApiResultDto(
    @SerialName("CODE") val code: String? = null,
    @SerialName("MESSAGE") val message: String? = null
)

@Serializable
data class CulturalEventDto(
    @SerialName("TITLE") val title: String? = null,
    @SerialName("CODENAME") val codeName: String? = null,
    @SerialName("DATE") val date: String? = null,
    @SerialName("USE_FEE") val useFee: String? = null,
    @SerialName("ORG_LINK") val orgLink: String? = null,
    @SerialName("GUNAME") val guName: String? = null,
    @SerialName("PLACE") val place: String? = null
)

fun CulturalEventDto.toDomain(): Event = Event(
    title = title,
    codeName = codeName,
    date = date,
    useFee = useFee,
    orgLink = orgLink,
    guName = guName,
    place = place
)

fun List<CulturalEventDto>.toDomainEvents(): List<Event> = map { it.toDomain() }
