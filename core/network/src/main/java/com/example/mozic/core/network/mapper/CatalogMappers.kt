package com.example.mozic.core.network.mapper

import com.example.mozic.core.domain.model.Artist
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.SearchResult
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.dto.PlaylistDto
import com.example.mozic.core.network.dto.SearchArtistDto
import com.example.mozic.core.network.dto.SearchCatalogRowDto
import com.example.mozic.core.network.dto.SearchResultType
import com.example.mozic.core.network.dto.SongDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

fun SongDto.toDomain(): Song = Song(
    id = id,
    title = title,
    artistName = artistName,
    coverImageUrl = coverImageUrl,
    audioUrl = audioUrl,
    durationMs = durationMs,
)

fun PlaylistDto.toDomain(songCount: Int): Playlist = Playlist(
    id = id,
    title = title,
    coverImageUrl = coverImageUrl,
    ownerId = ownerId,
    isPublic = isPublic,
    category = PlaylistCategory.valueOf(category),
    songCount = songCount,
)

/** Shared by `NetworkSongRepository.homeContent()` and `NetworkPlaylistRepository.playlists()`. */
suspend fun SupabaseCatalogApi.playlistsWithCounts(category: PlaylistCategory): List<Playlist> {
    val dtos = playlists(category.name)
    val counts = playlistSongCounts(dtos.map { it.id })
    return dtos.map { it.toDomain(counts[it.id] ?: 0) }
}

/**
 * [playlistSongCount] is resolved by the caller (a batched `playlistSongCounts`
 * call over the whole result page) rather than fetched here, to avoid an N+1
 * query per playlist hit in a search results page.
 */
fun SearchCatalogRowDto.toDomain(json: Json, playlistSongCount: (String) -> Int): SearchResult? = when (type) {
    SearchResultType.SONG -> song
        ?.let { json.decodeFromJsonElement<SongDto>(it) }
        ?.let { SearchResult.SongResult(it.toDomain()) }

    SearchResultType.ARTIST -> artist
        ?.let { json.decodeFromJsonElement<SearchArtistDto>(it) }
        ?.let { Artist(id = it.id, name = it.name, imageUrl = it.imageUrl, followerCount = 0) }
        ?.let { SearchResult.ArtistResult(it) }

    SearchResultType.PLAYLIST -> playlist
        ?.let { json.decodeFromJsonElement<PlaylistDto>(it) }
        ?.let { SearchResult.PlaylistResult(it.toDomain(playlistSongCount(it.id))) }

    else -> null
}
