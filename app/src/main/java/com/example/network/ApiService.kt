package com.example.network

import com.example.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getProfile(): Response<UserResponse>

    // Devices
    @GET("devices")
    suspend fun getDevices(): Response<List<DeviceDto>>

    @GET("devices/{id}")
    suspend fun getDeviceById(@Path("id") id: String): Response<DeviceDto>

    @POST("devices/{id}/quarantine")
    suspend fun quarantineDevice(@Path("id") id: String): Response<GenericResponse>

    @POST("devices/sync")
    suspend fun syncDevices(@Body request: SyncDevicesRequest): Response<GenericResponse>

    // Alerts & Traffic
    @GET("alerts")
    suspend fun getAlerts(): Response<List<AlertDto>>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummary>

    @POST("traffic/report")
    suspend fun reportTraffic(@Body request: TrafficRequest): Response<TrafficEvent>
}
