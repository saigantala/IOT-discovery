package com.example.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.TimelineEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimelineViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _events = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val events: StateFlow<List<TimelineEvent>> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTimelineStream().collect {
                _events.value = it
            }
        }
    }
}
