package com.examprep.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    val id: String,
    val title: String,
    val questions: List<Question>
)
