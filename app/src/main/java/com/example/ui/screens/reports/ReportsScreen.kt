package com.example.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SectionHeader

data class ReportTypeItem(
    val title: String,
    val description: String,
    val lastGenerated: String,
    val format: String
)

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel()
) {
    val deviceCount by viewModel.deviceCount.collectAsStateWithLifecycle()
    val compliance by viewModel.compliance.collectAsStateWithLifecycle()
    
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    val reports = listOf(
        ReportTypeItem(
            title = "Inventory Asset Report",
            description = "Detailed enumeration of all $deviceCount discovered IoT nodes, MAC OUIs, and vendor footprints.",
            lastGenerated = "Just now",
            format = "PDF / CSV"
        ),
        ReportTypeItem(
            title = "NIST Compliance Report",
            description = "Security Assessment Score: ${compliance?.score ?: 0}%. Based on detected insecure ports and rogue assets.",
            lastGenerated = "Today",
            format = "PDF"
        ),
        ReportTypeItem(
            title = "Rogue Threat & Vulnerability Audit",
            description = "Audit trail of detected security anomalies and recommended isolation actions.",
            lastGenerated = "Recent",
            format = "PDF / JSON"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        SectionHeader(title = "Live Enterprise Security Reports")

        if (exportStatusMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = exportStatusMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(reports.size) { index ->
                val report = reports[index]
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = "Report",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            SuggestionChip(
                                onClick = {},
                                label = { Text(report.format) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = report.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Last generated: ${report.lastGenerated}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { exportStatusMessage = "Exporting ${report.title} as PDF..." },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "PDF")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export PDF")
                            }

                            OutlinedButton(
                                onClick = { exportStatusMessage = "Exporting ${report.title} as CSV..." },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "CSV")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV")
                            }
                        }
                    }
                }
            }
        }
    }
}
