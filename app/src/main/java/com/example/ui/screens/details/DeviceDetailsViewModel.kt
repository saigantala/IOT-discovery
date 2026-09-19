package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.Device
import com.example.model.TimelineEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeviceDetailsViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _device = MutableStateFlow<Device?>(null)
    val device: StateFlow<Device?> = _device.asStateFlow()

    private val _events = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val events: StateFlow<List<TimelineEvent>> = _events.asStateFlow()

    fun loadDevice(deviceId: String) {
        viewModelScope.launch {
            repository.getDevicesStream().collect { devices ->
                val found = devices.find { it.id == deviceId }
                _device.value = found
            }
        }
        
        // In a real app, this would also be a stream from the repository
        _events.value = AppModule.getTimelineEvents().filter { it.deviceId == deviceId }
    }

    fun scanAgain() {
        viewModelScope.launch {
            repository.runNetworkDiscovery()
        }
    }
}
