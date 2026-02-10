package com.examprep.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestSeries(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("tests")
    val tests: List<Test> = emptyList() // Will be populated separately
)
