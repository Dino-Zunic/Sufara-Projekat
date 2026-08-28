package com.dino.sufara.feature.lesson.presentation.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonViewerViewModel(
    private val repository: LessonRepository,
    private val lessonId: String
) : ViewModel() {

    private val _lesson = MutableStateFlow<Lesson?>(null)
    val lesson = _lesson.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _completedSteps = MutableStateFlow<Set<Int>>(emptySet())
    val completedSteps = _completedSteps.asStateFlow()

    init {
        viewModelScope.launch {
            _lesson.value = repository.getLessonById(lessonId)
        }
    }

    fun markStepAsCompleted(index: Int) {
        _completedSteps.value = _completedSteps.value + index
    }

    fun nextStep() {
        val currentLesson = _lesson.value ?: return
        if (_currentIndex.value < currentLesson.steps.size - 1) {
            _currentIndex.value++
        }
    }

    fun previousStep() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
        }
    }

    fun advanceFrom(expectedIndex: Int, onComplete: () -> Unit) {
        val currentLesson = _lesson.value ?: return
        if (_currentIndex.value != expectedIndex) return

        if (expectedIndex == currentLesson.steps.lastIndex) {
            finishLesson(onComplete)
        } else {
            _currentIndex.value++
        }
    }
    
    fun finishLesson(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.completeLessonAndUnlockNext(lessonId)
            onComplete()
        }
    }
}
