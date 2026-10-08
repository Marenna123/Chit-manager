package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberChittySummary
import com.example.ui.MainViewModel
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.OrangeWarningLight
import com.example.ui.theme.RedAlert
import com.example.ui.theme.RedAlertLight
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onRecordPayment: (Long) -> Unit
) {
    LaunchedEffect(memberId) {
        viewModel.loadMemberDashboard(memberId)
    }

    val summary by viewModel.selectedMemberSummary.collectAsState()

    if (summary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading member dashboard...")
        }
        return
    }

    val member = summary!!.member

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${member.name} (${member.memberCode})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.prepareWhatsAppReminder(member) },
                        modifier = Modifier.testTag("member_whatsapp_button")
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("member_dashboard_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Contact Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = member.mobileNumber, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            StatusBadge(status = member.status)
                        }

                        if (member.address.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Address: ${member.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.prepareWhatsAppReminder(member) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp Reminder")
                            }

                            Button(
                                onClick = { onRecordPayment(member.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay")
                            }
                        }
                    }
                }
            }

            // PRIMARY PAYMENT DUES & BALANCE OVERVIEW
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_dues_breakdown_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment Dues & Balance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (summary!!.totalChitties > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (summary!!.totalChitties >= 2) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (summary!!.totalChitties >= 2) "${summary!!.totalChitties} Chitties" else "1 Chitty",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (summary!!.totalChitties >= 2) NavyPrimary else Color(0xFF475569)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Total Paying Current Month
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "1. Total Paying Current Month",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (summary!!.currentMonthPaid > 0)
                                        "Paid: ₹${summary!!.currentMonthPaid.toLong()} | Pending: ₹${summary!!.currentMonthPending.toLong()}"
                                    else
                                        "Current Month Scheduled Due",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "₹${summary!!.currentMonthDue.toLong()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary!!.currentMonthPending <= 0.0) GreenSuccess else OrangeWarning
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Previous Payment Any Pending (Old Pending)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "2. Previous Months Pending (Old Balance)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (summary!!.previousOutstanding > 0)
                                        "⚠️ Past unpaid installments"
                                    else
                                        "✅ All previous months cleared",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (summary!!.previousOutstanding > 0) RedAlert else GreenSuccess
                                )
                            }
                            Text(
                                text = "₹${summary!!.previousOutstanding.toLong()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary!!.previousOutstanding > 0) RedAlert else GreenSuccess
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Clearly add and show balance old and current month
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (summary!!.totalDue > 0) RedAlertLight else GreenSuccessLight,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL BALANCE DUE",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (summary!!.totalDue > 0) RedAlert else GreenSuccess
                                        )
                                        Text(
                                            text = "(Old Pending + Current Month)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (summary!!.totalDue > 0) RedAlert.copy(alpha = 0.8f) else GreenSuccess.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        text = "₹${summary!!.totalDue.toLong()}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (summary!!.totalDue > 0) RedAlert else GreenSuccess
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "₹${summary!!.currentMonthPending.toLong()} (Current Month) + ₹${summary!!.previousOutstanding.toLong()} (Old Balance) = ₹${summary!!.totalDue.toLong()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (summary!!.totalDue > 0) RedAlert else GreenSuccess
                                )
                            }
                        }

                        // 4. If person is in 2 or 3 cheetis, show individual cheeti breakdown
                        if (summary!!.chittySummaries.size >= 2) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Cheeti-wise Dues Breakdown (${summary!!.chittySummaries.size} Chitties):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                summary!!.chittySummaries.forEach { cs ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = cs.chitty.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "₹${cs.monthlyInstallment.toLong()}/mo",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Current: ₹${cs.currentMonthDue.toLong()}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = OrangeWarning
                                                )
                                                Text(
                                                    text = "Old: ₹${cs.previousOutstanding.toLong()}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (cs.previousOutstanding > 0) RedAlert else GreenSuccess
                                                )
                                                Text(
                                                    text = "Total: ₹${cs.totalDue.toLong()}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (cs.totalDue > 0) RedAlert else GreenSuccess
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ACCOUNT SUMMARY CARDS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Paid",
                        value = "₹${summary!!.totalPaid.toLong()}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = GreenSuccess,
                        backgroundColor = GreenSuccessLight,
                        subtitle = "Till Date",
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Advance Credit",
                        value = "₹${summary!!.advanceCredit.toLong()}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = TealAccent,
                        backgroundColor = Color(0xFFCCFBF1),
                        subtitle = "Available Balance",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Individual Chitties Breakdown
            item {
                Text(
                    text = "Enrolled Chitties (${summary!!.chittySummaries.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (summary!!.chittySummaries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("This member has not been enrolled in any chitty yet.")
                        }
                    }
                }
            } else {
                items(summary!!.chittySummaries) { chittySummary ->
                    MemberChittyCard(chittySummary = chittySummary)
                }
            }
        }
    }
}

@Composable
fun MemberChittyCard(chittySummary: MemberChittySummary) {
    val chitty = chittySummary.chitty

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = chitty.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Share #${chittySummary.shareNumber} | Duration: ${chitty.durationMonths} Mos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (chittySummary.hasReceived) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Won M${chittySummary.receivedMonth}",
                                color = Color(0xFFB45309),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Current Month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${chittySummary.currentMonthDue.toLong()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OrangeWarning))
                }

                Column {
                    Text(text = "Old Pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "₹${chittySummary.previousOutstanding.toLong()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (chittySummary.previousOutstanding > 0) RedAlert else GreenSuccess
                        )
                    )
                }

                Column {
                    Text(text = "Total Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "₹${chittySummary.totalDue.toLong()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (chittySummary.totalDue > 0) RedAlert else GreenSuccess
                        )
                    )
                }

                Column {
                    Text(text = "Duration Left", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${chittySummary.totalOutstanding.toLong()}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }

            if (chittySummary.hasReceived && chittySummary.receivedAmount != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏆 Payout Received: ₹${chittySummary.receivedAmount!!.toLong()} in Month ${chittySummary.receivedMonth}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = GreenSuccess)
                )
            }

            if (chittySummary.advanceAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💰 Advance Credit Recorded: ₹${chittySummary.advanceAmount.toLong()}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = TealAccent)
                )
            }

            // Installment status breakdown
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Recent Installments:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))

            chittySummary.installments.take(4).forEach { inst ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = inst.monthLabel, style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Due: ₹${inst.dueAmount.toLong()} | Paid: ₹${inst.paidAmount.toLong()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(status = inst.status)
                    }
                }
            }
        }
    }
}
