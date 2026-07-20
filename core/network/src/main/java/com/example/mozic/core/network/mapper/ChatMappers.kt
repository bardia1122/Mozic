package com.example.mozic.core.network.mapper

import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.model.chat.MessagePayload
import com.example.mozic.core.domain.model.chat.MessageStatus
import com.example.mozic.core.network.dto.ConversationRowDto
import com.example.mozic.core.network.dto.MessageRowDto
import com.example.mozic.core.network.dto.ProfileRowDto
import com.example.mozic.core.network.dto.WireMessageDto
import java.time.OffsetDateTime

private const val PAYLOAD_TYPE_SONG = "song"

fun WireMessageDto.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    sentAtEpochMs = sentAtEpochMs,
    status = MessageStatus.valueOf(status),
    payload = toPayload(payloadType, text, songId, songTitle, songArtist, songCover),
)

fun Message.toWireDto(): WireMessageDto {
    val fields = payload.toWireFields()
    return WireMessageDto(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        sentAtEpochMs = sentAtEpochMs,
        status = status.name,
        payloadType = fields.payloadType,
        text = fields.text,
        songId = fields.songId,
        songTitle = fields.songTitle,
        songArtist = fields.songArtist,
        songCover = fields.songCover,
    )
}

/**
 * [MessageRowDto.sentAt] is Postgres's ISO-8601 `timestamptz` text — the WS
 * wire format's epoch-millis field needs no such parse.
 */
fun MessageRowDto.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    sentAtEpochMs = OffsetDateTime.parse(sentAt).toInstant().toEpochMilli(),
    status = MessageStatus.valueOf(status),
    payload = toPayload(payloadType, text, songId, songTitle, songArtist, songCover),
)

/**
 * `lastMessage`/`unreadCount` are always placeholders here — those come from
 * `:core:data`'s Room cache, never from this REST-fetched conversation shell.
 * [peerId] resolves which side of `user_a`/`user_b` the caller already picked.
 */
fun ConversationRowDto.toDomain(peerProfile: ProfileRowDto): Conversation = Conversation(
    id = id,
    peer = User(
        id = peerProfile.id,
        username = peerProfile.username,
        displayName = peerProfile.displayName,
        avatarUrl = peerProfile.avatarUrl,
        isPremium = peerProfile.isPremium,
        // Follow state is C6's (social graph, not yet built) — unknown here.
        isFollowed = false,
    ),
    lastMessage = null,
    unreadCount = 0,
)

fun ConversationRowDto.peerId(myUserId: String): String = if (userA == myUserId) userB else userA

private fun toPayload(
    payloadType: String,
    text: String?,
    songId: String?,
    songTitle: String?,
    songArtist: String?,
    songCover: String?,
): MessagePayload = if (payloadType == PAYLOAD_TYPE_SONG) {
    MessagePayload.SongShare(
        songId = songId.orEmpty(),
        title = songTitle.orEmpty(),
        artistName = songArtist.orEmpty(),
        coverImageUrl = songCover.orEmpty(),
    )
} else {
    MessagePayload.Text(text.orEmpty())
}

private data class WireFields(
    val payloadType: String,
    val text: String?,
    val songId: String?,
    val songTitle: String?,
    val songArtist: String?,
    val songCover: String?,
)

private fun MessagePayload.toWireFields(): WireFields = when (this) {
    is MessagePayload.Text -> WireFields("text", text, null, null, null, null)
    is MessagePayload.SongShare -> WireFields(PAYLOAD_TYPE_SONG, null, songId, title, artistName, coverImageUrl)
}
