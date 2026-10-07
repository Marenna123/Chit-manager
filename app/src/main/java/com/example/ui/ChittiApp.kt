package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.WhatsAppPreviewDialog
import com.example.ui.screens.ChittiesScreen
import com.example.ui.screens.ChittyDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MemberDetailScreen
import com.example.ui.screens.MembersScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.RecipientsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealAccent
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    CHITTIES("Chitties", Icons.Default.MonetizationOn),
    MEMBERS("Members", Icons.Default.Group),
    PAYMENTS("Payments", Icons.Default.Payment),
    RECIPIENTS("Recipients", Icons.Default.EmojiEvents),
    REPORTS("Reports", Icons.Default.Assessment),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChittiApp(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val admin by viewModel.admin.collectAsState()

    // Dialogs / Sheet States
    val whatsAppPreview by viewModel.whatsAppPreview.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    // Check if first-time onboarding is needed
    val isSetupComplete = settings?.isFirstTimeSetupCompleted == true || admin != null

    if (!isSetupComplete) {
        SetupScreen(viewModel = viewModel)
        return
    }

    // Main navigation states
    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var viewingChittyId by remember { mutableStateOf<Long?>(null) }
    var viewingMemberId by remember { mutableStateOf<Long?>(null) }
    var preselectedPaymentMemberId by remember { mutableStateOf<Long?>(null) }

    // Shortcuts from Dashboard
    var openCreateChittyDialog by remember { mutableStateOf(false) }
    var openAddMemberDialog by remember { mutableStateOf(false) }
    var openRecordPaymentDialog by remember { mutableStateOf(false) }

    // Bottom Sheet for extra menu options
    var isMoreMenuOpen by remember { mutableStateOf(false) }

    // Handle back button when on sub-screens
    if (viewingChittyId != null) {
        BackHandler { viewingChittyId = null }
        ChittyDetailScreen(
            chittyId = viewingChittyId!!,
            viewModel = viewModel,
            onBack = { viewingChittyId = null },
            onMemberClick = { memberId -> viewingMemberId = memberId }
        )
        return
    }

    if (viewingMemberId != null) {
        BackHandler { viewingMemberId = null }
        MemberDetailScreen(
            memberId = viewingMemberId!!,
            viewModel = viewModel,
            onBack = { viewingMemberId = null },
            onRecordPayment = { memberId ->
                preselectedPaymentMemberId = memberId
                currentTab = ScreenTab.PAYMENTS
                viewingMemberId = null
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyPrimary)
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.White, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_cheeti_logo),
                                contentDescription = "Cheeti Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("cheeti_logo_drawer"),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cheeti",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        val adminName = admin?.name ?: settings?.adminName ?: "Management"
                        Text(
                            text = "Admin: $adminName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ScreenTab.values().forEach { tab ->
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                        label = { Text(tab.title, fontWeight = FontWeight.SemiBold) },
                        selected = currentTab == tab,
                        onClick = {
                            currentTab = tab
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NavyPrimary,
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_cheeti_logo),
                                    contentDescription = "Cheeti Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("cheeti_logo_topbar"),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = currentTab.title,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("nav_drawer_button")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        val adminName = admin?.name ?: settings?.adminName ?: ""
                        if (adminName.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = adminName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavyPrimary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                val isImeVisible = WindowInsets.isImeVisible
                if (!isImeVisible) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val primaryTabs = listOf(
                                ScreenTab.DASHBOARD,
                                ScreenTab.CHITTIES,
                                ScreenTab.MEMBERS,
                                ScreenTab.PAYMENTS
                            )

                            primaryTabs.forEach { tab ->
                                val isSelected = currentTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) NavyPrimary else Color.Transparent)
                                        .clickable { currentTab = tab }
                                        .testTag("tab_${tab.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            tint = if (isSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tab.title,
                                            color = if (isSelected) Color.White else Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // More Tab (Recipients, Reports, Settings)
                            val isMoreActive = currentTab == ScreenTab.RECIPIENTS ||
                                    currentTab == ScreenTab.REPORTS ||
                                    currentTab == ScreenTab.SETTINGS

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isMoreActive) NavyPrimary else Color.Transparent)
                                    .clickable { isMoreMenuOpen = true }
                                    .testTag("tab_more"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "More",
                                        tint = if (isMoreActive) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "More",
                                        color = if (isMoreActive) Color.White else Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontWeight = if (isMoreActive) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentTab) {
                    ScreenTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToChitties = { currentTab = ScreenTab.CHITTIES },
                        onNavigateToMembers = { currentTab = ScreenTab.MEMBERS },
                        onNavigateToPayments = { currentTab = ScreenTab.PAYMENTS },
                        onNavigateToReports = { currentTab = ScreenTab.REPORTS },
                        onOpenCreateChitty = {
                            currentTab = ScreenTab.CHITTIES
                            openCreateChittyDialog = true
                        },
                        onOpenAddMember = {
                            currentTab = ScreenTab.MEMBERS
                            openAddMemberDialog = true
                        },
                        onOpenRecordPayment = {
                            currentTab = ScreenTab.PAYMENTS
                            openRecordPaymentDialog = true
                        }
                    )
                    ScreenTab.CHITTIES -> ChittiesScreen(
                        viewModel = viewModel,
                        onChittyClick = { id -> viewingChittyId = id },
                        showCreateDialogInitially = openCreateChittyDialog,
                        onCloseCreateDialog = { openCreateChittyDialog = false }
                    )
                    ScreenTab.MEMBERS -> MembersScreen(
                        viewModel = viewModel,
                        onMemberClick = { id -> viewingMemberId = id },
                        showAddDialogInitially = openAddMemberDialog,
                        onCloseAddDialog = { openAddMemberDialog = false }
                    )
                    ScreenTab.PAYMENTS -> PaymentsScreen(
                        viewModel = viewModel,
                        preSelectedMemberId = preselectedPaymentMemberId,
                        showRecordDialogInitially = openRecordPaymentDialog,
                        onCloseRecordDialog = {
                            openRecordPaymentDialog = false
                            preselectedPaymentMemberId = null
                        }
                    )
                    ScreenTab.RECIPIENTS -> RecipientsScreen(viewModel = viewModel)
                    ScreenTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }

            // More Bottom Sheet for secondary tabs
            if (isMoreMenuOpen) {
                ModalBottomSheet(
                    onDismissRequest = { isMoreMenuOpen = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Additional Management Options",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val secondaryTabs = listOf(
                            ScreenTab.RECIPIENTS,
                            ScreenTab.REPORTS,
                            ScreenTab.SETTINGS
                        )

                        secondaryTabs.forEach { tab ->
                            NavigationDrawerItem(
                                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                                label = { Text(tab.title, fontWeight = FontWeight.SemiBold) },
                                selected = currentTab == tab,
                                onClick = {
                                    currentTab = tab
                                    isMoreMenuOpen = false
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Dialogs
            if (activeReceipt != null) {
                ReceiptDialog(
                    receipt = activeReceipt!!,
                    onDismiss = { viewModel.closeReceipt() }
                )
            }

            if (whatsAppPreview != null) {
                val (member, msg) = whatsAppPreview!!
                WhatsAppPreviewDialog(
                    memberName = member.name,
                    mobileNumber = member.mobileNumber,
                    messageText = msg,
                    onDismiss = { viewModel.closeWhatsAppPreview() }
                )
            }
        }
    }
}
