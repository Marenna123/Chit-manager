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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.ChittyMemberEntity
import com.example.data.local.entity.MemberEntity
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChittyDetailScreen(
    chittyId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onMemberClick: (Long) -> Unit
) {
    val chitties by viewModel.chitties.collectAsState()
    val allMembers by viewModel.members.collectAsState()
    val chitty = chitties.firstOrNull { it.id == chittyId }

    val chittyMembersFlow = remember(chittyId) { viewModel.repository.getChittyMembers(chittyId) }
    val chittyMembers by chittyMembersFlow.collectAsState(initial = emptyList())

    val installmentsFlow = remember(chittyId) { viewModel.repository.getInstallmentsForChitty(chittyId) }
    val installments by installmentsFlow.collectAsState(initial = emptyList())

    val recipientsFlow = remember(chittyId) { viewModel.repository.getRecipientsForChitty(chittyId) }
    val recipients by recipientsFlow.collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isEnrollDialogOpen by remember { mutableStateOf(false) }

    if (chitty == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chitty not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chitty.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chitty Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                                text = "Total Value: ₹${chitty.totalAmount.toLong()}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = GreenSuccess)
                            )
                            Text(
                                text = "Monthly Installment: ₹${chitty.monthlyInstallment.toLong()}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        StatusBadge(status = chitty.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Duration: ${chitty.durationMonths} Months",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Due Day: ${chitty.dueDayOfMonth}th",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Enrolled: ${chittyMembers.size}/${chitty.totalMembers}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    selectedContentColor = NavyPrimary,
                    unselectedContentColor = Color(0xFF64748B),
                    text = { Text("Members (${chittyMembers.size})", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    selectedContentColor = NavyPrimary,
                    unselectedContentColor = Color(0xFF64748B),
                    text = { Text("Installments Ledger", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    selectedContentColor = NavyPrimary,
                    unselectedContentColor = Color(0xFF64748B),
                    text = { Text("Recipients (${recipients.size})", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // Members Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enrolled Members",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = { isEnrollDialogOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("enroll_member_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enroll Member")
                            }
                        }

                        if (chittyMembers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No members joined this chitty yet. Tap 'Enroll Member' to add.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(chittyMembers) { cm ->
                                    val member = allMembers.firstOrNull { it.id == cm.memberId }
                                    if (member != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = "Phone: ${member.mobileNumber}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "Share: #${cm.shareNumber} | Joined: ${cm.joinedDate}",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { viewModel.removeMemberFromChitty(chitty.id, member.id) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove",
                                                        tint = MaterialTheme.colorScheme.error
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
                1 -> {
                    // Installments Ledger Tab
                    if (installments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No installment schedule generated yet. Enroll members to generate schedules.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(installments) { inst ->
                                val member = allMembers.firstOrNull { it.id == inst.memberId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${inst.monthLabel} - ${member?.name ?: "Member"}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Due: ${inst.dueDate}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Due: ₹${inst.dueAmount.toLong()} | Paid: ₹${inst.paidAmount.toLong()} | Pending: ₹${inst.outstandingAmount.toLong()}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        StatusBadge(status = inst.status)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Recipients Tab
                    if (recipients.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No recipients recorded yet for this chitty.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recipients) { r ->
                                val member = allMembers.firstOrNull { it.id == r.memberId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color(0xFFFEF3C7), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "Month ${r.monthNumber}: ${member?.name ?: "Member"}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Amount Received: ₹${r.amountReceived.toLong()}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = GreenSuccess,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Text(
                                                text = "Date: ${r.receivedDate}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // Enroll Member Dialog
        if (isEnrollDialogOpen) {
            EnrollMemberDialog(
                allMembers = allMembers,
                alreadyEnrolledIds = chittyMembers.map { it.memberId },
                onDismiss = { isEnrollDialogOpen = false },
                onEnroll = { memberId, shareNum ->
                    viewModel.addMemberToChitty(chitty.id, memberId, shareNum)
                    isEnrollDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun EnrollMemberDialog(
    allMembers: List<MemberEntity>,
    alreadyEnrolledIds: List<Long>,
    onDismiss: () -> Unit,
    onEnroll: (memberId: Long, shareNumber: Int) -> Unit
) {
    val availableMembers = allMembers.filter { it.status == "ACTIVE" && !alreadyEnrolledIds.contains(it.id) }
    var selectedMemberId by remember { mutableStateOf(availableMembers.firstOrNull()?.id) }
    var shareNumStr by remember { mutableStateOf("1") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(8.dp)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "Enroll Member in Chitty",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (availableMembers.isEmpty()) {
                    Text(
                        text = "All active members are already enrolled or no active members available.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Select Member:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableMembers) { m ->
                            val isSelected = m.id == selectedMemberId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("select_member_${m.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                onClick = { selectedMemberId = m.id }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${m.name} (${m.memberCode})",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val id = selectedMemberId
                            if (id != null) {
                                onEnroll(id, shareNumStr.toIntOrNull() ?: 1)
                            }
                        },
                        enabled = selectedMemberId != null,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("confirm_enroll_button")
                    ) {
                        Text("Enroll")
                    }
                }
            }
        }
    }
}
