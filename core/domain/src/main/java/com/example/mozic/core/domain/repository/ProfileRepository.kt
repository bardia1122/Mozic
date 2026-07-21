package com.example.mozic.core.domain.repository

import com.example.mozic.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * The caller's own account profile (`profiles` row) — separate from
 * [SocialRepository], which is scoped to browsing/following *other* users.
 * Requires login; every method throws [com.example.mozic.core.domain.model.NotLoggedInException]
 * when called while logged out, same convention [SocialRepository]'s
 * follow/unfollow already established.
 */
interface ProfileRepository {
    /** The logged-in user's own live profile, `null` while logged out. */
    fun myProfile(): Flow<User?>

    /** Uploads to Supabase Storage and patches `profiles.avatar_url`, returning the new public URL. */
    suspend fun updateAvatar(imageBytes: ByteArray, mimeType: String): String

    /** Clears `profiles.avatar_url` back to null and best-effort deletes the Storage file. */
    suspend fun removeAvatar()

    suspend fun setPremium(isPremium: Boolean)
}
