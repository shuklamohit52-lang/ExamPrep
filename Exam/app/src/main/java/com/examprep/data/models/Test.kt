package com.examprep.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Test(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("test_date")
    val testDate: String,
    @SerialName("shift")
    val shift: Int,
    @SerialName("duration_in_minutes")
    val durationInMinutes: Int? = null,
    @SerialName("test_series_id")
    val testSeriesId: String,
    @SerialName("questions")
    val questions: List<Question> = emptyList() // Will be populated separately
)
