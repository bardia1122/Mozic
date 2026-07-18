package com.example.mozic.feature.player

import com.example.mozic.core.domain.model.DownloadState

/**
 * The like/download/premium facts about the current song — kept separate from
 * [com.example.mozic.core.domain.model.PlayerState] because those three come
 * from repositories [PlayerViewModel] combines itself (liked songs, download
 * state, user preferences), not from [com.example.mozic.core.domain.player.PlayerController].
 */
data class PlayerExtrasUiState(
    val isLiked: Boolean = false,
    val downloadState: DownloadState = DownloadState.NotDownloaded,
    val isPremium: Boolean = false,
)

sealed interface PlayerEffect {
    data object ShowUpgradePrompt : PlayerEffect
}
