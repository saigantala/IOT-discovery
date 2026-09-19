package com.example.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.ComplianceReport
import com.example.model.Device
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _deviceCount = MutableStateFlow(0)
    val deviceCount: StateFlow<Int> = _deviceCount.asStateFlow()

    private val _compliance = MutableStateFlow<ComplianceReport?>(null)
    val compliance: StateFlow<ComplianceReport?> = _compliance.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getDevicesStream().collect { _deviceCount.value = it.size }
        }
        viewModelScope.launch {
            repository.getComplianceStream().collect { _compliance.value = it }
        }
    }
}
