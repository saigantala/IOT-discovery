package com.example.ui.screens.scanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import kotlinx.coroutines.launch

class ScanningViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    val scannedDevices = repository.currentScanDevices
    val scannedIpsCount = repository.scannedIpsCount
    val scanProgress = repository.scanProgress
    val isScanning = repository.isScanning

    fun startDeepScan() {
        viewModelScope.launch {
            repository.runNetworkDiscovery()
        }
    }

    fun getSubnet(): String {
        val info = repository.getCurrentWifiInfo()
        return if (info != null && info.ipAddress != "0.0.0.0") {
            "${info.ipAddress.substringBeforeLast(".")}.0/24"
        } else {
            "local subnet"
        }
    }
}
