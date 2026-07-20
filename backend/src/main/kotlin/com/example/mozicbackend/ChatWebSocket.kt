package com.example.mozicbackend

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ChatWebSocket")
// encodeDefaults is required: every outgoing frame's "type" discriminator is a
// default-valued property (e.g. `type: String = "typing"`), and kotlinx.serialization
// omits default-valued properties from output unless told otherwise.
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * One endpoint, `wss://…/ws?token=<supabase access token>&since=<epochMs>`
 * (`since` optional — omit it to skip the reconnect backfill). Protocol is
 * documented in full in PROTOCOL.md; this is the implementation of the
 * "persist first, then ack, then push" rule from CLAUDE_PERSON_C.md §4.
 */
fun Route.chatWebSocketRoute(gateway: SupabaseGateway, connections: ConnectionManager) {
    webSocket("/ws") {
        val token = call.request.queryParameters["token"]
        val sinceEpochMs = call.request.queryParameters["since"]?.toLongOrNull()
        val userId = token?.let { runCatching { gateway.authenticate(it) }.getOrNull() }

        if (userId == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "invalid or missing token"))
            return@webSocket
        }

        connections.register(userId, this)
        log.info("user {} connected", userId)

        try {
            if (sinceEpochMs != null) {
                val conversationIds = gateway.conversationIdsFor(userId)
                for (message in gateway.messagesSince(conversationIds, sinceEpochMs)) {
                    send(Frame.Text(json.encodeToString(MessageFrame(message = message))))
                }
            }

            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val root = runCatching { json.parseToJsonElement(frame.readText()).jsonObject }.getOrNull() ?: continue
                when (root["type"]?.jsonPrimitive?.content) {
                    "send" -> onSend(userId, json.decodeFromJsonElement<SendFrame>(root), gateway, connections)
                    "read" -> onRead(userId, json.decodeFromJsonElement<ReadFrame>(root), gateway, connections)
                    "typing" -> onTyping(userId, json.decodeFromJsonElement<TypingFrame>(root), gateway, connections)
                    else -> log.warn("unknown frame type from {}: {}", userId, root)
                }
            }
        } finally {
            connections.unregister(userId, this)
            log.info("user {} disconnected", userId)
        }
    }
}

private suspend fun onSend(
    senderId: String,
    frame: SendFrame,
    gateway: SupabaseGateway,
    connections: ConnectionManager,
) {
    val message = frame.message
    if (message.senderId != senderId) {
        log.warn("dropped send: claimed senderId {} != authenticated user {}", message.senderId, senderId)
        return
    }

    val participants = resolveParticipants(message.conversationId, gateway, connections) ?: return
    if (senderId != participants.first && senderId != participants.second) {
        log.warn("dropped send: {} is not a participant of {}", senderId, message.conversationId)
        return
    }

    val persisted = message.copy(status = "SENT")
    if (!gateway.insertMessage(persisted)) {
        log.warn("failed to persist message {} in conversation {}", message.id, message.conversationId)
        return
    }

    connections.sendTo(senderId, json.encodeToString(AckFrame(messageId = message.id)))

    val peerId = if (senderId == participants.first) participants.second else participants.first
    connections.sendTo(peerId, json.encodeToString(MessageFrame(message = persisted)))
}

private suspend fun onRead(
    readerId: String,
    frame: ReadFrame,
    gateway: SupabaseGateway,
    connections: ConnectionManager,
) {
    val participants = resolveParticipants(frame.conversationId, gateway, connections) ?: return
    gateway.markRead(frame.conversationId, readerId)

    val peerId = if (readerId == participants.first) participants.second else participants.first
    connections.sendTo(
        peerId,
        json.encodeToString(ReadReceiptFrame(conversationId = frame.conversationId, upToMs = System.currentTimeMillis())),
    )
}

private suspend fun onTyping(
    userId: String,
    frame: TypingFrame,
    gateway: SupabaseGateway,
    connections: ConnectionManager,
) {
    val participants = resolveParticipants(frame.conversationId, gateway, connections) ?: return
    val peerId = if (userId == participants.first) participants.second else participants.first
    connections.sendTo(
        peerId,
        json.encodeToString(TypingRelayFrame(conversationId = frame.conversationId, isTyping = frame.isTyping)),
    )
}

private suspend fun resolveParticipants(
    conversationId: String,
    gateway: SupabaseGateway,
    connections: ConnectionManager,
): Pair<String, String>? {
    connections.participantsOf(conversationId)?.let { return it }
    val fetched = gateway.participantsOf(conversationId) ?: return null
    connections.cacheParticipants(conversationId, fetched.first, fetched.second)
    return fetched
}
