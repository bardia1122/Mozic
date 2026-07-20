package com.example.mozicbackend

import kotlinx.serialization.Serializable

/**
 * Wire shape of a chat message — camelCase, matches the frozen `Message`
 * domain model the Android client maps this into (see PROTOCOL.md). Kept
 * separate from [MessageRow] (PostgREST's snake_case row shape) so a DB
 * column rename never leaks into the client-facing protocol.
 */
@Serializable
data class WireMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val sentAtEpochMs: Long,
    val status: String,
    val payloadType: String,
    val text: String? = null,
    val songId: String? = null,
    val songTitle: String? = null,
    val songArtist: String? = null,
    val songCover: String? = null,
)

// Client → server frames. Each is decoded from the raw JSON object once its
// "type" discriminator has been read (see ChatWebSocket.kt) — no polymorphic
// serializer needed for three shapes.
@Serializable
data class SendFrame(val message: WireMessage)

@Serializable
data class ReadFrame(val conversationId: String)

@Serializable
data class TypingFrame(val conversationId: String, val isTyping: Boolean)

// Server → client frames.
@Serializable
data class AckFrame(val messageId: String, val type: String = "ack")

@Serializable
data class MessageFrame(val message: WireMessage, val type: String = "message")

@Serializable
data class ReadReceiptFrame(val conversationId: String, val upToMs: Long, val type: String = "read")

@Serializable
data class TypingRelayFrame(val conversationId: String, val isTyping: Boolean, val type: String = "typing")
