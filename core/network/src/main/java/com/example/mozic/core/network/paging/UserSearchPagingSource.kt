package com.example.mozic.core.network.paging

import com.example.mozic.core.domain.model.User
import com.example.mozic.core.network.SupabaseSocialApi
import com.example.mozic.core.network.mapper.toDomain

/**
 * [followedIds] is a snapshot lambda (backed by [com.example.mozic.core.network.repository.NetworkSocialRepository]'s
 * in-memory follow-state cache), not a live [kotlinx.coroutines.flow.Flow] — Paging re-evaluates a
 * [PagingSource] wholesale on invalidation, not per emitted value, so a page already on screen
 * doesn't retroactively update if a follow happens elsewhere. Documented, minor, known gap (see
 * doc/PROGRESS.md) — the Following list and profile screens, both non-paged, don't share it.
 */
internal class UserSearchPagingSource(
    private val api: SupabaseSocialApi,
    private val query: String,
    private val excludingUserId: String?,
    private val followedIds: () -> Set<String>,
) : OffsetPagingSource<User>() {
    override suspend fun fetch(range: IntRange): RangePage<User> {
        val page = api.searchProfiles(query, excludingUserId, range)
        val followed = followedIds()
        return RangePage(page.items.map { it.toDomain(isFollowed = it.id in followed) }, page.total)
    }
}
