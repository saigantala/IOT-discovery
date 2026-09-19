package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mock.MockData
import com.example.ui.components.*
import com.example.ui.navigation.NavRoute
import com.example.utils.ThemeColors

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onSelectDevice: (String) -> Unit,
    onStartScan: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val currentWifi by viewModel.currentWifi.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val timeline by viewModel.timeline.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting & Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enterprise Gateway #01",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hello, Admin",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = systemStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.startScan()
                            onStartScan()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111D25),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("quick_scan_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = "Scan",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isScanning) "Scanning..." else "Scan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Connected Network Card
        item {
            currentWifi?.let { wifi ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = ThemeColors.CyanSecondary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Wifi,
                                            contentDescription = "Wifi",
                                            tint = ThemeColors.CyanSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = wifi.ssid,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Current Connected Network",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = when(wifi.signalLevel) {
                                    in 4..5 -> Icons.Default.SignalWifi4Bar
                                    3 -> Icons.Default.SignalWifiStatusbar4Bar
                                    else -> Icons.Default.SignalWifiStatusbarConnectedNoInternet4
                                },
                                contentDescription = "Signal Strength",
                                tint = ThemeColors.SuccessGreen
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WifiDetailItem("Local IP", wifi.ipAddress, Modifier.weight(1f))
                            WifiDetailItem("Gateway", wifi.gateway, Modifier.weight(1f))
                            WifiDetailItem("Speed", "${wifi.linkSpeed} Mbps", Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Network Health Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Network Health & Security Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val rogueCount = devices.count { it.status == com.example.model.DeviceStatus.ROGUE }
                        val healthScore = if (devices.isEmpty()) 1f else (devices.size - rogueCount).toFloat() / devices.size
                        val healthText = if (healthScore > 0.9f) "HEALTHY" else if (healthScore > 0.7f) "WARNING" else "CRITICAL"
                        val healthColor = if (healthScore > 0.9f) ThemeColors.SuccessGreen else if (healthScore > 0.7f) ThemeColors.WarningOrange else ThemeColors.DangerRed

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = healthColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = healthText,
                                color = healthColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val rogueCount = devices.count { it.status == com.example.model.DeviceStatus.ROGUE }
                        val healthScore = if (devices.isEmpty()) 1f else (devices.size - rogueCount).toFloat() / devices.size
                        val healthColor = if (healthScore > 0.9f) ThemeColors.SuccessGreen else if (healthScore > 0.7f) ThemeColors.WarningOrange else ThemeColors.DangerRed

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(70.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { healthScore },
                                modifier = Modifier.fillMaxSize(),
                                color = healthColor,
                                strokeWidth = 8.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${(healthScore * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            val currentIp = currentWifi?.ipAddress ?: "0.0.0.0"
                            val subnet = if (currentIp != "0.0.0.0") "${currentIp.substringBeforeLast(".")}.0/24" else "Unknown Subnet"
                            
                            Text(
                                text = "${devices.size} Assets on $subnet",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (rogueCount > 0) "$rogueCount Critical threats flagged requiring immediate isolation." else "No active threats detected on the network.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quick Statistics Grid (Horizontal scroll / Grid)
        item {
            Column {
                SectionHeader(title = "Network Statistics")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Assets",
                        value = devices.size.toString(),
                        icon = Icons.Default.Inventory,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(NavRoute.Devices.route) }
                    )
                    StatCard(
                        title = "Online",
                        value = devices.count { it.status == com.example.model.DeviceStatus.ONLINE }.toString(),
                        icon = Icons.Default.CheckCircle,
                        iconColor = ThemeColors.SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(NavRoute.Devices.route) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Rogue Assets",
                        value = devices.count { it.status == com.example.model.DeviceStatus.ROGUE }.toString(),
                        icon = Icons.Default.Report,
                        iconColor = ThemeColors.DangerRed,
                        subtitle = "Needs isolation",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(NavRoute.RogueDevices.route) }
                    )
                    StatCard(
                        title = "Threat Level",
                        value = if (devices.any { it.status == com.example.model.DeviceStatus.ROGUE }) "HIGH" else "LOW",
                        icon = Icons.Default.Security,
                        iconColor = if (devices.any { it.status == com.example.model.DeviceStatus.ROGUE }) ThemeColors.DangerRed else ThemeColors.SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(NavRoute.DiscoveryLogs.route) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "System Settings",
                        value = "Config",
                        icon = Icons.Default.Settings,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(NavRoute.Settings.route) }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Quick Actions Row
        item {
            Column {
                SectionHeader(title = "Quick Actions")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val actions = listOf(
                        Triple("Logs", Icons.Default.ReceiptLong, NavRoute.DiscoveryLogs.route),
                        Triple("Settings", Icons.Default.Settings, NavRoute.Settings.route),
                        Triple("Topology", Icons.Default.Hub, NavRoute.Topology.route),
                        Triple("Compliance", Icons.Default.VerifiedUser, NavRoute.Compliance.route),
                        Triple("Fingerprints", Icons.Default.Fingerprint, NavRoute.Fingerprints.route),
                        Triple("Reports", Icons.Default.Assessment, NavRoute.Reports.route)
                    )
                    items(actions) { (label, icon, route) ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .width(110.dp)
                                .clickable { onNavigate(route) }
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Security Alerts Section
        item {
            SectionHeader(
                title = "Recent Alerts",
                actionLabel = "View All",
                onActionClick = { onNavigate(NavRoute.Alerts.route) }
            )
        }

        items(alerts) { alert ->
            AlertCard(
                alert = alert,
                onClick = { onNavigate(NavRoute.Alerts.route) }
            )
        }

        // Recent Devices Discovered
        item {
            SectionHeader(
                title = "Recent Devices",
                actionLabel = "View All (${devices.size})",
                onActionClick = { onNavigate(NavRoute.Devices.route) }
            )
        }

        items(devices.take(4)) { device ->
            DeviceCard(
                device = device,
                onClick = { onSelectDevice(device.id) }
            )
        }

        // Recent Activity Timeline
        item {
            Column {
                SectionHeader(
                    title = "Activity Timeline",
                    actionLabel = "Full Log",
                    onActionClick = { onNavigate(NavRoute.Timeline.route) }
                )
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        timeline.forEachIndexed { index, event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = "Event",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = event.deviceName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = event.timestamp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = event.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < timeline.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(start = 44.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiDetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
