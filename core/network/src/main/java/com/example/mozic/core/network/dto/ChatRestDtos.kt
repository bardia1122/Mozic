package com.example.mozic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `public.conversations` (backend/supabase/schema.sql) — read via the caller's own JWT, RLS-scoped. */
@Serializable
data class ConversationRowDto(
    val id: String,
    @SerialName("user_a") val userA: String,
    @SerialName("user_b") val userB: String,
)

/** Mirrors `public.profiles` — public read, no auth needed. */
@Serializable
data class ProfileRowDto(
    val id: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_premium") val isPremium: Boolean = false,
)

/**
 * Mirrors `public.messages`. [sentAt] is Postgres's ISO-8601 `timestamptz`
 * text, unlike the WS wire protocol's `sentAtEpochMs` long — only this
 * REST-fetched row shape needs the timestamp parse (see `ChatMappers.kt`).
 */
@Serializable
data class MessageRowDto(
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
