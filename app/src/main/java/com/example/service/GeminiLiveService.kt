package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class LiveSessionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class VoiceMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "gemini"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiLiveService(private val context: Context) {

    private val tag = "GeminiLiveService"
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // State flows for UI observation
    private val _sessionState = MutableStateFlow(LiveSessionState.IDLE)
    val sessionState: StateFlow<LiveSessionState> = _sessionState.asStateFlow()

    private val _messages = MutableStateFlow<List<VoiceMessage>>(emptyList())
    val messages: StateFlow<List<VoiceMessage>> = _messages.asStateFlow()

    private val _currentAssistantText = MutableStateFlow("")
    val currentAssistantText: StateFlow<String> = _currentAssistantText.asStateFlow()

    private val _currentUserText = MutableStateFlow("")
    val currentUserText: StateFlow<String> = _currentUserText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _audioWaveLevel = MutableStateFlow(0f)
    val audioWaveLevel: StateFlow<Float> = _audioWaveLevel.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Networking
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    // Audio Hardware
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private var isRecording = false

    // TTS Fallback
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.ENGLISH
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize TTS: ${e.message}")
        }
    }

    /**
     * Connect to gemini-3.8-live via WebSockets and start the bidirectional session
     */
    fun startSession(systemContext: String = "") {
        if (_sessionState.value == LiveSessionState.CONNECTED ||
            _sessionState.value == LiveSessionState.LISTENING ||
            _sessionState.value == LiveSessionState.CONNECTING
        ) {
            return
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            _sessionState.value = LiveSessionState.ERROR
            _errorMessage.value = "Gemini API key is not configured. Please add your key in the AI Studio Secrets panel."
            return
        }

        _sessionState.value = LiveSessionState.CONNECTING
        _errorMessage.value = null

        scope.launch {
            initAudioTrack()

            val liveEndpoint = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey"
            val request = Request.Builder()
                .url(liveEndpoint)
                .build()

            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    Log.d(tag, "Live API WebSocket connected successfully")
                    _sessionState.value = LiveSessionState.CONNECTED
                    sendSetupMessage(ws, systemContext)
                    startRecording()
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    handleServerMessage(text)
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    Log.e(tag, "WebSocket failure: ${t.message}")
                    scope.launch {
                        _sessionState.value = LiveSessionState.ERROR
                        _errorMessage.value = "Live connection failed: ${t.localizedMessage ?: "Network error"}. You can still chat using voice or text."
                        stopRecording()
                    }
                }

                override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                    Log.d(tag, "WebSocket closed: $reason (code: $code)")
                    _sessionState.value = LiveSessionState.IDLE
                    stopRecording()
                }
            })
        }
    }

    /**
     * Send Setup message configuring the gemini-3.8-live session
     */
    private fun sendSetupMessage(ws: WebSocket, customContext: String) {
        try {
            val baseInstructions = """
                You are the AI Voice Assistant for Cheeti (chit fund manager application).
                The user is speaking to you using real-time voice (model gemini-3.8-live).
                You can answer queries about:
                - Active and completed chitties, duration, monthly installments
                - Members enrolled, due balances, old pending payments, and total balance
                - Calculating auction dividend, winner payout rules, and collection advice
                - General guidance on running a transparent, trustworthy chit fund
                Keep all answers conversational, natural, friendly, and concise for audio speech.
                $customContext
            """.trimIndent()

            val setupPayload = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/gemini-2.5-flash-native-audio-preview-12-2025")
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply {
                            put("AUDIO")
                            put("TEXT")
                        })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", "Aoede")
                                })
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", baseInstructions)
                            })
                        })
                    })
                })
            }

            ws.send(setupPayload.toString())
            Log.d(tag, "Setup message sent to gemini-3.8-live")
        } catch (e: Exception) {
            Log.e(tag, "Failed to send setup message: ${e.message}")
        }
    }

    /**
     * Handle incoming streaming response from gemini-3.8-live
     */
    private fun handleServerMessage(jsonText: String) {
        try {
            val root = JSONObject(jsonText)

            if (root.has("serverContent")) {
                val serverContent = root.getJSONObject("serverContent")

                if (serverContent.has("modelTurn")) {
                    _sessionState.value = LiveSessionState.SPEAKING
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    val parts = modelTurn.optJSONArray("parts")

                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)

                            // Real-time audio stream (PCM 24kHz)
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val base64Audio = inlineData.optString("data")
                                if (base64Audio.isNotBlank()) {
                                    playPcmAudio(base64Audio)
                                }
                            }

                            // Text transcript stream
                            if (part.has("text")) {
                                val textChunk = part.optString("text")
                                if (textChunk.isNotBlank()) {
                                    _currentAssistantText.value += textChunk
                                }
                            }
                        }
                    }
                }

                if (serverContent.optBoolean("turnComplete", false)) {
                    val fullText = _currentAssistantText.value.trim()
                    if (fullText.isNotBlank()) {
                        addMessage("gemini", fullText)
                        _currentAssistantText.value = ""
                    }
                    _sessionState.value = LiveSessionState.LISTENING
                }

                if (serverContent.optBoolean("interrupted", false)) {
                    Log.d(tag, "User interrupted model speaking")
                    stopAudioTrackPlayback()
                    _sessionState.value = LiveSessionState.LISTENING
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing server message: ${e.message}")
        }
    }

    /**
     * Send user spoken text or prompt turn
     */
    fun sendTextPrompt(text: String) {
        if (text.isBlank()) return
        addMessage("user", text)
        _currentUserText.value = text

        val ws = webSocket
        if (ws != null && _sessionState.value != LiveSessionState.IDLE && _sessionState.value != LiveSessionState.ERROR) {
            try {
                _sessionState.value = LiveSessionState.PROCESSING
                val clientMessage = JSONObject().apply {
                    put("clientContent", JSONObject().apply {
                        put("turns", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", text)
                                    })
                                })
                            })
                        })
                        put("turnComplete", true)
                    })
                }
                ws.send(clientMessage.toString())
            } catch (e: Exception) {
                Log.e(tag, "Error sending text prompt over WebSocket: ${e.message}")
                fallbackRestRequest(text)
            }
        } else {
            fallbackRestRequest(text)
        }
    }

    /**
     * Fallback REST API generation if Live WebSocket is disconnected
     */
    private fun fallbackRestRequest(prompt: String) {
        scope.launch {
            _sessionState.value = LiveSessionState.PROCESSING
            val apiKey = BuildConfig.GEMINI_API_KEY
            try {
                val restUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val req = Request.Builder()
                    .url(restUrl)
                    .post(bodyJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val resp = okHttpClient.newCall(req).execute()
                val respStr = resp.body?.string() ?: ""
                val respJson = JSONObject(respStr)
                val reply = respJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "I heard you, but could not retrieve a response."

                withContext(Dispatchers.Main) {
                    addMessage("gemini", reply)
                    speakTts(reply)
                    _sessionState.value = LiveSessionState.LISTENING
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error: ${e.message}"
                    _sessionState.value = LiveSessionState.ERROR
                }
            }
        }
    }

    private fun speakTts(text: String) {
        if (isTtsReady && tts != null && !_isMuted.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gemini_tts")
        }
    }

    /**
     * Start recording microphone and streaming PCM 16kHz audio chunks
     */
    private fun startRecording() {
        if (isRecording) return
        isRecording = true
        _sessionState.value = LiveSessionState.LISTENING

        recordingJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBuf
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(tag, "AudioRecord failed to initialize")
                    return@launch
                }

                audioRecord?.startRecording()
                val buffer = ByteArray(2048)

                while (isActive && isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0 && !_isMuted.value) {
                        // Calculate RMS amplitude for UI audio wave
                        var sum = 0.0
                        for (i in 0 until read step 2) {
                            val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                            sum += sample * sample
                        }
                        val rms = Math.sqrt(sum / (read / 2))
                        val normalized = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                        _audioWaveLevel.value = normalized

                        // Stream audio chunk to WebSocket
                        val audioChunk = buffer.copyOf(read)
                        val base64 = Base64.encodeToString(audioChunk, Base64.NO_WRAP)
                        sendRealtimeAudio(base64)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Recording error: ${e.message}")
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                audioRecord = null
            }
        }
    }

    private fun sendRealtimeAudio(base64Data: String) {
        val ws = webSocket ?: return
        try {
            val audioPayload = JSONObject().apply {
                put("realtimeInput", JSONObject().apply {
                    put("mediaChunks", JSONArray().apply {
                        put(JSONObject().apply {
                            put("mimeType", "audio/pcm;rate=16000")
                            put("data", base64Data)
                        })
                    })
                })
            }
            ws.send(audioPayload.toString())
        } catch (_: Exception) {}
    }

    private fun stopRecording() {
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _audioWaveLevel.value = 0f
    }

    /**
     * AudioTrack for streaming PCM 24kHz audio from Gemini
     */
    private fun initAudioTrack() {
        if (audioTrack != null) return
        try {
            val sampleRate = 24000
            val channel = AudioFormat.CHANNEL_OUT_MONO
            val format = AudioFormat.ENCODING_PCM_16BIT
            val bufSize = AudioTrack.getMinBufferSize(sampleRate, channel, format).coerceAtLeast(4096)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(format)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channel)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize AudioTrack: ${e.message}")
        }
    }

    private fun playPcmAudio(base64: String) {
        if (_isMuted.value) return
        try {
            val pcmBytes = Base64.decode(base64, Base64.DEFAULT)
            audioTrack?.write(pcmBytes, 0, pcmBytes.size)
        } catch (e: Exception) {
            Log.e(tag, "Error playing PCM audio: ${e.message}")
        }
    }

    private fun stopAudioTrackPlayback() {
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.play()
        } catch (_: Exception) {}
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun addMessage(sender: String, text: String) {
        val newMsg = VoiceMessage(sender = sender, text = text)
        _messages.value = _messages.value + newMsg
    }

    fun clearMessages() {
        _messages.value = emptyList()
        _currentAssistantText.value = ""
        _currentUserText.value = ""
    }

    fun endSession() {
        stopRecording()
        try {
            webSocket?.close(1000, "User ended session")
        } catch (_: Exception) {}
        webSocket = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null

        try {
            tts?.stop()
        } catch (_: Exception) {}

        _sessionState.value = LiveSessionState.IDLE
        _audioWaveLevel.value = 0f
    }

    fun destroy() {
        endSession()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}
