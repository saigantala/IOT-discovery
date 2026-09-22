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
data class AlertDto(
    val id: String?,
    val deviceId: String?,
    val deviceName: String?,
    val trafficEventId: String?,
    val riskScore: Double?,
    val attackType: String?,
    val severity: String?,
    val status: String?,
    val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class AlertListResponse(
    val success: Boolean,
    val data: List<AlertDto>,
    val page: Int
)

@JsonClass(generateAdapter = true)
data class DashboardSummary(
    val deviceCount: Int = 0,
    val alertCount: Int = 0,
    val quarantinedCount: Int = 0,
    val onlineCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class TrafficRequest(
    @Json(name = "deviceId") val deviceId: String,
    @Json(name = "packetRate") val packetRate: Double,
    @Json(name = "destDiversity") val destDiversity: Int,
    @Json(name = "portDiversity") val portDiversity: Int,
    @Json(name = "mqttFreq") val mqttFreq: Double,
    @Json(name = "isSimulatedAttack") val isSimulatedAttack: Boolean = false,
    @Json(name = "attackType") val attackType: String? = null
)

@JsonClass(generateAdapter = true)
data class TrafficEvent(
    val id: String?,
    val deviceId: String?,
    val packetRate: Double?,
    val destDiversity: Int?,
    val portDiversity: Int?,
    val mqttFreq: Double?,
    val riskScore: Double?,
    val isAnomaly: Boolean?,
    val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class DeviceDto(
    val id: String?,
    val deviceId: String?,
    val name: String?,
    val type: String?,
    val ipAddress: String?,
    val status: String?,
    val discoverySource: String?,
    val manufacturer: String?,
    val hostname: String?,
    val os: String?,
    val riskLevel: String?,
    val riskScore: Int?,
    val riskReason: String?,
    val openPorts: List<Int>?,
    val services: List<String>?,
    val fingerprintConfidence: Int?,
    val lastSeen: String?,
    val isQuarantined: Boolean?
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

fun parseDeviceType(typeStr: String?): DeviceType {
    if (typeStr.isNullOrBlank()) return DeviceType.UNKNOWN
    return try {
        DeviceType.valueOf(typeStr.uppercase().trim())
    } catch (e: Exception) {
        DeviceType.UNKNOWN
    }
}

fun parseDeviceStatus(statusStr: String?): DeviceStatus {
    if (statusStr.isNullOrBlank()) return DeviceStatus.UNKNOWN
    return try {
        DeviceStatus.valueOf(statusStr.uppercase().trim())
    } catch (e: Exception) {
        DeviceStatus.UNKNOWN
    }
}

fun parseRiskLevel(riskStr: String?): RiskLevel {
    if (riskStr.isNullOrBlank()) return RiskLevel.LOW
    return try {
        RiskLevel.valueOf(riskStr.uppercase().trim())
    } catch (e: Exception) {
        RiskLevel.LOW
    }
}

fun DeviceDto.toRoomEntity(): Device {
    val devId = deviceId ?: id ?: ipAddress ?: "dev_unknown"
    return Device(
        id = devId,
        name = name ?: "Unverified Device",
        type = parseDeviceType(type),
        manufacturer = manufacturer ?: "Unknown Vendor",
        ipAddress = ipAddress ?: "0.0.0.0",
        macAddress = devId,
        hostname = hostname ?: ipAddress ?: "Host",
        os = os ?: "Embedded Stack",
        riskLevel = parseRiskLevel(riskLevel),
        status = if (isQuarantined == true) DeviceStatus.BLOCKED else parseDeviceStatus(status),
        lastSeen = lastSeen ?: "Active: Just now",
        openPorts = openPorts ?: emptyList(),
        services = services ?: emptyList(),
        fingerprintConfidence = fingerprintConfidence ?: 90,
        riskScore = riskScore ?: 0,
        riskReason = riskReason ?: "Backend Synced",
        discoveryMethod = discoverySource ?: "REST API"
    )
}

fun AlertDto.toAlertItem(): AlertItem {
    return AlertItem(
        id = id ?: "alert_${System.currentTimeMillis()}",
        title = attackType ?: "Security Warning",
        description = "Threat detected on device $deviceName",
        severity = parseRiskLevel(severity),
        timestamp = createdAt ?: "Just now",
        deviceName = deviceName ?: deviceId ?: "Unknown Device",
        deviceIp = deviceId
    )
}
