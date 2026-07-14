package com.example.mozic.core.domain.model.chat

enum class MessageStatus { SENDING, SENT, READ }

/**
 * [SongShare] is denormalized on purpose (carries title/artist/cover, not just
 * the id) so a shared song still renders in offline chat history; [songId]
 * remains for tap-to-play.
 */
sealed interface MessagePayload {
    data class Text(val text: String) : MessagePayload

    data class SongShare(
        val songId: String,
        val title: String,
        val artistName: String,
        val coverImageUrl: String,
    ) : MessagePayload
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val sentAtEpochMs: Long,
    val status: MessageStatus,
    val payload: MessagePayload,
)
