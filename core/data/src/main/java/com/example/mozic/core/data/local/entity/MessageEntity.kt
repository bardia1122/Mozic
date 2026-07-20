package com.example.mozic.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * C5's single source of truth for chat history — the UI reads only from this
 * table (via [com.example.mozic.core.data.local.dao.MessageDao]'s
 * `PagingSource`), never straight from the network, so offline history and
 * live updates are the same code path. [status]/[payloadType] are stored as
 * plain strings (mirrors [com.example.mozic.core.domain.model.chat.MessageStatus]/
 * the `payloadType` discriminator) rather than a Room `TypeConverter`-backed
 * enum, matching this codebase's existing entities (e.g. `DownloadEntity`).
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
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
