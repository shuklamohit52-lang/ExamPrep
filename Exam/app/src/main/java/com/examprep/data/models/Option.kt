package com.examprep.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Option(
    @SerialName("id")
    val id: String,
    @SerialName("text")
    val text: String
)
