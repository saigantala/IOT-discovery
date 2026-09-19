package com.example.utils

import androidx.compose.ui.graphics.Color
import com.example.model.DeviceStatus
import com.example.model.RiskLevel

object ThemeColors {
    val DeepBlue = Color(0xFF0F2027)
    val PrimaryNavy = Color(0xFF1E3A8A)
    val CyanSecondary = Color(0xFF00E5FF)
    val DarkSurface = Color(0xFF1E293B)
    val DarkBackground = Color(0xFF0F172A)
    val LightBackground = Color(0xFFF8FAFC)
    val LightCard = Color(0xFFFFFFFF)
    
    val SuccessGreen = Color(0xFF10B981)
    val WarningOrange = Color(0xFFF59E0B)
    val DangerRed = Color(0xFFEF4444)
    val CriticalPurple = Color(0xFF8B5CF6)
}

fun RiskLevel.toColor(): Color {
    return when (this) {
        RiskLevel.LOW -> ThemeColors.SuccessGreen
        RiskLevel.MEDIUM -> ThemeColors.WarningOrange
        RiskLevel.HIGH -> ThemeColors.DangerRed
        RiskLevel.CRITICAL -> ThemeColors.CriticalPurple
    }
}

fun DeviceStatus.toColor(): Color {
    return when (this) {
        DeviceStatus.ONLINE -> ThemeColors.SuccessGreen
        DeviceStatus.OFFLINE -> Color.Gray
        DeviceStatus.UNKNOWN -> ThemeColors.WarningOrange
        DeviceStatus.ROGUE -> ThemeColors.DangerRed
        DeviceStatus.BLOCKED -> ThemeColors.CriticalPurple
    }
}
