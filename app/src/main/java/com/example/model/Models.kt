package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class DeviceType(val displayName: String) {
    ROUTER("Gateway"),
    SWITCH("Switch"),
    ACCESS_POINT("Access Point"),
    CAMERA("Camera"),
    PRINTER("Printer"),
    SPEAKER("Speaker"),
    THERMOSTAT("Thermostat"),
    SMART_TV("TV"),
    SMART_BULB("Bulb"),
    GATEWAY("Bridge"),
    SERVER("Server"),
    LAPTOP("Laptop"),
    MOBILE("Mobile"),
    UNKNOWN("Other")
}

enum class RiskLevel(val label: String, val scoreRange: String) {
    LOW("Low Risk", "0-25"),
    MEDIUM("Medium Risk", "26-50"),
    HIGH("High Risk", "51-75"),
    CRITICAL("Critical Risk", "76-100")
}

enum class DeviceStatus(val label: String) {
    ONLINE("Online"),
    OFFLINE("Offline"),
    UNKNOWN("Unknown"),
    ROGUE("Rogue"),
    BLOCKED("Blocked")
}

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey val id: String,
    val name: String,
    val type: DeviceType,
    val manufacturer: String,
    val ipAddress: String,
    val macAddress: String,
    val hostname: String,
    val os: String,
    val riskLevel: RiskLevel,
    val status: DeviceStatus,
    val lastSeen: String,
    val openPorts: List<Int>,
    val services: List<String>,
    val fingerprintConfidence: Int,
    val riskScore: Int,
    val riskReason: String,
    val discoveryMethod: String,
    val location: String = "Main Office Network"
)

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        val type = Types.newParameterizedType(List::class.java, Integer::class.java)
        return moshi.adapter<List<Int>>(type).toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = Types.newParameterizedType(List::class.java, Integer::class.java)
        return moshi.adapter<List<Int>>(type).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromDeviceType(value: DeviceType) = value.name

    @TypeConverter
    fun toDeviceType(value: String) = DeviceType.valueOf(value)

    @TypeConverter
    fun fromRiskLevel(value: RiskLevel) = value.name

    @TypeConverter
    fun toRiskLevel(value: String) = RiskLevel.valueOf(value)

    @TypeConverter
    fun fromDeviceStatus(value: DeviceStatus) = value.name

    @TypeConverter
    fun toDeviceStatus(value: String) = DeviceStatus.valueOf(value)
}

data class AlertItem(
    val id: String,
    val title: String,
    val description: String,
    val severity: RiskLevel,
    val timestamp: String,
    val deviceName: String? = null,
    val deviceIp: String? = null,
    val isRead: Boolean = false
)

data class TimelineEvent(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val eventType: String, // Connected, Disconnected, Scanned, Flagged
    val timestamp: String,
    val details: String
)

data class DiscoveryLog(
    val id: String,
    val timestamp: String,
    val deviceName: String,
    val ipAddress: String,
    val status: DeviceStatus,
    val discoveryMethod: String
)

data class FingerprintData(
    val id: String,
    val vendor: String,
    val services: List<String>,
    val ports: List<Int>,
    val httpHeaders: Map<String, String>,
    val confidenceScore: Int,
    val predictedDevice: String
)

data class ComplianceReport(
    val score: Int,
    val inventoryTotal: Int,
    val weakPasswordCount: Int,
    val openInsecurePorts: Int,
    val outdatedFirmware: Int,
    val recommendations: List<String>
)

data class NetworkNode(
    val id: String,
    val label: String,
    val type: DeviceType,
    val ip: String,
    val status: DeviceStatus,
    val parentId: String? = null
)

data class LoginHistory(
    val id: String,
    val adminName: String,
    val loginTime: String,
    val ipAddress: String,
    val location: String,
    val deviceModel: String,
    val status: String // Success, Failed
)

data class NearbyNetwork(
    val ssid: String,
    val bssid: String,
    val signalStrength: Int, // dBm
    val security: String,
    val isConnected: Boolean = false,
    val channel: Int
)

data class WifiConnectionInfo(
    val ssid: String,
    val bssid: String,
    val ipAddress: String,
    val gateway: String,
    val signalLevel: Int, // 0-4
    val frequency: Int, // MHz
    val linkSpeed: Int // Mbps
)
