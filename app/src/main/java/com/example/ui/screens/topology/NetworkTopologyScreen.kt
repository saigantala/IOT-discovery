package com.example.ui.screens.topology

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mock.MockData
import com.example.model.DeviceStatus
import com.example.model.DeviceType
import com.example.model.NetworkNode
import com.example.ui.components.getDeviceIcon
import com.example.ui.components.getDeviceVisuals
import com.example.utils.ThemeColors
import com.example.utils.toColor

@Composable
fun NetworkTopologyScreen(
    onSelectDevice: (String) -> Unit,
    viewModel: NetworkTopologyViewModel = viewModel()
) {
    val nodes by viewModel.nodes.collectAsStateWithLifecycle()
    var zoomLevel by remember { mutableStateOf(100) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    // Update selected node if list changes and previous selection is gone
    LaunchedEffect(nodes) {
        if (selectedNodeId == null && nodes.isNotEmpty()) {
            selectedNodeId = nodes.firstOrNull { it.type == DeviceType.ROUTER }?.id ?: nodes.firstOrNull()?.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("network_topology_screen")
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Info & Controls Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live L2 / L3 Topology",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (nodes.isEmpty()) "Connect to Wi-Fi & Scan to visualize" else "Dynamic Network Visualization",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Zoom Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (zoomLevel > 60) zoomLevel -= 10 },
                            modifier = Modifier.size(36.dp).testTag("zoom_out_button")
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
                        }
                        Text(
                            text = "$zoomLevel%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { if (zoomLevel < 140) zoomLevel += 10 },
                            modifier = Modifier.size(36.dp).testTag("zoom_in_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryTag("Discovered", "${nodes.size}", ThemeColors.CyanSecondary)
                    SummaryTag("Links", "${if (nodes.size > 1) nodes.size - 1 else 0}", MaterialTheme.colorScheme.primary)
                    SummaryTag("Status", if (nodes.isNotEmpty()) "Monitoring" else "Ready", if (nodes.isNotEmpty()) ThemeColors.SuccessGreen else Color.Gray)
                }
            }
        }

        // Graph Visualizer Canvas Box
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (nodes.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WifiOff, null, Modifier.size(48.dp), Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("No live assets detected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Topology Hierarchy Tree View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Level 1: Root Node (Router or first device)
                        val rootNode = nodes.find { it.parentId == null } ?: nodes.first()
                        TopologyNodeWidget(
                            node = rootNode,
                            isSelected = selectedNodeId == rootNode.id,
                            onClick = { selectedNodeId = rootNode.id }
                        )

                        if (nodes.size > 1) {
                            // Vertical Down Link Line
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            )

                            // Level 2: Leaf Devices
                            val leafNodes = nodes.filter { it.id != rootNode.id }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 24.dp)
                            ) {
                                items(leafNodes) { leaf ->
                                    TopologyNodeWidget(
                                        node = leaf,
                                        isSelected = selectedNodeId == leaf.id,
                                        onClick = { selectedNodeId = leaf.id }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Topology Legend Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Asset Legend",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem("Gateway", ThemeColors.PrimaryNavy)
                    LegendItem("Mobile", ThemeColors.SuccessGreen)
                    LegendItem("Unknown", Color.Gray)
                    LegendItem("Rogue", ThemeColors.DangerRed)
                }
            }
        }
    }
}

@Composable
private fun TopologyNodeWidget(
    node: NetworkNode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val (icon, statusColor) = getDeviceVisuals(node.type, node.status, node.label)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (node.status == DeviceStatus.ROGUE) ThemeColors.DangerRed.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp),
        modifier = Modifier
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("topology_node_${node.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = node.label,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = node.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = node.ip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryTag(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
