package com.example.ui.screens.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.ComplianceReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ComplianceViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _report = MutableStateFlow<ComplianceReport?>(null)
    val report: StateFlow<ComplianceReport?> = _report.asStateFlow()

    init {
        observeCompliance()
    }

    private fun observeCompliance() {
        viewModelScope.launch {
            repository.getComplianceStream().collect {
                _report.value = it
            }
        }
    }
}
