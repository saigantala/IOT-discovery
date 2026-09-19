package com.example.ui.screens.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mock.MockData
import com.example.ui.components.AlertCard
import com.example.ui.components.FilterChipGroup
import com.example.ui.components.SectionHeader

@Composable
fun AlertsScreen(
    onSelectDevice: (String) -> Unit,
    viewModel: AlertsViewModel = viewModel()
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Critical", "High", "Medium", "Low")

    val filteredAlerts = remember(selectedFilter, alerts) {
        alerts.filter {
            selectedFilter == "All" || it.severity.name.equals(selectedFilter, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("alerts_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        FilterChipGroup(
            options = filterOptions,
            selectedOption = selectedFilter,
            onOptionSelected = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeader(title = "Live Security Alerts (${filteredAlerts.size})")

        if (filteredAlerts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Shield, null, Modifier.size(64.dp), Color.Gray.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))
                Text("No active alerts", color = MaterialTheme.colorScheme.onSurface)
                Text("Your network is currently monitored and clear.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAlerts) { alert ->
                    AlertCard(
                        alert = alert,
                        onClick = {
                            if (alert.id.startsWith("alert_")) {
                                onSelectDevice(alert.id.removePrefix("alert_"))
                            }
                        }
                    )
                }
            }
        }
    }
}
