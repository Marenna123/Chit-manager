package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupSummary
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalThemeController
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val admin by viewModel.admin.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val members by viewModel.members.collectAsState()
    val chitties by viewModel.chitties.collectAsState()
    val payments by viewModel.payments.collectAsState()

    var adminName by remember(admin, settings) {
        mutableStateOf(admin?.name ?: settings?.adminName ?: "")
    }
    var mobileNumber by remember(admin, settings) {
        mutableStateOf(admin?.mobileNumber ?: settings?.mobileNumber ?: "")
    }
    var reminderHeader by remember(settings) {
        mutableStateOf(settings?.reminderHeader ?: "🔔 Cheeti Payment Reminder")
    }
    var customNotes by remember(settings) {
        mutableStateOf(settings?.customNotes ?: "Please make the payment by the due date.")
    }

    // Backup & Restore state
    var exportedJsonData by remember { mutableStateOf<String?>(null) }
    var exportFilename by remember { mutableStateOf("") }
    var showExportSuccessDialog by remember { mutableStateOf(false) }

    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var pendingRestoreSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var showPasteJsonDialog by remember { mutableStateOf(false) }
    var pastedJsonText by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }

    // File Create Launcher (Export)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && exportedJsonData != null) {
            try {
                BackupManager.writeJsonToUri(context, uri, exportedJsonData!!)
                Toast.makeText(context, "Backup file saved successfully!", Toast.LENGTH_LONG).show()
                showExportSuccessDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // File Open Launcher (Restore)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = BackupManager.readJsonFromUri(context, uri)
                val result = viewModel.inspectBackup(jsonString)
                if (result.isSuccess) {
                    pendingRestoreJson = jsonString
                    pendingRestoreSummary = result.getOrNull()
                    showConfirmRestoreDialog = true
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Invalid backup file"
                    Toast.makeText(context, "Cannot read backup: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Admin & Management Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "The Admin name set here is dynamically inserted into all WhatsApp reminders and customer receipts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance & Theme Mode Card
        item {
            val themeController = LocalThemeController.current
            val currentThemeMode = themeController.currentMode

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (currentThemeMode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    else -> Icons.Default.BrightnessAuto
                                },
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Theme & Appearance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Global theme state across all screens",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Dark Mode Switch Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                themeController.toggleLightDark()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (themeController.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (themeController.isDark) Color(0xFF86EFAC) else NavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dark Mode Toggle",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = if (themeController.isDark) "Dark palette is currently active" else "Light palette is currently active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = themeController.isDark,
                            onCheckedChange = { isChecked ->
                                val newMode = if (isChecked) ThemeMode.DARK else ThemeMode.LIGHT
                                themeController.onModeChanged(newMode)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NavyPrimary
                            ),
                            modifier = Modifier.testTag("dark_mode_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Display Mode Selection",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(
                            Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                            Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
                            Triple(ThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto)
                        )

                        options.forEach { (mode, label, icon) ->
                            val isSelected = currentThemeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        themeController.onModeChanged(mode)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("theme_mode_${mode.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(NavyPrimary, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live active palette indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "App Palette:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (themeController.isDark) "Dark Palette Active" else "Light Palette Active",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Profile Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Owner / Administrator Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Admin / Management Name") },
                        placeholder = { Text("e.g. Gopal Krishna") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_admin_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("e.g. 9876543210") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_mobile_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateSettings(adminName, mobileNumber, reminderHeader, customNotes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_settings_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Profile Changes")
                    }
                }
            }
        }

        // WhatsApp Reminder Settings & Live Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp Message Customization",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = reminderHeader,
                        onValueChange = { reminderHeader = it },
                        label = { Text("Message Header") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        label = { Text("Closing Message / Due Date Note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Live WhatsApp Message Preview:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Live WhatsApp preview bubble showing dynamic admin name
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE7FCE9), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF25D366).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "$reminderHeader\n\nDear Ravi,\n\nYour payments for this month:\n\n• ₹1,00,000 Chitty — ₹5,000\n• ₹50,000 Chitty — ₹3,000\n\nCurrent Month Due: ₹8,000\nPrevious Outstanding: ₹0\nTotal Amount Due: ₹8,000\n\n$customNotes\n\nThank you\n${adminName.ifBlank { "Management" }}",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.updateSettings(adminName, mobileNumber, reminderHeader, customNotes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update Message Template")
                    }
                }
            }
        }

        // Data Security: Backup & Restore Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backup_restore_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Database Backup & Restore",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Export data as JSON or import backup file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Database Metrics Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Current Database Status",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Members: ${members.size} registered\n• Chitties: ${chitties.size} groups\n• Payments recorded: ${payments.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Export Backup (Save to Device / Cloud)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val json = viewModel.exportDatabaseBackup()
                                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        exportFilename = "cheeti_backup_$timestamp.json"
                                        exportedJsonData = json
                                        createDocumentLauncher.launch(exportFilename)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_backup_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val json = viewModel.exportDatabaseBackup()
                                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        exportFilename = "cheeti_backup_$timestamp.json"
                                        exportedJsonData = json
                                        BackupManager.shareBackup(context, json, exportFilename)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_backup_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Backup")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Restore Database (Import Backup)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Restore previously exported JSON backup file to retrieve all members, chitties, and payments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_backup_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON File")
                        }

                        OutlinedButton(
                            onClick = {
                                pastedJsonText = ""
                                showPasteJsonDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("paste_backup_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste JSON Text")
                        }
                    }
                }
            }
        }

        // Test Data Seeding Section (Requirement 17)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TealAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Requirement 17 Test Scenario",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Seed the required test dataset:\n• Admin: Gopal Krishna\n• Chitty 1: ₹1,00,000 (20 mos, ₹5,000/mo) - Paid ₹5,000\n• Chitty 2: ₹50,000 (12 mos, ₹3,000/mo) - Not Paid\n• Member: Ravi Kumar\n• Resulting Dashboard: Due = ₹8,000, Paid = ₹5,000, Outstanding = ₹3,000",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { viewModel.seedSampleTestData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seed_test_data_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Test Data (Gopal Krishna)")
                    }
                }
            }
        }
    }

    // Confirmation Restore Dialog
    if (showConfirmRestoreDialog && pendingRestoreSummary != null && pendingRestoreJson != null) {
        val summary = pendingRestoreSummary!!
        AlertDialog(
            onDismissRequest = {
                if (!isRestoring) {
                    showConfirmRestoreDialog = false
                    pendingRestoreJson = null
                    pendingRestoreSummary = null
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Restore Database Backup?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Backup file verified successfully:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• Exported on: ${summary.exportDate}", style = MaterialTheme.typography.bodySmall)
                            Text("• Members: ${summary.membersCount}", style = MaterialTheme.typography.bodySmall)
                            Text("• Chitties: ${summary.chittiesCount}", style = MaterialTheme.typography.bodySmall)
                            Text("• Installments: ${summary.installmentsCount}", style = MaterialTheme.typography.bodySmall)
                            Text("• Payments recorded: ${summary.paymentsCount}", style = MaterialTheme.typography.bodySmall)
                            Text("• Receipts: ${summary.receiptsCount}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚠️ Warning: This will overwrite your current local database with the data from this backup file. This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDC2626)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoring = true
                        viewModel.restoreDatabase(pendingRestoreJson!!) { success, msg ->
                            isRestoring = false
                            showConfirmRestoreDialog = false
                            pendingRestoreJson = null
                            pendingRestoreSummary = null
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = !isRestoring,
                    modifier = Modifier.testTag("confirm_restore_button")
                ) {
                    Text(if (isRestoring) "Restoring..." else "Confirm & Restore")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmRestoreDialog = false
                        pendingRestoreJson = null
                        pendingRestoreSummary = null
                    },
                    enabled = !isRestoring
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Paste JSON Restore Dialog
    if (showPasteJsonDialog) {
        AlertDialog(
            onDismissRequest = { showPasteJsonDialog = false },
            title = { Text("Paste JSON Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Paste the raw Cheeti JSON backup text below:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pastedJsonText,
                        onValueChange = { pastedJsonText = it },
                        placeholder = { Text("{\"app\":\"Cheeti\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = viewModel.inspectBackup(pastedJsonText)
                        if (result.isSuccess) {
                            pendingRestoreJson = pastedJsonText
                            pendingRestoreSummary = result.getOrNull()
                            showPasteJsonDialog = false
                            showConfirmRestoreDialog = true
                        } else {
                            Toast.makeText(context, "Invalid JSON: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Inspect & Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPasteJsonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Success Dialog
    if (showExportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            icon = {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(32.dp))
            },
            title = { Text("Backup Created", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your database backup ($exportFilename) has been saved securely.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can also share it to Google Drive, WhatsApp, or copy the JSON to clipboard for safekeeping.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportSuccessDialog = false
                        exportedJsonData?.let {
                            BackupManager.shareBackup(context, it, exportFilename)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Backup")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        exportedJsonData?.let {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        showExportSuccessDialog = false
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy JSON")
                }
            }
        )
    }
}
