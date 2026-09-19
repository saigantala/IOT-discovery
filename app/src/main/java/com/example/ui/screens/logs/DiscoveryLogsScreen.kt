package com.example.ui.screens.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mock.MockData
import com.example.model.DiscoveryLog
import com.example.model.LoginHistory
import com.example.ui.components.CustomSearchBar
import com.example.ui.components.StatusChip
import com.example.ui.screens.history.LoginHistoryCard
import com.example.ui.components.getDeviceVisuals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryLogsScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Device Logs", "Login History")
    val icons = listOf(Icons.Default.ReceiptLong, Icons.Default.History)

    val discoveryLogs by viewModel.discoveryLogs.collectAsStateWithLifecycle()
    val loginHistory by viewModel.loginHistory.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("discovery_logs_screen")
    ) {
        // Live Status Indicator
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = com.example.utils.ThemeColors.SuccessGreen,
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIVE SYSTEM MONITORING ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold) },
                    icon = { Icon(icons[index], contentDescription = null) }
                )
            }
        }

        when (selectedTab) {
            0 -> DeviceLogsTab(discoveryLogs)
            1 -> LoginHistoryTab(loginHistory)
        }
    }
}

@Composable
fun DeviceLogsTab(logs: List<DiscoveryLog>) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(searchQuery, logs) {
        logs.filter {
            searchQuery.isBlank() ||
                    it.deviceName.contains(searchQuery, ignoreCase = true) ||
                    it.ipAddress.contains(searchQuery, ignoreCase = true) ||
                    it.discoveryMethod.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        CustomSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search logs by protocol, IP, or asset name..."
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredLogs) { log ->
                DiscoveryLogCard(log)
            }
        }
    }
}

@Composable
fun LoginHistoryTab(history: List<LoginHistory>) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredHistory = remember(searchQuery, history) {
        history.filter {
            searchQuery.isBlank() ||
                    it.adminName.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true) ||
                    it.ipAddress.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        CustomSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search by admin name, location, or IP..."
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredHistory) { entry ->
                LoginHistoryCard(entry)
            }
        }
    }
}

@Composable
fun DiscoveryLogCard(log: DiscoveryLog) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint) = getDeviceVisuals(com.example.model.DeviceType.UNKNOWN, log.status, log.deviceName)
            
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Log",
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.deviceName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusChip(status = log.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "IP: ${log.ipAddress} • Method: ${log.discoveryMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
