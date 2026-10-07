package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.ChittyEntity
import com.example.ui.MainViewModel
import com.example.ui.components.RecentSearchHistorySection
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyPrimary

@Composable
fun ChittiesScreen(
    viewModel: MainViewModel,
    onChittyClick: (Long) -> Unit,
    showCreateDialogInitially: Boolean = false,
    onCloseCreateDialog: () -> Unit = {}
) {
    val chitties by viewModel.chitties.collectAsState()
    val chittySearchHistory by viewModel.chittySearchHistory.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    var isCreateDialogOpen by remember { mutableStateOf(showCreateDialogInitially) }

    val filteredChitties = remember(chitties, searchQuery) {
        if (searchQuery.isBlank()) {
            chitties
        } else {
            val query = searchQuery.trim()
            chitties.filter { chitty ->
                chitty.name.contains(query, ignoreCase = true) ||
                        chitty.chittyCode.contains(query, ignoreCase = true) ||
                        chitty.status.contains(query, ignoreCase = true) ||
                        chitty.monthlyInstallment.toLong().toString().contains(query) ||
                        chitty.totalAmount.toLong().toString().contains(query) ||
                        "${chitty.durationMonths}".contains(query)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreateDialogOpen = true },
                containerColor = NavyPrimary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("create_chitty_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Chitty")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (chitties.isNotEmpty()) {
                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, code, status, or amount...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Chitties")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.testTag("chitty_search_clear_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search text"
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.addChittySearch(searchQuery.trim())
                                }
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chitty_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Recent Searches History
                RecentSearchHistorySection(
                    searches = chittySearchHistory,
                    onSelectQuery = { selected ->
                        searchQuery = selected
                        viewModel.addChittySearch(selected)
                    },
                    onRemoveQuery = { toRemove ->
                        viewModel.removeChittySearch(toRemove)
                    },
                    onClearAll = {
                        viewModel.clearChittySearchHistory()
                    },
                    testTagPrefix = "chitty_search"
                )
            }

            if (chitties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Chitties Created Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first chitty scheme to start adding members and managing installments.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { isCreateDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Chitty Scheme")
                        }
                    }
                }
            } else if (filteredChitties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching chitties found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No schemes match \"$searchQuery\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { searchQuery = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text("Clear Search")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chitties_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredChitties) { chitty ->
                        ChittyCard(
                            chitty = chitty,
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.addChittySearch(searchQuery.trim())
                                }
                                onChittyClick(chitty.id)
                            }
                        )
                    }
                }
            }
        }

        if (isCreateDialogOpen) {
            CreateChittyDialog(
                onDismiss = {
                    isCreateDialogOpen = false
                    onCloseCreateDialog()
                },
                onConfirm = { name, total, duration, installment, startDate, dueDay, membersCount ->
                    viewModel.createChitty(
                        name = name,
                        totalAmount = total,
                        durationMonths = duration,
                        monthlyInstallment = installment,
                        startDate = startDate,
                        dueDayOfMonth = dueDay,
                        totalMembers = membersCount
                    )
                    isCreateDialogOpen = false
                    onCloseCreateDialog()
                }
            )
        }
    }
}

@Composable
fun ChittyCard(
    chitty: ChittyEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chitty_card_${chitty.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
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
                        text = chitty.chittyCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = chitty.status)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider()
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${chitty.totalAmount.toLong()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenSuccess)
                    )
                }

                Column {
                    Text(
                        text = "Monthly Installment",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${chitty.monthlyInstallment.toLong()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Column {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${chitty.durationMonths} Mos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${chitty.totalMembers} Shares",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${chitty.dueDayOfMonth}th of month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CreateChittyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, total: Double, duration: Int, installment: Double, startDate: String, dueDay: Int, members: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var totalAmountStr by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("20") }
    var installmentStr by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("2026-10-01") }
    var dueDayStr by remember { mutableStateOf("10") }
    var membersStr by remember { mutableStateOf("20") }
    var errorText by remember { mutableStateOf<String?>(null) }

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
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create New Chitty",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Chitty Name") },
                    placeholder = { Text("e.g. ₹1,00,000 Chitty") },
                    modifier = Modifier.fillMaxWidth().testTag("chitty_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = totalAmountStr,
                    onValueChange = { totalAmountStr = it },
                    label = { Text("Total Chitty Amount (₹)") },
                    placeholder = { Text("e.g. 100000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("chitty_total_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Duration (Months)") },
                        placeholder = { Text("e.g. 12, 15, 20") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("chitty_duration_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = membersStr,
                        onValueChange = { membersStr = it },
                        label = { Text("Total Shares") },
                        placeholder = { Text("e.g. 20") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("chitty_shares_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Monthly Installment (Must be manually entered, not auto-calculated)
                OutlinedTextField(
                    value = installmentStr,
                    onValueChange = { installmentStr = it },
                    label = { Text("Monthly Installment (₹)") },
                    placeholder = { Text("Admin enters installment amount") },
                    supportingText = { Text("Admin sets custom monthly installment") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("chitty_installment_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1.2f).testTag("chitty_startdate_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = dueDayStr,
                        onValueChange = { dueDayStr = it },
                        label = { Text("Due Day") },
                        placeholder = { Text("1-31") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f).testTag("chitty_dueday_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

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
                            val total = totalAmountStr.toDoubleOrNull() ?: 0.0
                            val dur = durationStr.toIntOrNull() ?: 0
                            val inst = installmentStr.toDoubleOrNull() ?: 0.0
                            val dueDay = dueDayStr.toIntOrNull() ?: 10
                            val members = membersStr.toIntOrNull() ?: 20

                            if (name.isBlank()) {
                                errorText = "Please enter Chitty Name"
                            } else if (total <= 0) {
                                errorText = "Please enter valid Total Amount"
                            } else if (dur <= 0) {
                                errorText = "Please enter valid Duration in months"
                            } else if (inst <= 0) {
                                errorText = "Please enter valid Monthly Installment"
                            } else {
                                onConfirm(name, total, dur, inst, startDate, dueDay, members)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("chitty_save_button")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
