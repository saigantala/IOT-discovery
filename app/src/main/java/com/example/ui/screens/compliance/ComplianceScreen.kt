package com.example.ui.screens.compliance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mock.MockData
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.utils.ThemeColors

@Composable
fun ComplianceScreen(
    viewModel: ComplianceViewModel = viewModel()
) {
    val reportOpt by viewModel.report.collectAsStateWithLifecycle()
    val report = reportOpt ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("compliance_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Compliance Score Hero
        item {
            val scoreColor = when {
                report.score > 80 -> ThemeColors.SuccessGreen
                report.score > 50 -> ThemeColors.WarningOrange
                else -> ThemeColors.DangerRed
            }
            
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Live Network Compliance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Real-time Security Assessment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Compliance",
                            tint = scoreColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Score: ${report.score}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = scoreColor
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = scoreColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (report.score > 80) "SECURE" else "NEEDS ATTENTION",
                                color = scoreColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { report.score / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = scoreColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Metrics Breakdown Grid
        item {
            Column {
                SectionHeader(title = "Live Audit Metrics")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Weak Config",
                        value = "${report.weakPasswordCount}",
                        icon = Icons.Default.Warning,
                        iconColor = ThemeColors.WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Insecure Ports",
                        value = "${report.openInsecurePorts}",
                        icon = Icons.Default.Warning,
                        iconColor = ThemeColors.DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Low Trust",
                        value = "${report.outdatedFirmware}",
                        icon = Icons.Default.Warning,
                        iconColor = ThemeColors.WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Assets Scanned",
                        value = "${report.inventoryTotal}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = ThemeColors.SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Actionable Recommendations
        item {
            SectionHeader(title = "Immediate Security Actions")
        }

        if (report.recommendations.isEmpty()) {
            item {
                Text("No critical actions required.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        items(report.recommendations) { rec ->
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (rec.contains("Isolate", true)) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = "Recommendation",
                        tint = if (rec.contains("Isolate", true)) ThemeColors.DangerRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = rec,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
