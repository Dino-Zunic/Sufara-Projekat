package com.dino.sufara.feature.lesson.presentation.writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WritingLessonViewModel(
    private val repository: LessonRepository,
    private val lessonId: String
) : ViewModel() {
    private val _lesson = MutableStateFlow<Lesson?>(null)
    val lesson = _lesson.asStateFlow()

    init {
        viewModelScope.launch {
            _lesson.value = repository.getWritingLessons().find { it.id == lessonId }
        }
    }

    fun complete(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.completeWritingLessonAndUnlockNext(lessonId)
            onComplete()
        }
    }
}
