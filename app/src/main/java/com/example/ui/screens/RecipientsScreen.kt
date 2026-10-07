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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.ChittyEntity
import com.example.data.local.entity.MemberEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecipientsScreen(viewModel: MainViewModel) {
    val recipients by viewModel.recipients.collectAsState()
    val allChitties by viewModel.chitties.collectAsState()
    val allMembers by viewModel.members.collectAsState()

    var isRecordDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isRecordDialogOpen = true },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("record_recipient_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Record Recipient")
            }
        }
    ) { padding ->
        if (recipients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFFEF3C7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFFD97706)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Chitty Recipients Recorded Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Record which member receives the chitty prize money for each month. The system prevents duplicate wins in the same chitty.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { isRecordDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Record Recipient")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("recipients_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recipients) { r ->
                    val chitty = allChitties.firstOrNull { it.id == r.chittyId }
                    val member = allMembers.firstOrNull { it.id == r.memberId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recipient_card_${r.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFFFEF3C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member?.name ?: "Member",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${chitty?.name ?: "Chitty"} • Month ${r.monthNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Payout Date: ${r.receivedDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${r.amountReceived.toLong()}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GreenSuccess
                                    )
                                )
                                if (r.notes.isNotBlank()) {
                                    Text(
                                        text = r.notes,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isRecordDialogOpen) {
            RecordRecipientDialog(
                viewModel = viewModel,
                onDismiss = { isRecordDialogOpen = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordRecipientDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val chitties by viewModel.chitties.collectAsState()
    val allMembers by viewModel.members.collectAsState()

    var selectedChittyId by remember { mutableStateOf(chitties.firstOrNull()?.id) }
    var enrolledMembers by remember { mutableStateOf<List<MemberEntity>>(emptyList()) }
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var monthNumberStr by remember { mutableStateOf("1") }
    var amountReceivedStr by remember { mutableStateOf("") }
    var receivedDate by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
    }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Load enrolled members when chitty changes
    LaunchedEffect(selectedChittyId) {
        val cid = selectedChittyId
        if (cid != null) {
            val cmList = viewModel.repository.getChittyMembers(cid)
            cmList.collect { list ->
                val memberIds = list.map { it.memberId }
                val membersInChitty = allMembers.filter { memberIds.contains(it.id) }
                enrolledMembers = membersInChitty
                selectedMemberId = membersInChitty.firstOrNull()?.id

                val currentChitty = chitties.firstOrNull { it.id == cid }
                if (currentChitty != null && amountReceivedStr.isBlank()) {
                    amountReceivedStr = currentChitty.totalAmount.toLong().toString()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(6.dp)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Record Chitty Recipient",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Chitty Dropdown
                Text(text = "Select Chitty:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))

                var chittyExpanded by remember { mutableStateOf(false) }
                val currentChitty = chitties.firstOrNull { it.id == selectedChittyId }

                ExposedDropdownMenuBox(
                    expanded = chittyExpanded,
                    onExpandedChange = { chittyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentChitty?.name ?: "Select Chitty",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chittyExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("recipient_chitty_dropdown"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = chittyExpanded,
                        onDismissRequest = { chittyExpanded = false }
                    ) {
                        chitties.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedChittyId = c.id
                                    chittyExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Member Dropdown (Enrolled in this Chitty)
                Text(text = "Recipient Member:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))

                if (enrolledMembers.isEmpty()) {
                    Text(
                        text = "No members enrolled in this chitty yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    var memberExpanded by remember { mutableStateOf(false) }
                    val currentMember = enrolledMembers.firstOrNull { it.id == selectedMemberId }

                    ExposedDropdownMenuBox(
                        expanded = memberExpanded,
                        onExpandedChange = { memberExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currentMember?.let { "${it.name} (${it.memberCode})" } ?: "Select Member",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("recipient_member_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = memberExpanded,
                            onDismissRequest = { memberExpanded = false }
                        ) {
                            enrolledMembers.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.name} (${m.memberCode})") },
                                    onClick = {
                                        selectedMemberId = m.id
                                        memberExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = monthNumberStr,
                        onValueChange = { monthNumberStr = it },
                        label = { Text("Month #") },
                        placeholder = { Text("e.g. 1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f).testTag("recipient_month_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = amountReceivedStr,
                        onValueChange = { amountReceivedStr = it },
                        label = { Text("Amount (₹)") },
                        placeholder = { Text("e.g. 95000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f).testTag("recipient_amount_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = receivedDate,
                    onValueChange = { receivedDate = it },
                    label = { Text("Date Received") },
                    modifier = Modifier.fillMaxWidth().testTag("recipient_date_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Auction discount)") },
                    modifier = Modifier.fillMaxWidth().testTag("recipient_notes_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                            val cid = selectedChittyId
                            val mid = selectedMemberId
                            val monthNum = monthNumberStr.toIntOrNull() ?: 1
                            val amount = amountReceivedStr.toDoubleOrNull() ?: 0.0

                            if (cid == null) {
                                errorText = "Please select a chitty"
                            } else if (mid == null) {
                                errorText = "Please select an enrolled member"
                            } else if (amount <= 0) {
                                errorText = "Please enter valid amount"
                            } else {
                                viewModel.recordRecipient(
                                    chittyId = cid,
                                    monthNumber = monthNum,
                                    memberId = mid,
                                    amountReceived = amount,
                                    receivedDate = receivedDate,
                                    notes = notes,
                                    onSuccess = onDismiss,
                                    onError = { err -> errorText = err }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("confirm_recipient_button")
                    ) {
                        Text("Record Winner")
                    }
                }
            }
        }
    }
}
