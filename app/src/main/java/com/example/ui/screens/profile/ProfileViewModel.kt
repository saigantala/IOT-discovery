package com.example.ui.screens.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.WifiConnectionInfo
import com.example.network.RetrofitClient
import com.example.network.TokenManager
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

    private val _userEmail = MutableStateFlow("admin@enterprise.io")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow("Admin User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        observeData()
    }

    fun loadProfile(context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.getApiService(context).getProfile()
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    if (user != null) {
                        _userName.value = user.name
                        _userEmail.value = user.email
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Failed to fetch profile: ${e.message}")
            }
        }
    }

    fun logout(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            val tokenManager = TokenManager(context)
            val refreshToken = tokenManager.getRefreshToken() ?: ""
            try {
                Log.d("ProfileVM", "🔒 Requesting auth logout on backend...")
                RetrofitClient.getApiService(context).logout(mapOf("refreshToken" to refreshToken))
            } catch (e: Exception) {
                Log.e("ProfileVM", "Logout API exception: ${e.message}")
            } finally {
                tokenManager.clear()
                onComplete()
            }
        }
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
