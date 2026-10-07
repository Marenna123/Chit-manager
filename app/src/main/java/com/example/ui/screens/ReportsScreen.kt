package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RedAlert
import com.example.ui.theme.RedAlertLight

@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val members by viewModel.members.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val chitties by viewModel.chitties.collectAsState()
    val metrics by viewModel.metrics.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                selectedContentColor = NavyPrimary,
                unselectedContentColor = Color(0xFF64748B),
                text = { Text("Outstanding Dues", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                selectedContentColor = NavyPrimary,
                unselectedContentColor = Color(0xFF64748B),
                text = { Text("Payment Receipts (${receipts.size})", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                selectedContentColor = NavyPrimary,
                unselectedContentColor = Color(0xFF64748B),
                text = { Text("Chitty Overview", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        when (selectedTabIndex) {
            0 -> {
                // Outstanding Dues Report with 1-tap WhatsApp reminder
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = RedAlertLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = RedAlert,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Total System Outstanding",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = RedAlert
                                        )
                                        Text(
                                            text = "₹${metrics.totalOutstanding.toLong()}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RedAlert
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Member Dues Ledger",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(members) { member ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${member.name} (${member.memberCode})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = member.mobileNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = member.status)

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { viewModel.prepareWhatsAppReminder(member) },
                                        modifier = Modifier.testTag("report_whatsapp_${member.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "Send WhatsApp Reminder",
                                            tint = Color(0xFF25D366)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Receipts Ledger
                if (receipts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No payment receipts generated yet.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(receipts) { receipt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.showReceipt(receipt) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(GreenSuccessLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = GreenSuccess)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = receipt.receiptNumber,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${receipt.memberName} • ${receipt.date}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Method: ${receipt.paymentMethod}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Text(
                                        text = "₹${receipt.amount.toLong()}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GreenSuccess
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Chitties Overview
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chitties) { chitty ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = chitty.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    StatusBadge(status = chitty.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Total: ₹${chitty.totalAmount.toLong()}", style = MaterialTheme.typography.bodySmall)
                                    Text(text = "Monthly: ₹${chitty.monthlyInstallment.toLong()}", style = MaterialTheme.typography.bodySmall)
                                    Text(text = "${chitty.durationMonths} Mos", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
