package com.example.mozic.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached conversation shell — peer profile fields, refreshed from Supabase
 * whenever the list is opened (see `RealChatRepository.refreshConversations`).
 * [hiddenLocally]/[forcedUnread] are local-only UI flags (C4's delete/mark-
 * unread menu) with no server-side equivalent — a REST refresh must never
 * stomp them, see `RealChatRepository`'s upsert-then-update-profile split.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val peerId: String,
    val peerUsername: String,
    val peerDisplayName: String,
    val peerAvatarUrl: String?,
    val peerIsPremium: Boolean,
    val hiddenLocally: Boolean = false,
    val forcedUnread: Boolean = false,
)
