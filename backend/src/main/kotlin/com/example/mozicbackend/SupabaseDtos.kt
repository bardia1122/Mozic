package com.example.mozicbackend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/** Response shape of `GET /auth/v1/user`. */
@Serializable
data class SupabaseAuthUser(val id: String, val email: String? = null)

/** `conversations` row — nullable participant fields so a `select=id`-only query still decodes. */
@Serializable
data class ConversationRow(
    val id: String,
    @SerialName("user_a") val userA: String? = null,
    @SerialName("user_b") val userB: String? = null,
)

/** `messages` row, PostgREST's snake_case column names. */
@Serializable
data class MessageRow(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("sent_at") val sentAt: String,
    val status: String,
    @SerialName("payload_type") val payloadType: String,
    val text: String? = null,
    @SerialName("song_id") val songId: String? = null,
    @SerialName("song_title") val songTitle: String? = null,
    @SerialName("song_artist") val songArtist: String? = null,
    @SerialName("song_cover") val songCover: String? = null,
)

fun MessageRow.toWire() = WireMessage(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    sentAtEpochMs = Instant.parse(sentAt).toEpochMilli(),
    status = status,
    payloadType = payloadType,
    text = text,
    songId = songId,
    songTitle = songTitle,
    songArtist = songArtist,
    songCover = songCover,
)
