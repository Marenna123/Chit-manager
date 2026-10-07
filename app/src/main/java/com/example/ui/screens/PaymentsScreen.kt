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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentsScreen(
    viewModel: MainViewModel,
    preSelectedMemberId: Long? = null,
    showRecordDialogInitially: Boolean = false,
    onCloseRecordDialog: () -> Unit = {}
) {
    val payments by viewModel.payments.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val members by viewModel.members.collectAsState()

    var isRecordDialogOpen by remember { mutableStateOf(showRecordDialogInitially || preSelectedMemberId != null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isRecordDialogOpen = true },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("record_payment_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Record Payment")
            }
        }
    ) { padding ->
        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Payments Recorded Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Record payments for single or combined chitties. Receipts will be generated automatically.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { isRecordDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Record Payment")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("payments_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(payments) { payment ->
                    val member = members.firstOrNull { it.id == payment.memberId }
                    val receipt = receipts.firstOrNull { it.paymentId == payment.id }

                    PaymentCard(
                        payment = payment,
                        memberName = member?.name ?: "Member",
                        onViewReceipt = {
                            if (receipt != null) {
                                viewModel.showReceipt(receipt)
                            }
                        }
                    )
                }
            }
        }

        if (isRecordDialogOpen) {
            RecordPaymentDialog(
                viewModel = viewModel,
                initialMemberId = preSelectedMemberId,
                onDismiss = {
                    isRecordDialogOpen = false
                    onCloseRecordDialog()
                }
            )
        }
    }
}

@Composable
fun PaymentCard(
    payment: PaymentEntity,
    memberName: String,
    onViewReceipt: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewReceipt)
            .testTag("payment_card_${payment.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = memberName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (payment.isCombined) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE0E7FF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Combined",
                                color = NavyPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${payment.receiptNumber} • ${payment.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Date: ${payment.paymentDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${payment.totalAmount.toLong()}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = GreenSuccess
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = onViewReceipt, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = "Receipt", tint = NavyPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    viewModel: MainViewModel,
    initialMemberId: Long?,
    onDismiss: () -> Unit
) {
    val members by viewModel.members.collectAsState()
    val allChitties by viewModel.chitties.collectAsState()

    var selectedMemberId by remember { mutableStateOf(initialMemberId ?: members.firstOrNull()?.id) }
    var paymentDate by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
    }
    var paymentMethod by remember { mutableStateOf("UPI") }
    val paymentMethods = listOf("Cash", "UPI", "Bank Transfer", "Cheque", "Other")

    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Chitties joined by this member
    var joinedChitties by remember { mutableStateOf<List<ChittyEntity>>(emptyList()) }
    val allocationMap = remember { mutableStateMapOf<Long, String>() }

    // Fetch joined chitties when selectedMemberId changes
    LaunchedEffect(selectedMemberId) {
        val mid = selectedMemberId
        if (mid != null) {
            val memberships = viewModel.repository.getChittyMembershipsDirect(mid)
            val list = allChitties.filter { c -> memberships.any { it.chittyId == c.id } }
            joinedChitties = list
            allocationMap.clear()
            // Default allocation to monthly installment
            list.forEach { c ->
                allocationMap[c.id] = c.monthlyInstallment.toLong().toString()
            }
        }
    }

    val totalCalculated = allocationMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }

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
                    text = "Record Payment",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Member Selection
                Text(text = "Select Member:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                var memberDropdownExpanded by remember { mutableStateOf(false) }
                val currentMember = members.firstOrNull { it.id == selectedMemberId }

                ExposedDropdownMenuBox(
                    expanded = memberDropdownExpanded,
                    onExpandedChange = { memberDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentMember?.let { "${it.name} (${it.memberCode})" } ?: "Select Member",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("payment_member_dropdown"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = memberDropdownExpanded,
                        onDismissRequest = { memberDropdownExpanded = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name} (${m.memberCode})") },
                                onClick = {
                                    selectedMemberId = m.id
                                    memberDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chitties Allocation (Handles Combined Payment for multiple chitties)
                Text(
                    text = "Allocate to Joined Chitties:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (joinedChitties.isEmpty()) {
                    Text(
                        text = "This member is not enrolled in any chitty yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    joinedChitties.forEach { chitty ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        text = chitty.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Installment: ₹${chitty.monthlyInstallment.toLong()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedTextField(
                                    value = allocationMap[chitty.id] ?: "",
                                    onValueChange = { allocationMap[chitty.id] = it },
                                    label = { Text("Amount (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .testTag("allocation_input_${chitty.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Combined Payment:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "₹${totalCalculated.toLong()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method
                Text(text = "Payment Method:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val isSelected = paymentMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { paymentMethod = method }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Notes
                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_date_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reference (Optional)") },
                    placeholder = { Text("e.g. UPI Ref # 12345") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_notes_input"),
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
                            val mid = selectedMemberId
                            if (mid == null) {
                                errorText = "Please select a member"
                                return@Button
                            }
                            if (totalCalculated <= 0) {
                                errorText = "Please enter valid payment amounts for at least one chitty"
                                return@Button
                            }

                            val allocations = allocationMap.mapNotNull { (cid, amountStr) ->
                                val amt = amountStr.toDoubleOrNull() ?: 0.0
                                if (amt > 0) Pair(cid, amt) else null
                            }

                            if (allocations.isEmpty()) {
                                errorText = "No valid allocation entered"
                                return@Button
                            }

                            viewModel.recordPayment(
                                memberId = mid,
                                totalAmount = totalCalculated,
                                paymentDate = paymentDate,
                                paymentMethod = paymentMethod,
                                notes = notes,
                                allocations = allocations
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("confirm_record_payment_button")
                    ) {
                        Text("Record & Generate Receipt")
                    }
                }
            }
        }
    }
}
