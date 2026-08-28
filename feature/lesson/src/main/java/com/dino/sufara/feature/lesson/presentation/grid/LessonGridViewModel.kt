package com.dino.sufara.feature.lesson.presentation.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.sufara.feature.lesson.domain.model.Lesson
import com.dino.sufara.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonGridViewModel(
    private val repository: LessonRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons = _lessons.asStateFlow()
    private val _writingUnlocked = MutableStateFlow(false)
    val writingUnlocked = _writingUnlocked.asStateFlow()
    private val _writingLessons = MutableStateFlow<List<Lesson>>(emptyList())
    val writingLessons = _writingLessons.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _lessons.value = repository.getAllLessons()
            _writingUnlocked.value = repository.isWritingUnlocked()
            _writingLessons.value = repository.getWritingLessons()
        }
    }
}
