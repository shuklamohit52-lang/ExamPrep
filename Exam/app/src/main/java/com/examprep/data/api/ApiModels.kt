package com.examprep.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiTestSeries(
    val id: String,
    val title: String,
    @SerialName("total_tests") val totalTests: Int,
    @SerialName("free_tests") val freeTests: Int,
    val languages: List<String>
)

@Serializable
data class ResourceFile(
    val id: String,
    val title: String,
    val category: String,
    @SerialName("download_url") val downloadUrl: String,
    @SerialName("is_free") val isFree: Boolean
)

@Serializable
 data class CurrentAffairItem(
    val id: String,
    val headline: String,
    val date: String,
    @SerialName("pdf_url") val pdfUrl: String?
)
