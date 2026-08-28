package com.dino.sufara.feature.lesson.presentation.anki

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnkiQuizViewModel(private val repository: LessonRepository) : ViewModel() {
    private val _dueQuizzes = MutableStateFlow<List<Pair<String, LessonStep.Quiz>>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _currentQuestion = MutableStateFlow<Pair<String, LessonStep.Quiz>?>(null)
    val currentQuestion = _currentQuestion.asStateFlow()

    private val _remainingCount = MutableStateFlow(0)
    val remainingCount = _remainingCount.asStateFlow()

    private val _questionRevision = MutableStateFlow(0)
    val questionRevision = _questionRevision.asStateFlow()

    private val _feedbackCorrect = MutableStateFlow<Boolean?>(null)
    val feedbackCorrect = _feedbackCorrect.asStateFlow()

    init {
        loadDueQuizzes()
    }

    private fun loadDueQuizzes() {
        viewModelScope.launch {
            _isLoading.value = true
            val quizzes = repository.getDueQuizzes()
            _dueQuizzes.value = quizzes
            _remainingCount.value = quizzes.size
            _currentQuestion.value = quizzes.firstOrNull()
            _isLoading.value = false
        }
    }

    fun submitAnswer(isCorrect: Boolean) {
        val current = _currentQuestion.value ?: return
        if (_feedbackCorrect.value != null) return
        viewModelScope.launch {
            // Одговор се чува одмах, а повратна информација остаје док корисник не настави.
            repository.submitQuizAnswer(current.second.id, current.first, isCorrect)
            _feedbackCorrect.value = isCorrect
        }
    }

    fun advanceAfterFeedback() {
        val isCorrect = _feedbackCorrect.value ?: return
        val currentList = ReviewSessionQueue.afterAnswer(_dueQuizzes.value, isCorrect)
        _dueQuizzes.value = currentList
        _remainingCount.value = currentList.size
        _currentQuestion.value = currentList.firstOrNull()
        _feedbackCorrect.value = null
        _questionRevision.value++
    }
}
