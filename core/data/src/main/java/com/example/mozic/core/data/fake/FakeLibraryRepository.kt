package com.example.mozic.core.data.fake

import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeLibraryRepository @Inject constructor() : LibraryRepository {
    private val likedIds = MutableStateFlow(
        SampleData.songs.filter { it.isLiked }.map { it.id }.toSet(),
    )
    private val recents = MutableStateFlow(SampleData.songs.take(5))

    override fun likedSongs(): Flow<List<Song>> = likedIds.map { ids ->
        SampleData.songs.filter { it.id in ids }.map { it.copy(isLiked = true) }
    }

    override fun recentlyPlayed(): Flow<List<Song>> = recents.asStateFlow()

    override suspend fun toggleLike(songId: String) {
        likedIds.update { if (songId in it) it - songId else it + songId }
    }

    override suspend fun removeRecent(songId: String) {
        recents.update { list -> list.filterNot { it.id == songId } }
    }

    override suspend fun recordPlayed(songId: String) {
        val song = SampleData.songs.find { it.id == songId } ?: return
        recents.update { (listOf(song) + it).distinctBy { s -> s.id }.take(20) }
    }
}
