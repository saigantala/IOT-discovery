package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.AlertItem
import com.example.model.Device
import com.example.model.TimelineEvent
import com.example.model.WifiConnectionInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    private val _systemStatus = MutableStateFlow("Initializing...")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    private val _currentWifi = MutableStateFlow<WifiConnectionInfo?>(null)
    val currentWifi: StateFlow<WifiConnectionInfo?> = _currentWifi.asStateFlow()

    private val _timeline = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val timeline: StateFlow<List<TimelineEvent>> = _timeline.asStateFlow()

    val isScanning = repository.isScanning
    val scanProgress = repository.scanProgress

    init {
        observeData()
        refreshLiveBackendData()
    }

    fun startScan() {
        viewModelScope.launch {
            repository.runNetworkDiscovery()
        }
    }

    fun refreshLiveBackendData() {
        viewModelScope.launch {
            repository.refreshDevicesFromApi()
            repository.fetchAlertsFromApi()
            repository.fetchDashboardSummary()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getDevicesStream().collect {
                _devices.value = it
            }
        }

        viewModelScope.launch {
            repository.getAlertsStream().collect {
                _alerts.value = it
            }
        }

        viewModelScope.launch {
            repository.getRealTimeStatus().collect {
                _systemStatus.value = it
                _currentWifi.value = repository.getCurrentWifiInfo()
            }
        }

        viewModelScope.launch {
            repository.getTimelineStream().collect {
                _timeline.value = it
            }
        }
    }
}
