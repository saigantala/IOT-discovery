package com.example.ui.screens.rogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RogueDevicesViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _rogueDevices = MutableStateFlow<List<Device>>(emptyList())
    val rogueDevices: StateFlow<List<Device>> = _rogueDevices.asStateFlow()

    init {
        observeRogueDevices()
    }

    private fun observeRogueDevices() {
        viewModelScope.launch {
            repository.getRogueDevicesStream().collect {
                _rogueDevices.value = it
            }
        }
    }
}
