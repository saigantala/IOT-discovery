package com.example.di

import android.content.Context
import com.example.ai.GeminiService
import com.example.data.AppDatabase
import com.example.data.DataRepository
import com.example.discovery.NetworkDiscoveryManager
import com.example.utils.CredentialsManager
import com.example.mock.MockData
import com.example.model.Device
import com.example.network.RetrofitClient

/**
 * Provides repositories and connection managers for IoT Device Discovery.
 */
object AppModule {
    private var dataRepository: DataRepository? = null
    private var credentialsManager: CredentialsManager? = null

    fun getRepository(context: Context): DataRepository {
        if (dataRepository == null) {
            dataRepository = DataRepository(context.applicationContext)
        }
        return dataRepository!!
    }

    // Helper for ViewModels that don't want to pass context every time if already initialized
    fun getRepository(): DataRepository {
        return dataRepository ?: throw IllegalStateException("Repository not initialized. Call getRepository(context) first.")
    }

    fun getCredentialsManager(context: Context): CredentialsManager {
        if (credentialsManager == null) {
            credentialsManager = CredentialsManager(context)
        }
        return credentialsManager!!
    }

    fun getDevices(): List<Device> = MockData.devices
    fun getAlerts() = MockData.alerts
    fun getTimelineEvents() = MockData.timelineEvents
    fun getDiscoveryLogs() = MockData.discoveryLogs
    fun getFingerprints() = MockData.fingerprints
    fun getComplianceReport() = MockData.complianceReport
    fun getNetworkNodes() = MockData.networkNodes

    // Real connections
    fun getApiService(context: Context) = RetrofitClient.getApiService(context)
    fun getDatabase(context: Context) = AppDatabase.getDatabase(context)
    fun getGeminiService() = GeminiService
    fun getDiscoveryManager(context: Context) = NetworkDiscoveryManager(context)
}
