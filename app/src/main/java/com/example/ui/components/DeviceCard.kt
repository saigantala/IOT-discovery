package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Device
import com.example.model.DeviceType

@Composable
fun getDeviceIcon(type: DeviceType): ImageVector {
    return when (type) {
        DeviceType.ROUTER -> Icons.Default.Router
        DeviceType.SWITCH -> Icons.Default.Hub
        DeviceType.ACCESS_POINT -> Icons.Default.Wifi
        DeviceType.CAMERA -> Icons.Default.Videocam
        DeviceType.PRINTER -> Icons.Default.Print
        DeviceType.SPEAKER -> Icons.Default.Speaker
        DeviceType.THERMOSTAT -> Icons.Default.Thermostat
        DeviceType.SMART_TV -> Icons.Default.Tv
        DeviceType.SMART_BULB -> Icons.Default.Lightbulb
        DeviceType.GATEWAY -> Icons.Default.Sensors
        DeviceType.SERVER -> Icons.Default.Storage
        DeviceType.LAPTOP -> Icons.Default.Computer
        DeviceType.MOBILE -> Icons.Default.Smartphone
        DeviceType.UNKNOWN -> Icons.Default.QuestionMark
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceIcon(device = device, size = 64.dp, iconSize = 34.dp)

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RiskBadge(riskLevel = device.riskLevel)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${device.manufacturer} • ${device.ipAddress}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MAC: ${device.macAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    StatusChip(status = device.status)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Device Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
