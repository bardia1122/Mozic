package com.example.mozic.core.media

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.DownloadRepository
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Smart play (A4): resolves a [Song] to the [MediaItem] it should actually be played from —
 * the downloaded local file if [DownloadRepository] reports [DownloadState.Downloaded], the
 * remote [Song.audioUrl] (through [PlaybackService]'s cache-wrapped `MediaSource.Factory`)
 * otherwise. A pure suspend function, unit-testable against a fake [DownloadRepository] — see
 * A4 in `doc/CLAUDE_PERSON_A.md`. Reads the current download state once per resolve (a one-shot
 * decision at play-time, same as the rest of [Media3PlayerController]'s queue setup), not a
 * live subscription — a download completing mid-playback doesn't retroactively swap the source
 * of a song already playing.
 */
class PlaybackSourceResolver @Inject constructor(
    private val downloadRepository: DownloadRepository,
) {
    suspend fun resolve(song: Song): MediaItem {
        val state = downloadRepository.downloadState(song.id).first()
        val uri = when (state) {
            is DownloadState.Downloaded -> Uri.fromFile(File(state.localFilePath))
            else -> Uri.parse(song.audioUrl)
        }
        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artistName)
                    .setArtworkUri(Uri.parse(song.coverImageUrl))
                    .build(),
            )
            .build()
    }
}
