package com.example.mozic.core.media

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service hosting the single app-wide [ExoPlayer]. [Media3PlayerController] never
 * touches ExoPlayer directly — it talks to this service through a [MediaSession]/[MediaController]
 * pair, which is what keeps playback alive across process/activity lifecycles (backgrounding,
 * swipe-from-recents) and gives us the media notification + lockscreen controls for free.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        mediaSession = MediaSession.Builder(this, player)
            .apply { launchAppPendingIntent()?.let(::setSessionActivity) }
            .build()
    }

    /**
     * Launches whatever activity the manifest declares as this app's launcher, resolved by
     * package rather than a direct [MainActivity][com.example.mozic.MainActivity] reference —
     * `:core:media` must not depend on `:app` (features/app depend on core, never the reverse).
     * [EXTRA_OPEN_NOW_PLAYING] rides along on the intent so the activity (which *can* depend on
     * this module) knows to navigate straight to Now Playing rather than just resuming wherever
     * it was left — same contract other cross-module signaling in this codebase uses (share a
     * constant defined by the lower-level module, not a class reference to the higher one).
     *
     * **`FLAG_UPDATE_CURRENT` is required, not optional**: a `PendingIntent` is matched by the
     * underlying `Intent`'s action/component/categories only — extras are explicitly excluded
     * from that equality check. Without this flag, once the system has cached a `PendingIntent`
     * for this same request code from an earlier run (e.g. an install before this extra
     * existed), every later call here silently hands back that stale cached instance — extras
     * and all — instead of one carrying today's `EXTRA_OPEN_NOW_PLAYING`.
     */
    private fun launchAppPendingIntent(): PendingIntent? =
        packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
            launchIntent.putExtra(EXTRA_OPEN_NOW_PLAYING, true)
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /** Stop the foreground service once the task is swiped away, unless a song is still playing. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    companion object {
        /** Set on the notification's launch [Intent] — see [launchAppPendingIntent]. */
        const val EXTRA_OPEN_NOW_PLAYING = "com.example.mozic.core.media.EXTRA_OPEN_NOW_PLAYING"
    }
}
