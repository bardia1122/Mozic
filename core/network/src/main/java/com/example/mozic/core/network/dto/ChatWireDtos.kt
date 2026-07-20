package com.example.mozic.core.network.dto

import kotlinx.serialization.Serializable

/**
 * Wire shape of a chat message over the WS protocol (`backend/PROTOCOL.md`) —
 * epoch-millis timestamp, unlike [MessageRowDto]'s REST `timestamptz` string.
 */
@Serializable
data class WireMessageDto(
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

// Client -> server. `type` is always explicit (never relies on
// encodeDefaults) — C3's own PROTOCOL.md/PROGRESS.md flag exactly this bug
// class server-side (a default-valued `type` silently omitted from JSON).
@Serializable
data class SendFrameDto(val type: String, val message: WireMessageDto)

@Serializable
data class ReadFrameDto(val type: String, val conversationId: String)

@Serializable
data class TypingFrameDto(val type: String, val conversationId: String, val isTyping: Boolean)

// Server -> client.
@Serializable
data class AckFrameDto(val messageId: String)

@Serializable
data class MessageFrameDto(val message: WireMessageDto)

@Serializable
data class ReadReceiptFrameDto(val conversationId: String, val upToMs: Long)

@Serializable
data class TypingRelayFrameDto(val conversationId: String, val isTyping: Boolean)

internal const val FRAME_TYPE_SEND = "send"
internal const val FRAME_TYPE_READ = "read"
internal const val FRAME_TYPE_TYPING = "typing"
internal const val FRAME_TYPE_ACK = "ack"
internal const val FRAME_TYPE_MESSAGE = "message"
