package com.example.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.DiscoveryLog
import com.example.model.LoginHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _discoveryLogs = MutableStateFlow<List<DiscoveryLog>>(emptyList())
    val discoveryLogs: StateFlow<List<DiscoveryLog>> = _discoveryLogs.asStateFlow()

    private val _loginHistory = MutableStateFlow<List<LoginHistory>>(repository.getLoginHistory())
    val loginHistory: StateFlow<List<LoginHistory>> = _loginHistory.asStateFlow()

    init {
        observeLogs()
    }

    private fun observeLogs() {
        viewModelScope.launch {
            repository.getDiscoveryLogsStream().collect {
                _discoveryLogs.value = it
            }
        }
    }
}
