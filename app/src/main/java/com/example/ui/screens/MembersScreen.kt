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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.MemberEntity
import com.example.ui.MainViewModel
import com.example.ui.components.RecentSearchHistorySection
import com.example.ui.components.StatusBadge
import com.example.ui.theme.NavyPrimary

@Composable
fun MembersScreen(
    viewModel: MainViewModel,
    onMemberClick: (Long) -> Unit,
    showAddDialogInitially: Boolean = false,
    onCloseAddDialog: () -> Unit = {}
) {
    val members by viewModel.members.collectAsState()
    val memberSearchHistory by viewModel.memberSearchHistory.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    var isAddDialogOpen by remember { mutableStateOf(showAddDialogInitially) }
    var memberToEdit by remember { mutableStateOf<MemberEntity?>(null) }

    val filteredMembers = remember(members, searchQuery) {
        if (searchQuery.isBlank()) {
            members
        } else {
            members.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.mobileNumber.contains(searchQuery, ignoreCase = true) ||
                        it.memberCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_member_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, phone or member ID...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("member_search_clear_button")
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
                                viewModel.addMemberSearch(searchQuery.trim())
                            }
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Recent Searches History
            RecentSearchHistorySection(
                searches = memberSearchHistory,
                onSelectQuery = { selected ->
                    searchQuery = selected
                    viewModel.addMemberSearch(selected)
                },
                onRemoveQuery = { toRemove ->
                    viewModel.removeMemberSearch(toRemove)
                },
                onClearAll = {
                    viewModel.clearMemberSearchHistory()
                },
                testTagPrefix = "member_search"
            )

            if (filteredMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No Members Added Yet" else "No matching members found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No members match \"$searchQuery\"",
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
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { isAddDialogOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Text("Add First Member")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("members_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMembers) { member ->
                        MemberCard(
                            member = member,
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.addMemberSearch(searchQuery.trim())
                                }
                                onMemberClick(member.id)
                            },
                            onEdit = { memberToEdit = member },
                            onSendReminder = { viewModel.prepareWhatsAppReminder(member) }
                        )
                    }
                }
            }
        }

        if (isAddDialogOpen) {
            AddEditMemberDialog(
                member = null,
                onDismiss = {
                    isAddDialogOpen = false
                    onCloseAddDialog()
                },
                onSave = { name, mobile, address ->
                    viewModel.addMember(name, mobile, address)
                    isAddDialogOpen = false
                    onCloseAddDialog()
                }
            )
        }

        if (memberToEdit != null) {
            AddEditMemberDialog(
                member = memberToEdit,
                onDismiss = { memberToEdit = null },
                onSave = { name, mobile, address ->
                    val existing = memberToEdit
                    if (existing != null) {
                        viewModel.updateMember(existing.copy(name = name, mobileNumber = mobile, address = address))
                    }
                    memberToEdit = null
                },
                onToggleStatus = {
                    val existing = memberToEdit
                    if (existing != null) {
                        viewModel.toggleMemberStatus(existing.id)
                    }
                    memberToEdit = null
                }
            )
        }
    }
}

@Composable
fun MemberCard(
    member: MemberEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onSendReminder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("member_card_${member.id}"),
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${member.memberCode})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = member.mobileNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (member.address.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = member.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(status = member.status)

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onSendReminder,
                    modifier = Modifier.testTag("whatsapp_member_${member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp Reminder",
                        tint = Color(0xFF25D366)
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Member",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditMemberDialog(
    member: MemberEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, mobile: String, address: String) -> Unit,
    onToggleStatus: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(member?.name ?: "") }
    var mobile by remember { mutableStateOf(member?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(member?.address ?: "") }
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
                    text = if (member == null) "Add New Member" else "Edit Member (${member.memberCode})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Full Name") },
                    placeholder = { Text("e.g. Ravi Kumar") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("e.g. 9876543210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_mobile_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Locality") },
                    placeholder = { Text("e.g. Temple Road, Bengaluru") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_address_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                if (member != null && onToggleStatus != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onToggleStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (member.status == "ACTIVE") "Deactivate Member" else "Activate Member")
                    }
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
                            if (name.isBlank()) {
                                errorText = "Please enter Member Name"
                            } else if (mobile.isBlank()) {
                                errorText = "Please enter Mobile Number"
                            } else {
                                onSave(name, mobile, address)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("save_member_button")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
