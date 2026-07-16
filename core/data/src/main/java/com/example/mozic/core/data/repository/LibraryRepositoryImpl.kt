package com.example.mozic.core.data.repository

import com.example.mozic.core.common.result.getOrNull
import com.example.mozic.core.data.local.dao.LikedSongDao
import com.example.mozic.core.data.local.dao.RecentPlayDao
import com.example.mozic.core.data.local.entity.LikedSongEntity
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.LibraryRepository
import com.example.mozic.core.domain.repository.SongRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * IDs are real (Room), resolved to full [Song]s via [songRepository] — still
 * sample-data-backed under the hood pending Person C's real catalog (C2),
 * same seam `SearchRepositoryImpl` uses for search results.
 *
 * [recordPlayed] has no caller yet: per `doc/CLAUDE_PERSON_B.md` §5.5, Person
 * A's `PlayerController` calls it on media-item transition (his A1/A2) — an
 * empty Recently Played list until that lands is the honest state, not a bug.
 */
@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val likedSongDao: LikedSongDao,
    private val recentPlayDao: RecentPlayDao,
    private val songRepository: SongRepository,
) : LibraryRepository {

    override fun likedSongs(): Flow<List<Song>> = likedSongDao.likedSongIds().map { ids ->
        ids.mapNotNull { id -> songRepository.song(id).getOrNull()?.copy(isLiked = true) }
    }

    override fun recentlyPlayed(): Flow<List<Song>> = recentPlayDao.recentSongIds().map { ids ->
        ids.mapNotNull { id -> songRepository.song(id).getOrNull() }
    }

    override suspend fun toggleLike(songId: String) {
        if (likedSongDao.isLiked(songId)) {
            likedSongDao.unlike(songId)
        } else {
            likedSongDao.like(LikedSongEntity(songId = songId, likedAtEpochMs = System.currentTimeMillis()))
        }
    }

    override suspend fun removeRecent(songId: String) {
        recentPlayDao.remove(songId)
    }

    override suspend fun recordPlayed(songId: String) {
        recentPlayDao.recordPlay(songId = songId, playedAtEpochMs = System.currentTimeMillis())
    }
}
