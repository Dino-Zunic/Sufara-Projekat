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

    init {
        viewModelScope.launch {
            // Убацујемо пун назив фолдера за претрагу
            val folders = repository.getAllLessons()
            val fullLesson = folders.find { it.id == lessonId }
            _lesson.value = repository.getLessonById(fullLesson?.id + " " + fullLesson?.title)
        }
    }

    fun nextStep() {
        val currentLesson = _lesson.value ?: return
        if (_currentIndex.value < currentLesson.steps.size - 1) {
            _currentIndex.value++
        } else {
            // Овде ћемо касније додати логику за завршетак лекције
        }
    }

    fun previousStep() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
        }
    }
}