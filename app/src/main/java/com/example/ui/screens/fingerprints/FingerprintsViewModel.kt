package com.example.ui.screens.fingerprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.FingerprintData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FingerprintsViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _fingerprints = MutableStateFlow<List<FingerprintData>>(emptyList())
    val fingerprints: StateFlow<List<FingerprintData>> = _fingerprints.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFingerprintsStream().collect {
                _fingerprints.value = it
            }
        }
    }
}
