package com.example.network

import com.example.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Health Check (Requirement 4 & 11)
    @GET("/health")
    suspend fun checkHealth(): Response<HealthStatus>

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponseData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponseData>>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: Map<String, String>): Response<ApiResponse<AuthResponseData>>

    @POST("auth/refresh")
    fun refreshSync(@Body body: Map<String, String>): Call<ApiResponse<AuthResponseData>>

    @POST("auth/logout")
    suspend fun logout(@Body body: Map<String, String>): Response<ApiResponse<GenericMessage>>

    @GET("auth/me")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    // Devices
    @GET("devices")
    suspend fun getDevices(): Response<ApiResponse<List<DeviceDto>>>

    @GET("devices/{id}")
    suspend fun getDeviceById(@Path("id") id: String): Response<ApiResponse<DeviceDto>>

    @POST("devices/{id}/quarantine")
    suspend fun quarantineDevice(@Path("id") id: String): Response<ApiResponse<GenericMessage>>

    @POST("devices/{id}/unquarantine")
    suspend fun unquarantineDevice(@Path("id") id: String): Response<ApiResponse<GenericMessage>>

    @POST("devices/sync")
    suspend fun syncDevices(@Body request: SyncDevicesRequest): Response<ApiResponse<GenericMessage>>

    // Dashboard & Alerts
    @GET("alerts")
    suspend fun getAlerts(): Response<ApiResponse<List<AlertDto>>>

    @PATCH("alerts/{id}")
    suspend fun updateAlertStatus(@Path("id") id: String, @Body body: Map<String, String>): Response<ApiResponse<AlertDto>>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<ApiResponse<DashboardSummary>>

    // Traffic & Sessions
    @POST("traffic/report")
    suspend fun reportTraffic(@Body request: TrafficRequest): Response<ApiResponse<TrafficEvent>>

    @POST("sessions")
    suspend fun logSession(@Body sessionData: Map<String, Any>): Response<ApiResponse<GenericMessage>>
}
