package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.components.StatCard
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.OrangeWarningLight
import com.example.ui.theme.RedAlert
import com.example.ui.theme.RedAlertLight
import com.example.ui.theme.TealAccent

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToChitties: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToReports: () -> Unit,
    onOpenCreateChitty: () -> Unit,
    onOpenAddMember: () -> Unit,
    onOpenRecordPayment: () -> Unit
) {
    val metrics by viewModel.metrics.collectAsState()
    val admin by viewModel.admin.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val adminDisplayName = admin?.name ?: settings?.adminName ?: "Management"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cheeti Dashboard",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hello, $adminDisplayName",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_cheeti_logo),
                                contentDescription = "Cheeti Official Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("cheeti_logo_dashboard"),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Collection Progress bar
                    val collectionProgress = if (metrics.thisMonthExpectedCollection > 0) {
                        (metrics.thisMonthCollected / metrics.thisMonthExpectedCollection).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    Text(
                        text = "Monthly Collection Progress: ${(collectionProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { collectionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = TealAccent,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }
        }

        // Quick Action Buttons
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevatedButton(
                    onClick = onOpenRecordPayment,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_record_payment_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Payment", fontSize = 13.sp)
                }

                ElevatedButton(
                    onClick = onOpenCreateChitty,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_create_chitty_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Chitty", fontSize = 13.sp)
                }

                ElevatedButton(
                    onClick = onOpenAddMember,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_member_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Member", fontSize = 13.sp)
                }
            }
        }

        // 6 Main Metrics
        item {
            Text(
                text = "Overview Metrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Chitties",
                    value = "${metrics.totalChitties}",
                    icon = Icons.Default.Assignment,
                    iconColor = NavyPrimary,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Active Chitties",
                    value = "${metrics.activeChitties}",
                    icon = Icons.Default.CheckCircle,
                    iconColor = GreenSuccess,
                    backgroundColor = GreenSuccessLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Members",
                    value = "${metrics.totalMembers}",
                    icon = Icons.Default.Group,
                    iconColor = TealAccent,
                    backgroundColor = Color(0xFFCCFBF1),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Expected Collection",
                    value = "₹${metrics.thisMonthExpectedCollection.toLong()}",
                    icon = Icons.Default.TrendingUp,
                    iconColor = NavyPrimary,
                    backgroundColor = Color(0xFFE0E7FF),
                    subtitle = "This Month",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Collected So Far",
                    value = "₹${metrics.thisMonthCollected.toLong()}",
                    icon = Icons.Default.CheckCircle,
                    iconColor = GreenSuccess,
                    backgroundColor = GreenSuccessLight,
                    subtitle = "This Month",
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Total Outstanding",
                    value = "₹${metrics.totalOutstanding.toLong()}",
                    icon = Icons.Default.Warning,
                    iconColor = RedAlert,
                    backgroundColor = RedAlertLight,
                    subtitle = "Pending Dues",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Shortcut to Reports & Reminders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(OrangeWarningLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = OrangeWarning,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pending Reminders",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Send 1-tap WhatsApp payment notices to members",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateToReports,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View")
                    }
                }
            }
        }
    }
}
