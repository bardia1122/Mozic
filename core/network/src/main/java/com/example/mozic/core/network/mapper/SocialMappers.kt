package com.example.mozic.core.network.mapper

import com.example.mozic.core.domain.model.User
import com.example.mozic.core.network.dto.ProfileRowDto

fun ProfileRowDto.toDomain(isFollowed: Boolean): User = User(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    isPremium = isPremium,
    isFollowed = isFollowed,
)
