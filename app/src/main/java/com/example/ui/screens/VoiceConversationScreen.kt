package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.LiveSessionState
import com.example.ui.MainViewModel
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConversationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionState by viewModel.liveSessionState.collectAsState()
    val messages by viewModel.liveVoiceMessages.collectAsState()
    val audioWaveLevel by viewModel.liveAudioWaveLevel.collectAsState()
    val currentAssistantText by viewModel.liveCurrentAssistantText.collectAsState()
    val errorMessage by viewModel.liveErrorMessage.collectAsState()
    val isMuted by viewModel.isLiveMuted.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.startLiveVoiceSession()
        }
    }

    // Auto-scroll when messages update
    LaunchedEffect(messages.size, currentAssistantText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Clean up or auto-start
    DisposableEffect(Unit) {
        if (hasMicPermission) {
            viewModel.startLiveVoiceSession()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        onDispose {
            viewModel.endLiveVoiceSession()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cheeti Voice Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Gemini Live Audio • Real-Time AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleVoiceMute() },
                        modifier = Modifier.testTag("voice_mute_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White
                        )
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
                .background(MaterialTheme.colorScheme.background)
                .testTag("voice_conversation_screen")
        ) {
            // Live Status Banner
            LiveStatusHeader(
                sessionState = sessionState,
                hasMicPermission = hasMicPermission,
                onRequestMic = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )

            // Animated Voice Orb Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                LiveVoiceOrb(
                    sessionState = sessionState,
                    audioLevel = audioWaveLevel,
                    isMuted = isMuted,
                    onClick = {
                        if (sessionState == LiveSessionState.IDLE || sessionState == LiveSessionState.ERROR) {
                            if (hasMicPermission) {
                                viewModel.startLiveVoiceSession()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            viewModel.toggleVoiceMute()
                        }
                    }
                )
            }

            // Real-Time Assistant Speech Preview
            if (currentAssistantText.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentAssistantText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Error Display if any
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Conversation Chat List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start speaking to Cheeti AI",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Ask about dues, collections, members, or chitty rules",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    items(messages) { msg ->
                        VoiceMessageBubble(message = msg)
                    }
                }
            }

            // Suggested Voice Prompt Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Who owes money this month?",
                    "Total collection expected?",
                    "How does auction dividend work?",
                    "List my active chitties",
                    "Ravi's pending balance"
                )
                items(suggestions) { query ->
                    Surface(
                        onClick = { viewModel.sendVoiceTextPrompt(query) },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = query,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom Action Bar: Text input and Voice Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Speak or type to Cheeti AI...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        trailingIcon = {
                            if (textInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.sendVoiceTextPrompt(textInput)
                                        textInput = ""
                                    },
                                    modifier = Modifier.testTag("voice_send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = NavyPrimary
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // End Session Button
                    if (sessionState != LiveSessionState.IDLE) {
                        IconButton(
                            onClick = { viewModel.endLiveVoiceSession() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(RedAlert.copy(alpha = 0.1f), CircleShape)
                                .testTag("voice_end_session")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Live Session",
                                tint = RedAlert
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = {
                                if (hasMicPermission) {
                                    viewModel.startLiveVoiceSession()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            containerColor = NavyPrimary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("voice_start_session"),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = "Start Voice Session")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveStatusHeader(
    sessionState: LiveSessionState,
    hasMicPermission: Boolean,
    onRequestMic: () -> Unit
) {
    val (statusText, statusColor) = when (sessionState) {
        LiveSessionState.IDLE -> Pair("Tap Mic to Start Voice Conversation", Color(0xFF64748B))
        LiveSessionState.CONNECTING -> Pair("Connecting to gemini-3.8-live...", Color(0xFFD97706))
        LiveSessionState.CONNECTED -> Pair("Connected to Live API", Color(0xFF2563EB))
        LiveSessionState.LISTENING -> Pair("Listening... Speak now", GreenSuccess)
        LiveSessionState.PROCESSING -> Pair("Gemini is thinking...", Color(0xFF7C3AED))
        LiveSessionState.SPEAKING -> Pair("Gemini is speaking...", TealAccent)
        LiveSessionState.ERROR -> Pair("Connection Offline", RedAlert)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(statusColor.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = statusColor
            )
        }

        if (!hasMicPermission) {
            Button(
                onClick = onRequestMic,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Grant Mic", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LiveVoiceOrb(
    sessionState: LiveSessionState,
    audioLevel: Float,
    isMuted: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val orbScale = when (sessionState) {
        LiveSessionState.LISTENING -> (1.0f + audioLevel * 0.4f).coerceIn(0.9f, 1.4f)
        LiveSessionState.SPEAKING -> idlePulse * 1.15f
        LiveSessionState.PROCESSING -> idlePulse * 1.08f
        LiveSessionState.CONNECTING -> idlePulse
        else -> 1.0f
    }

    val orbColors = when (sessionState) {
        LiveSessionState.LISTENING -> listOf(GreenSuccess, Color(0xFF10B981), Color(0xFF059669))
        LiveSessionState.SPEAKING -> listOf(TealAccent, Color(0xFF06B6D4), NavyPrimary)
        LiveSessionState.PROCESSING -> listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF4F46E5))
        LiveSessionState.CONNECTING -> listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
        LiveSessionState.ERROR -> listOf(RedAlert, Color(0xFFDC2626), Color(0xFFB91C1C))
        else -> listOf(NavyPrimary, Color(0xFF3B82F6), Color(0xFF1D4ED8))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp)
    ) {
        // Outer glowing halo
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(orbScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(orbColors.first().copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Middle ring
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(orbScale * 0.95f)
                .background(
                    Brush.linearGradient(orbColors),
                    shape = CircleShape
                )
                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )

        // Inner core button
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(70.dp)
                .background(Color.White, CircleShape)
                .testTag("voice_orb_center")
        ) {
            Icon(
                imageVector = when {
                    isMuted -> Icons.Default.MicOff
                    sessionState == LiveSessionState.SPEAKING -> Icons.Default.GraphicEq
                    sessionState == LiveSessionState.LISTENING -> Icons.Default.Mic
                    else -> Icons.Default.AutoAwesome
                },
                contentDescription = "Voice State",
                tint = orbColors.first(),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun VoiceMessageBubble(message: com.example.service.VoiceMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.RecordVoiceOver else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isUser) Color.White.copy(alpha = 0.8f) else NavyPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUser) "You" else "Cheeti AI (Live)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isUser) Color.White.copy(alpha = 0.8f) else NavyPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
