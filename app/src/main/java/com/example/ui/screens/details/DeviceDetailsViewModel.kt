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
        
        viewModelScope.launch {
            repository.getTimelineStream().collect { events ->
                _events.value = events.filter { it.deviceId == deviceId }
            }
        }
    }

    fun quarantineDevice(deviceId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.quarantineDevice(deviceId)
            onResult(result.isSuccess)
        }
    }

    fun scanAgain() {
        viewModelScope.launch {
            repository.runNetworkDiscovery()
        }
    }
}
