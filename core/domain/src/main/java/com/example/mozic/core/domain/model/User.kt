package com.example.mozic.core.domain.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val isPremium: Boolean,
    val isFollowed: Boolean,
)
