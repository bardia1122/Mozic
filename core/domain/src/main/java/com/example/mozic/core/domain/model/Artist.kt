package com.example.mozic.core.domain.model

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val followerCount: Int,
)
