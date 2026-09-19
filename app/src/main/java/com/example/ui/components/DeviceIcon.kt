package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.Device
import com.example.model.DeviceStatus
import com.example.model.DeviceType

@Composable
fun DeviceIcon(
    device: Device,
    size: Dp = 64.dp, // Increased from 56
    iconSize: Dp = 36.dp, // Increased from 32
    modifier: Modifier = Modifier
) {
    val (icon, tint) = getDeviceVisuals(device.type, device.status, device.name)
    
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "${device.type.displayName} icon",
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getDeviceVisuals(type: DeviceType, status: DeviceStatus, name: String = ""): Pair<ImageVector, Color> {
    if (status == DeviceStatus.ROGUE) {
        return Icons.Default.Report to Color(0xFFF0444B) // Red
    }
    
    if (status == DeviceStatus.OFFLINE) {
        return Icons.Default.WifiOff to Color.Gray
    }

    val lowerName = name.lowercase()
    
    return when {
        type == DeviceType.ROUTER || type == DeviceType.GATEWAY || lowerName.contains("gateway") || lowerName.contains("router") -> 
            Icons.Default.Router to Color(0xFF668CFF) // Blue
            
        type == DeviceType.CAMERA || lowerName.contains("camera") || lowerName.contains("cctv") -> 
            Icons.Default.Videocam to Color(0xFF00B8E8) // Cyan
            
        type == DeviceType.SMART_TV || lowerName.contains("tv") -> 
            Icons.Default.Tv to Color(0xFFE91E63) // Pink
            
        type == DeviceType.MOBILE || lowerName.contains("phone") || lowerName.contains("android") || lowerName.contains("iphone") -> 
            Icons.Default.Smartphone to Color(0xFF15C58A) // Green
            
        type == DeviceType.LAPTOP || lowerName.contains("laptop") || lowerName.contains("macbook") || lowerName.contains("pc") -> 
            Icons.Default.Laptop to Color(0xFF607D8B) // Blue-Gray
            
        type == DeviceType.PRINTER -> 
            Icons.Default.Print to Color(0xFF795548) // Brown
            
        type == DeviceType.SMART_BULB -> 
            Icons.Default.Lightbulb to Color(0xFFFFD700) // Amber
            
        type == DeviceType.THERMOSTAT -> 
            Icons.Default.Thermostat to Color(0xFFFFB020) // Orange
            
        type == DeviceType.SPEAKER -> 
            Icons.Default.Speaker to Color(0xFF9C27B0) // Purple
            
        type == DeviceType.SERVER -> 
            Icons.Default.Storage to Color(0xFF607D8B) // Blue-Gray
            
        type == DeviceType.SWITCH -> 
            Icons.Default.Hub to Color(0xFF00B8E8)
            
        type == DeviceType.ACCESS_POINT -> 
            Icons.Default.Wifi to Color(0xFF00B8E8)
            
        type == DeviceType.UNKNOWN -> 
            Icons.AutoMirrored.Filled.HelpCenter to Color(0xFF8BA2AB)

        else -> Icons.Default.Devices to Color(0xFF15C58A)
    }
}
