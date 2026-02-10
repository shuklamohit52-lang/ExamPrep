package com.examprep.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.examprep.data.QuestionStatus
import com.examprep.data.TestResult
import com.examprep.data.UserAnswer
import com.examprep.data.models.Question
import com.examprep.data.models.Test
import com.examprep.data.supabase.SupabaseService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val test: Test? = null, // Test metadata only (name, duration)
    val questionIds: List<String> = emptyList(), // All question IDs for the test
    val currentQuestion: Question? = null, // The actual question object for the current index
    val userAnswers: List<UserAnswer> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val timeLeft: Long = 1800000, // Default to 30 minutes
    val showPalette: Boolean = false,
    val isTimeUp: Boolean = false
)

class QuizViewModel(private val supabaseService: SupabaseService) : ViewModel() {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _testResult = MutableStateFlow<TestResult?>(null)
    val testResult: StateFlow<TestResult?> = _testResult.asStateFlow()

    private val _resultQuestions = MutableStateFlow<List<Question>>(emptyList())
    val resultQuestions: StateFlow<List<Question>> = _resultQuestions.asStateFlow()

    private var timerJob: Job? = null

    fun loadTest(testId: String) {
        viewModelScope.launch {
            // Step 1: Fetch test metadata
            val test = supabaseService.getTestById(testId)
            // Step 2: Fetch all question IDs for the test
            val qIds = supabaseService.getQuestionIdsForTest(testId)

            if (test != null && qIds.isNotEmpty()) {
                val initialUserAnswers = qIds.map { id -> UserAnswer(id, null, QuestionStatus.NOT_VISITED) }.toMutableList()
                
                // Mark the first question as UNANSWERED if userAnswers is not empty
                if (initialUserAnswers.isNotEmpty()) {
                    initialUserAnswers[0] = initialUserAnswers[0].copy(status = QuestionStatus.UNANSWERED)
                }

                _testResult.value = null
                _resultQuestions.value = emptyList()
                _uiState.update {
                    it.copy(
                        test = test, // Store only metadata
                        questionIds = qIds, // Store all IDs
                        userAnswers = initialUserAnswers,
                        currentQuestionIndex = 0,
                        timeLeft = (test.durationInMinutes?.times(60000) ?: 1800000).toLong(),
                        isTimeUp = false
                    )
                }
                // Step 3: Load the first question's content
                loadQuestion(0)
                startTimer()
            } else {
                // Handle error: test or questions not found
                _uiState.update { it.copy(test = null, questionIds = emptyList(), currentQuestion = null) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1000) }
            }
            if (_uiState.value.timeLeft <= 0 && !_uiState.value.isTimeUp) {
                _uiState.update { it.copy(isTimeUp = true) }
                finishTest() // Automatically finish the test when time is up
            }
        }
    }

    private suspend fun loadQuestion(index: Int) {
        val questionId = _uiState.value.questionIds.getOrNull(index)
        if (questionId != null) {
            val question = supabaseService.getQuestionById(questionId)
            _uiState.update { it.copy(currentQuestion = question) }
        } else {
            _uiState.update { it.copy(currentQuestion = null) }
        }
    }

    fun finishTest() {
        timerJob?.cancel()
        val snapshot = _uiState.value
        val allQuestionIds = snapshot.questionIds

        viewModelScope.launch {
            val questions = allQuestionIds.mapNotNull { questionId ->
                supabaseService.getQuestionById(questionId)
            }

            _resultQuestions.value = questions

            var correct = 0
            var incorrect = 0

            questions.forEach { question ->
                val userAnswer = snapshot.userAnswers.find { it.questionId == question.id }?.selectedOption
                if (userAnswer != null) {
                    if (userAnswer == question.correctAnswer) {
                        correct++
                    } else {
                        incorrect++
                    }
                }
            }

            val totalQuestions = allQuestionIds.size
            val attemptedCount = snapshot.userAnswers.count { it.selectedOption != null }
            val unattempted = totalQuestions - attemptedCount
            val score = correct * 2 - incorrect
            val accuracy = if (attemptedCount > 0) (correct.toFloat() / attemptedCount.toFloat()) * 100 else 0f

            _testResult.value = TestResult(
                rank = "N/A",
                score = "$score/${totalQuestions * 2}",
                percentile = "N/A",
                accuracy = "${String.format("%.2f", accuracy)}%",
                attempted = "$attemptedCount/$totalQuestions",
                correct = correct,
                incorrect = incorrect,
                unattempted = unattempted,
                sectionalSummary = emptyList(),
                averageScore = 0,
                bestScore = 0
            )
        }
    }

    fun clearTestResult() {
        _testResult.value = null
        _resultQuestions.value = emptyList()
    }

    fun selectAnswer(option: String) {
        val answers = _uiState.value.userAnswers.toMutableList()
        val currentQuestionId = _uiState.value.currentQuestion?.id ?: return
        val currentAnswerIndex = answers.indexOfFirst { it.questionId == currentQuestionId }
        if (currentAnswerIndex == -1) return
        val currentAnswer = answers[currentAnswerIndex]

        val newStatus = if (currentAnswer.status == QuestionStatus.MARKED_FOR_REVIEW || currentAnswer.status == QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW) {
            QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW
        } else {
            QuestionStatus.ANSWERED
        }
        answers[currentAnswerIndex] = currentAnswer.copy(selectedOption = option, status = newStatus)
        _uiState.update { it.copy(userAnswers = answers) }
    }

    fun markForReview() {
        val answers = _uiState.value.userAnswers.toMutableList()
        val currentQuestionId = _uiState.value.currentQuestion?.id ?: return
        val currentAnswerIndex = answers.indexOfFirst { it.questionId == currentQuestionId }
        if (currentAnswerIndex == -1) return
        val currentAnswer = answers[currentAnswerIndex]

        val newStatus = when (currentAnswer.status) {
            QuestionStatus.ANSWERED -> QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW
            QuestionStatus.UNANSWERED, QuestionStatus.NOT_VISITED -> QuestionStatus.MARKED_FOR_REVIEW
            QuestionStatus.MARKED_FOR_REVIEW -> QuestionStatus.UNANSWERED
            QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW -> QuestionStatus.ANSWERED
            else -> currentAnswer.status // Should not happen for these statuses
        }
        answers[currentAnswerIndex] = currentAnswer.copy(status = newStatus)
        _uiState.update { it.copy(userAnswers = answers) }
    }

    private fun moveToQuestion(index: Int) {
        val questionIds = _uiState.value.questionIds
        if (index < 0 || index >= questionIds.size) return

        val answers = _uiState.value.userAnswers.toMutableList()

        // Update status of the *previous* question if it was NOT_VISITED
        val previousQuestionId = questionIds.getOrNull(_uiState.value.currentQuestionIndex)
        if (previousQuestionId != null) {
            val previousAnswerIndex = answers.indexOfFirst { it.questionId == previousQuestionId }
            if (previousAnswerIndex != -1) {
                val previousAnswer = answers[previousAnswerIndex]
                if (previousAnswer.status == QuestionStatus.NOT_VISITED) {
                    answers[previousAnswerIndex] = previousAnswer.copy(status = QuestionStatus.UNANSWERED)
                }
            }
        }

        // Update status of the *new* question if it's NOT_VISITED
        val newQuestionId = questionIds.getOrNull(index)
        if (newQuestionId != null) {
            val newAnswerIndex = answers.indexOfFirst { it.questionId == newQuestionId }
            if (newAnswerIndex != -1) {
                val newAnswer = answers[newAnswerIndex]
                if (newAnswer.status == QuestionStatus.NOT_VISITED) {
                    answers[newAnswerIndex] = newAnswer.copy(status = QuestionStatus.UNANSWERED)
                }
            }
        }

        _uiState.update { it.copy(currentQuestionIndex = index, userAnswers = answers) }
        viewModelScope.launch { // Fetch the new question's content
            loadQuestion(index)
        }
    }

    fun nextQuestion() {
        moveToQuestion(_uiState.value.currentQuestionIndex + 1)
    }

    fun previousQuestion() {
        moveToQuestion(_uiState.value.currentQuestionIndex - 1)
    }

    fun navigateToQuestion(questionIndex: Int) {
        moveToQuestion(questionIndex)
    }

    fun togglePalette() {
        _uiState.update { it.copy(showPalette = !it.showPalette) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
