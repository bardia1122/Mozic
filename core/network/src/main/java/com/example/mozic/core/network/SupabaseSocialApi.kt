package com.example.mozic.core.network

import com.example.mozic.core.network.dto.FollowInsertDto
import com.example.mozic.core.network.dto.FollowRowDto
import com.example.mozic.core.network.dto.PlaylistDto
import com.example.mozic.core.network.dto.ProfileRowDto
import com.example.mozic.core.network.paging.RangePage
import com.example.mozic.core.network.paging.applyRange
import com.example.mozic.core.network.paging.toRangePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

private const val REST_PATH = "/rest/v1/"

/**
 * Thin wrapper over PostgREST's `profiles`/`follows`/`playlists` surface for
 * C6's social graph — same "DTOs only, mapping happens in mapper/" rule as
 * [SupabaseCatalogApi]/[SupabaseChatApi]. `profiles` and `follows` selects are
 * public (no auth needed, `backend/supabase/schema.sql`'s own
 * `..._select_all`/`follows_select_all` policies); writing a follow/unfollow
 * requires the caller's own JWT since RLS scopes both to `follower_id = auth.uid()`.
 */
@Singleton
class SupabaseSocialApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun searchProfiles(query: String, excludingUserId: String?, range: IntRange): RangePage<ProfileRowDto> {
        val response = client.get(restUrl("profiles")) {
            parameter("or", "(username.ilike.*$query*,display_name.ilike.*$query*)")
            if (excludingUserId != null) parameter("id", "neq.$excludingUserId")
            parameter("select", "*")
            applyRange(range)
        }
        return response.toRangePage()
    }

    suspend fun profileById(id: String): ProfileRowDto? {
        val response = client.get(restUrl("profiles")) {
            parameter("id", "eq.$id")
            parameter("select", "*")
        }
        return response.body<List<ProfileRowDto>>().firstOrNull()
    }

    suspend fun profiles(ids: List<String>): List<ProfileRowDto> {
        if (ids.isEmpty()) return emptyList()
        return client.get(restUrl("profiles")) {
            parameter("id", "in.(${ids.joinToString(",")})")
            parameter("select", "*")
        }.body()
    }

    suspend fun followedIds(myUserId: String): List<String> {
        val rows: List<FollowRowDto> = client.get(restUrl("follows")) {
            parameter("follower_id", "eq.$myUserId")
            parameter("select", "followee_id")
        }.body()
        return rows.map { it.followeeId }
    }

    /** `resolution=merge-duplicates` makes a re-tap of an already-followed user a safe no-op, not a 409. */
    suspend fun follow(accessToken: String, followerId: String, followeeId: String) {
        client.post(restUrl("follows")) {
            bearerAuth(accessToken)
            header("Prefer", "resolution=merge-duplicates,return=minimal")
            contentType(ContentType.Application.Json)
            setBody(FollowInsertDto(followerId, followeeId))
        }
    }

    /** A delete matching zero rows (already not following) is a normal, non-error no-op. */
    suspend fun unfollow(accessToken: String, followerId: String, followeeId: String) {
        client.delete(restUrl("follows")) {
            bearerAuth(accessToken)
            parameter("follower_id", "eq.$followerId")
            parameter("followee_id", "eq.$followeeId")
        }
    }

    suspend fun playlistsByOwner(ownerId: String): List<PlaylistDto> = client.get(restUrl("playlists")) {
        parameter("owner_id", "eq.$ownerId")
        parameter("is_public", "eq.true")
        parameter("select", "*")
    }.body()

    private fun restUrl(path: String) = "${BuildConfig.SUPABASE_URL}$REST_PATH$path"
}
