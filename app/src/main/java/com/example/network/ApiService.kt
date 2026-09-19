package com.example.network

import com.example.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    fun refresh(@Body body: Map<String, String>): retrofit2.Call<AuthResponse>

    @GET("auth/me")
    suspend fun getProfile(): Response<UserResponse>

    // Devices
    @GET("devices")
    suspend fun getDevices(): Response<List<Device>>

    @GET("devices/{id}")
    suspend fun getDeviceById(@Path("id") id: String): Response<Device>

    @POST("devices/{id}/quarantine")
    suspend fun quarantineDevice(@Path("id") id: String): Response<GenericResponse>

    // Alerts & Traffic
    @GET("alerts")
    suspend fun getAlerts(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<AlertListResponse>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummary>

    @POST("traffic/simulate")
    suspend fun simulateTraffic(@Body request: TrafficRequest): Response<TrafficEvent>
}
