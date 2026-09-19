package com.example.ui.screens.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.model.DeviceType

@Composable
fun DevicesScreen(
    onSelectDevice: (String) -> Unit,
    onStartScan: () -> Unit,
    viewModel: DevicesViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Direct category buttons as requested
    val filterOptions = listOf("All", "Mobile", "Laptop", "Gateway", "Camera", "TV", "Other")

    val allDevices by viewModel.devices.collectAsStateWithLifecycle()

    val filteredDevices = remember(searchQuery, selectedFilter, allDevices) {
        allDevices.filter { device ->
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Mobile" -> device.type == DeviceType.MOBILE
                "Laptop" -> device.type == DeviceType.LAPTOP
                "Gateway" -> device.type == DeviceType.ROUTER || device.type == DeviceType.GATEWAY || device.type == DeviceType.ACCESS_POINT
                "Camera" -> device.type == DeviceType.CAMERA
                "TV" -> device.type == DeviceType.SMART_TV
                "Other" -> !listOf(DeviceType.MOBILE, DeviceType.LAPTOP, DeviceType.ROUTER, DeviceType.GATEWAY, DeviceType.ACCESS_POINT, DeviceType.CAMERA, DeviceType.SMART_TV).contains(device.type)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    device.name.contains(searchQuery, ignoreCase = true) ||
                    device.ipAddress.contains(searchQuery, ignoreCase = true) ||
                    device.macAddress.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartScan,
                containerColor = Color(0xFF111D25),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Refresh Scan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            CustomSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search ${allDevices.size} live subnet assets..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Live Asset Inventory",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FilterChipGroup(
                options = filterOptions,
                selectedOption = selectedFilter,
                onOptionSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredDevices.isEmpty()) {
                EmptyView(
                    title = if (allDevices.isEmpty()) "Subnet Inventory Empty" else "No $selectedFilter Devices",
                    description = if (allDevices.isEmpty()) 
                        "Initiate a Deep Scan to identify and categorize assets on your current Wi-Fi." 
                        else "No active devices match the '$selectedFilter' category in your recent scan."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Connected Subnet Assets (${filteredDevices.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(filteredDevices, key = { it.id }) { device ->
                        DeviceCard(
                            device = device,
                            onClick = { onSelectDevice(device.id) }
                        )
                    }
                }
            }
        }
    }
}
