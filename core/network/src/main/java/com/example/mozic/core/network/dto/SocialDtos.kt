package com.example.mozic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One row of `follows` (backend/supabase/schema.sql), `select=followee_id` shape. */
@Serializable
data class FollowRowDto(@SerialName("followee_id") val followeeId: String)

/** POST body for a `follows` insert. */
@Serializable
data class FollowInsertDto(
    @SerialName("follower_id") val followerId: String,
    @SerialName("followee_id") val followeeId: String,
)
