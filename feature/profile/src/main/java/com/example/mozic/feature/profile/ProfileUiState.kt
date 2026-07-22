package com.example.mozic.feature.profile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    /**
     * [displayName]/[isLoggedIn] only ever come from the real `profiles` row
     * (via [com.example.mozic.core.domain.repository.ProfileRepository]) —
     * `null`/`false` while logged out, since there's no server identity to
     * show yet. [avatarUri]/[isPremium] fall back to the local
     * device-only prefs in that case, unchanged from before login existed.
     */
    data class Content(
        val displayName: String?,
        val avatarUri: String?,
        val isPremium: Boolean,
        val isLoggedIn: Boolean,
        val isPurchasing: Boolean,
        /** Only meaningful while [isLoggedIn] — 0 otherwise, since there's no server identity to count against. */
        val followerCount: Int = 0,
        val followingCount: Int = 0,
        val publicPlaylistCount: Int = 0,
    ) : ProfileUiState
}

sealed interface ProfileEvent {
    /** Logged-out fallback: stored locally only, same as before login existed. */
    data class SetLocalAvatar(val uri: String?) : ProfileEvent

    /** Logged-in path: uploaded to Supabase Storage and patched into `profiles.avatar_url`. */
    data class UploadAvatar(val bytes: ByteArray, val mimeType: String) : ProfileEvent

    /** Clears whichever avatar source is currently active (local prefs, or the real `profiles` row). */
    data object RemoveAvatar : ProfileEvent

    data object PurchasePremium : ProfileEvent
}

sealed interface ProfileEffect {
    data object PurchaseCompleted : ProfileEffect

    data object AvatarUpdateFailed : ProfileEffect
}
