package com.example.mozic.core.network.ws

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.model.chat.ConnectionState
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.network.BuildConfig
import com.example.mozic.core.network.dto.AckFrameDto
import com.example.mozic.core.network.dto.FRAME_TYPE_READ
import com.example.mozic.core.network.dto.FRAME_TYPE_SEND
import com.example.mozic.core.network.dto.FRAME_TYPE_TYPING
import com.example.mozic.core.network.dto.MessageFrameDto
import com.example.mozic.core.network.dto.ReadFrameDto
import com.example.mozic.core.network.dto.ReadReceiptFrameDto
import com.example.mozic.core.network.dto.SendFrameDto
import com.example.mozic.core.network.dto.TypingFrameDto
import com.example.mozic.core.network.dto.TypingRelayFrameDto
import com.example.mozic.core.network.mapper.toDomain
import com.example.mozic.core.network.mapper.toWireDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val INITIAL_BACKOFF_MS = 1_000L
private const val MAX_BACKOFF_MS = 30_000L

/**
 * C5's WS client — one connection, owned here, not per-screen. Auto-reconnects
 * with exponential backoff (capped at 30s, reset on a successful connect),
 * and only runs while both logged in *and* foregrounded
 * ([ProcessLifecycleOwner], per CLAUDE_PERSON_C.md §5 — a music app doesn't
 * need a chat socket while backgrounded, and this dodges background-network
 * restrictions). `:core:data`'s `RealChatRepository` is the only consumer — it
 * owns Room, this class only ever speaks JSON frames.
 */
@Singleton
class ChatWebSocketClient @Inject constructor(
    private val client: HttpClient,
    private val authRepository: AuthRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var session: DefaultClientWebSocketSession? = null

    @Volatile
    private var sinceEpochMs: Long = 0L

    private val _connectionState = MutableStateFlow(ConnectionState.OFFLINE)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incoming = MutableSharedFlow<ServerFrame>(extraBufferCapacity = FRAME_BUFFER_CAPACITY)
    val incoming: SharedFlow<ServerFrame> = _incoming.asSharedFlow()

    init {
        val foreground = ProcessLifecycleOwner.get().lifecycle.currentStateFlow
            .map { it.isAtLeast(Lifecycle.State.STARTED) }

        scope.launch {
            combine(authRepository.authState, foreground) { auth, isForeground -> auth to isForeground }
                .distinctUntilChanged()
                .collectLatest { (auth, isForeground) ->
                    if (auth is AuthState.LoggedIn && isForeground) {
                        runConnectionLoop(auth.accessToken)
                    } else {
                        _connectionState.value = ConnectionState.OFFLINE
                    }
                }
        }
    }

    /** Called whenever a message is persisted, so the next (re)connect's `since=` backfill never regresses. */
    fun updateSince(epochMs: Long) {
        if (epochMs > sinceEpochMs) sinceEpochMs = epochMs
    }

    suspend fun sendMessage(message: Message) {
        sendFrame(SendFrameDto(FRAME_TYPE_SEND, message.toWireDto()))
    }

    suspend fun sendRead(conversationId: String) {
        sendFrame(ReadFrameDto(FRAME_TYPE_READ, conversationId))
    }

    suspend fun sendTyping(conversationId: String, isTyping: Boolean) {
        sendFrame(TypingFrameDto(FRAME_TYPE_TYPING, conversationId, isTyping))
    }

    private suspend inline fun <reified T> sendFrame(frame: T) {
        val current = session ?: return
        runCatching { current.send(Frame.Text(json.encodeToString(frame))) }
    }

    // Every path through the body suspends (either the long-lived `webSocket`
    // block or `delay`), so this loop is cooperatively cancellable without an
    // explicit isActive check — `collectLatest` cancels it cleanly on the next
    // auth/foreground change. Catching/swallowing Exception here is
    // deliberate: any failure mode (network drop, server restart, DNS
    // hiccup, …) all mean the same thing — retry with backoff — so there is
    // nothing more specific to do with it.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun runConnectionLoop(token: String) {
        var backoff = INITIAL_BACKOFF_MS
        while (true) {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                client.webSocket(wsUrl(token)) {
                    session = this
                    _connectionState.value = ConnectionState.CONNECTED
                    backoff = INITIAL_BACKOFF_MS
                    for (frame in incoming) {
                        if (frame is Frame.Text) handleFrame(frame.readText())
                    }
                }
            } catch (e: Exception) {
                // Fall through to the retry/backoff below — a dropped socket
                // (network change, server restart, …) is expected, not fatal.
            }
            session = null
            _connectionState.value = ConnectionState.OFFLINE
            delay(backoff)
            backoff = (backoff * 2).coerceAtMost(MAX_BACKOFF_MS)
        }
    }

    private suspend fun handleFrame(text: String) {
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return
        val frame = when (root["type"]?.jsonPrimitive?.content) {
            "ack" -> json.decodeFromJsonElement<AckFrameDto>(root)
                .let { ServerFrame.Ack(it.messageId) }

            "message" -> json.decodeFromJsonElement<MessageFrameDto>(root)
                .let { ServerFrame.Push(it.message.toDomain()) }

            "read" -> json.decodeFromJsonElement<ReadReceiptFrameDto>(root)
                .let { ServerFrame.Read(it.conversationId, it.upToMs) }

            "typing" -> json.decodeFromJsonElement<TypingRelayFrameDto>(root)
                .let { ServerFrame.Typing(it.conversationId, it.isTyping) }

            else -> null
        }
        frame?.let { _incoming.emit(it) }
    }

    private fun wsUrl(token: String): String {
        val since = sinceEpochMs.takeIf { it > 0 }?.let { "&since=$it" }.orEmpty()
        return "ws://${BuildConfig.CHAT_WS_HOST}/ws?token=$token$since"
    }

    private companion object {
        const val FRAME_BUFFER_CAPACITY = 64
    }
}
