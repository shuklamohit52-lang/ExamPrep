package com.examprep.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    @SerialName("id")
    val id: String,
    @SerialName("question_text")
    val questionText: String,
    @SerialName("options")
    val options: List<String> = emptyList(),
    @SerialName("correct_answer")
    val correctAnswer: String,
    @SerialName("explanation")
    val explanation: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("test_id")
    val testId: String
)
