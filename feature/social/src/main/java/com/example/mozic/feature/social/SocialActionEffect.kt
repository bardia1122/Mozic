package com.example.mozic.feature.social

/** Shared one-shot effect for a failed follow/unfollow tap — used by all three C6 screens. */
sealed interface SocialActionEffect {
    data object ActionFailed : SocialActionEffect

    data object LoginRequired : SocialActionEffect
}
