package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val success: Boolean,
    val accessToken: String?,
    val refreshToken: String?,
    val user: UserDto?
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val success: Boolean,
    val message: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    val success: Boolean,
    val data: UserDto
)

@JsonClass(generateAdapter = true)
data class AlertListResponse(
    val success: Boolean,
    val data: List<AlertItem>,
    val page: Int
)

@JsonClass(generateAdapter = true)
data class DashboardSummary(
    val deviceCount: Int,
    val alertCount: Int,
    val quarantinedCount: Int
)

@JsonClass(generateAdapter = true)
data class TrafficRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "packet_rate") val packetRate: Double,
    @Json(name = "dest_diversity") val destDiversity: Int,
    @Json(name = "port_diversity") val portDiversity: Int,
    @Json(name = "mqtt_freq") val mqttFreq: Double,
    @Json(name = "is_simulated_attack") val isSimulatedAttack: Boolean,
    @Json(name = "attack_type") val attackType: String?
)

@JsonClass(generateAdapter = true)
data class TrafficEvent(
    val id: String,
    val deviceId: String,
    val riskScore: Double,
    val isAnomaly: Boolean
)

@JsonClass(generateAdapter = true)
data class DeviceSyncDto(
    val deviceId: String,
    val name: String,
    val type: String,
    val ipAddress: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class SyncDevicesRequest(
    val devices: List<DeviceSyncDto>
)
