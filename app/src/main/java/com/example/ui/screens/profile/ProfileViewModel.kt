package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.WifiConnectionInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _deviceCount = MutableStateFlow(0)
    val deviceCount: StateFlow<Int> = _deviceCount.asStateFlow()

    private val _wifiInfo = MutableStateFlow<WifiConnectionInfo?>(null)
    val wifiInfo: StateFlow<WifiConnectionInfo?> = _wifiInfo.asStateFlow()

    private val _systemStatus = MutableStateFlow("Initializing...")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getDevicesStream().collect {
                _deviceCount.value = it.size
            }
        }
        viewModelScope.launch {
            repository.getRealTimeStatus().collect {
                _systemStatus.value = it
            }
        }
        _wifiInfo.value = repository.getCurrentWifiInfo()
    }
}
