package com.example.mozic.feature.profile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Content(val avatarUri: String?, val isPremium: Boolean, val isPurchasing: Boolean) : ProfileUiState
}

sealed interface ProfileEvent {
    data class SetAvatar(val uri: String?) : ProfileEvent

    data object PurchasePremium : ProfileEvent
}

sealed interface ProfileEffect {
    data object PurchaseCompleted : ProfileEffect
}
