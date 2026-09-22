package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.network.RetrofitClient
import com.example.utils.ConfigManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val configManager = remember { ConfigManager(context) }
    val scope = rememberCoroutineScope()
    
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoScanEnabled by remember { mutableStateOf(true) }
    var cloudSyncEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English (US)") }
    
    var showIpDialog by remember { mutableStateOf(false) }
    var backendIp by remember { mutableStateOf(configManager.getBackendIp()) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testResultSuccess by remember { mutableStateOf(false) }

    fun runTestConnection(targetIp: String) {
        isTestingConnection = true
        testResultText = null
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                val response = api.checkHealth()
                if (response.isSuccessful && response.body()?.status == "OK") {
                    val dbStatus = response.body()?.database?.status ?: "connected"
                    testResultSuccess = true
                    testResultText = "✅ Connection Successful! Server Online | Database: $dbStatus"
                } else {
                    testResultSuccess = false
                    testResultText = "❌ Server Error (${response.code()}): ${response.errorBody()?.string() ?: "Unreachable"}"
                }
            } catch (e: Exception) {
                testResultSuccess = false
                testResultText = "❌ Connection Failed: ${e.message ?: "Unable to connect to http://$targetIp:4000/health"}"
            } finally {
                isTestingConnection = false
            }
        }
    }

    if (showIpDialog) {
        var tempIp by remember { mutableStateOf(backendIp) }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Server Connection Setup") },
            text = {
                Column {
                    Text(
                        text = "Enter the PC Wi-Fi IP address running the Node.js backend server:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = {
                            tempIp = it
                            isError = !configManager.isValidIpAddress(it)
                            testResultText = null
                        },
                        label = { Text("Backend IP Address") },
                        placeholder = { Text("e.g. ${ConfigManager.DEFAULT_IP}") },
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text(
                                    text = "Invalid IPv4 address format (e.g. 172.30.116.78)",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "Target API: http://${tempIp.ifEmpty { "IP" }}:4000/api/v1/",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isTestingConnection) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing connection to http://$tempIp:4000/health...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (testResultText != null) {
                        Text(
                            text = testResultText!!,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (testResultSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            configManager.setBackendIp(tempIp)
                            runTestConnection(tempIp)
                        },
                        enabled = configManager.isValidIpAddress(tempIp) && !isTestingConnection,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = "Test")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Connection (/health)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = configManager.setBackendIp(tempIp)
                        if (success) {
                            backendIp = configManager.getBackendIp()
                            showIpDialog = false
                            Toast.makeText(context, "Backend URL updated: ${configManager.getBaseUrl()}", Toast.LENGTH_LONG).show()
                        } else {
                            isError = true
                        }
                    },
                    enabled = configManager.isValidIpAddress(tempIp)
                ) {
                    Text("Save & Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Backend Server Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingClickRow(
                        title = "Server IP Address",
                        value = backendIp,
                        subtitle = configManager.getBaseUrl(),
                        icon = Icons.Default.Computer,
                        onClick = { showIpDialog = true },
                        testTag = "backend_ip_setting"
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Display & Visual Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Dark Theme",
                        subtitle = "Enable high contrast dark navy palette",
                        icon = Icons.Default.DarkMode,
                        checked = isDarkTheme,
                        onCheckedChange = onToggleDarkTheme,
                        testTag = "dark_theme_switch"
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Network Scanning & Threat Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Automated Subnet Sweep",
                        subtitle = "Perform background mDNS/ARP active scans every 15 mins",
                        icon = Icons.Default.Radar,
                        checked = autoScanEnabled,
                        onCheckedChange = { autoScanEnabled = it },
                        testTag = "auto_scan_switch"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    SettingToggleRow(
                        title = "Real-time Rogue Alerts",
                        subtitle = "Push instant alerts when promiscuous sniffer detected",
                        icon = Icons.Default.Notifications,
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        testTag = "notifications_switch"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    SettingToggleRow(
                        title = "Cloud Threat Intelligence Sync",
                        subtitle = "Sync latest CVE signatures from Cisco & Mitre database",
                        icon = Icons.Default.CloudSync,
                        checked = cloudSyncEnabled,
                        onCheckedChange = { cloudSyncEnabled = it },
                        testTag = "cloud_sync_switch"
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "System & Localization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingClickRow(
                        title = "Language",
                        value = selectedLanguage,
                        subtitle = null,
                        icon = Icons.Default.Language,
                        onClick = {},
                        testTag = "language_setting"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    SettingClickRow(
                        title = "About IoT Device Discovery",
                        value = "v2.5.0 Enterprise",
                        subtitle = null,
                        icon = Icons.Default.Info,
                        onClick = onNavigateToAbout,
                        testTag = "about_app_setting"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingClickRow(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
