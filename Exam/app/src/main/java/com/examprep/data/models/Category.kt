package com.examprep.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("test_series")
    val testSeries: List<TestSeries> = emptyList() // Will be populated separately
)
