package com.example.mozic.core.network.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mozic.core.common.result.Result
import com.example.mozic.core.domain.model.HomeContent
import com.example.mozic.core.domain.model.HomeRow
import com.example.mozic.core.domain.model.HomeSection
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.SongRepository
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.mapper.playlistsWithCounts
import com.example.mozic.core.network.mapper.toDomain
import com.example.mozic.core.network.paging.SongsPagingSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val HOME_ROW_SIZE = 8
private const val HOME_CAROUSEL_SIZE = 5
private const val HOME_SECTION_PAGE_SIZE = 20
private const val POPULARITY_DESC = "popularity.desc"
private const val CREATED_AT_DESC = "created_at.desc"

/**
 * Real, Supabase/PostgREST-backed [SongRepository] (C2). Suspend Ktor calls are
 * already non-blocking and internally sequenced onto their own dispatcher — no
 * `@IoDispatcher` to inject here, same reasoning as
 * `UserPreferencesRepositoryImpl`'s DataStore calls.
 */
@Singleton
class NetworkSongRepository @Inject constructor(
    private val api: SupabaseCatalogApi,
) : SongRepository {

    override fun homeContent(): Flow<HomeContent> = flow {
        coroutineScope {
            val popularDeferred = async { api.songs(POPULARITY_DESC, 0 until HOME_ROW_SIZE) }
            val newestDeferred = async { api.songs(CREATED_AT_DESC, 0 until HOME_ROW_SIZE) }
            val worldDeferred = async { api.playlistsWithCounts(PlaylistCategory.WORLD) }
            val localDeferred = async { api.playlistsWithCounts(PlaylistCategory.LOCAL) }

            val popular = popularDeferred.await().items.map { it.toDomain() }
            val newest = newestDeferred.await().items.map { it.toDomain() }

            emit(
                HomeContent(
                    carousel = popular.take(HOME_CAROUSEL_SIZE),
                    rows = listOf(
                        HomeRow.Songs("Most popular", HomeSection.MOST_POPULAR, popular),
                        HomeRow.Songs("Newest", HomeSection.NEWEST, newest),
                        HomeRow.Playlists("Global playlists", PlaylistCategory.WORLD, worldDeferred.await()),
                        HomeRow.Playlists("Local playlists", PlaylistCategory.LOCAL, localDeferred.await()),
                    ),
                ),
            )
        }
    }

    override fun pagedSection(section: HomeSection): Flow<PagingData<Song>> {
        val order = when (section) {
            HomeSection.MOST_POPULAR -> POPULARITY_DESC
            HomeSection.NEWEST -> CREATED_AT_DESC
        }
        return Pager(PagingConfig(pageSize = HOME_SECTION_PAGE_SIZE)) {
            SongsPagingSource(api, order)
        }.flow
    }

    // Same reasoning as OffsetPagingSource.load: this is Song's one-shot
    // Result-wrapped boundary, so any network/serialization failure needs to
    // land as Result.Error rather than propagate.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun song(id: String): Result<Song> = try {
        api.songById(id)?.toDomain()?.let { Result.Success(it) }
            ?: Result.Error(NoSuchElementException("No song with id=$id"))
    } catch (e: Exception) {
        Result.Error(e)
    }
}
