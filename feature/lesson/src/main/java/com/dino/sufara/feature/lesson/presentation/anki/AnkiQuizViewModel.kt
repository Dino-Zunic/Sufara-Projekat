package com.dino.sufara.feature.lesson.presentation.anki

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.sufara.feature.lesson.domain.model.LessonStep
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnkiQuizViewModel(private val repository: LessonRepository) : ViewModel() {

    // Lista svih dospjelih pitanja (lessonId, Quiz)
    private val _dueQuizzes = MutableStateFlow<List<Pair<String, LessonStep.Quiz>>>(emptyList())
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _currentQuestion = MutableStateFlow<Pair<String, LessonStep.Quiz>?>(null)
    val currentQuestion = _currentQuestion.asStateFlow()

    // Koliko nam je ostalo do kraja sesije
    private val _remainingCount = MutableStateFlow(0)
    val remainingCount = _remainingCount.asStateFlow()

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
        
        viewModelScope.launch {
            // 1. Uvijek upisujemo u bazu da bi algoritam znao da je bila greška (ili pogodak)
            repository.submitQuizAnswer(current.second.id, current.first, isCorrect)
            
            val currentList = _dueQuizzes.value.toMutableList()
            
            if (isCorrect) {
                // Ako je tačno, samo ga izbacimo iz liste
                if (currentList.isNotEmpty()) {
                    currentList.removeAt(0)
                }
            } else {
                // AKO JE GREŠKA: 
                // Pomeramo ga sa prvog mesta na kraj liste da bi se ponovo pojavilo
                if (currentList.isNotEmpty()) {
                    val failedQuestion = currentList.removeAt(0)
                    currentList.add(failedQuestion) // Ide na kraj "queue"-a
                }
            }
            
            // 2. Ažuriramo stanje
            _dueQuizzes.value = currentList
            _remainingCount.value = currentList.size
            _currentQuestion.value = currentList.firstOrNull()
            
            com.dino.sufara.feature.lesson.domain.util.SufaraLogger.log(
                "ANKI QUEUE: Preostalo pitanja: ${currentList.size}. Sljedeće: ${_currentQuestion.value?.second?.id ?: "Nema"}"
            )
        }
    }
}