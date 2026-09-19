package com.example.ui.screens.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.Device
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DevicesViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    init {
        observeDevices()
    }

    private fun observeDevices() {
        viewModelScope.launch {
            repository.getDevicesStream().collect { allDevices ->
                _devices.value = allDevices
            }
        }
    }

    fun startScan() {
        viewModelScope.launch {
            repository.runNetworkDiscovery()
        }
    }
}
