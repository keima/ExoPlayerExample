package net.pside.android.example.mediaplayer.util

import com.google.android.exoplayer2.ExoPlayer

object ExoPlayerUtil {
    fun getPlaybackStateString(playbackState: Int): String = when (playbackState) {
        ExoPlayer.STATE_BUFFERING -> "BUFFERING"
        ExoPlayer.STATE_ENDED -> "ENDED"
        ExoPlayer.STATE_IDLE -> "IDLE"
        ExoPlayer.STATE_READY -> "READY"
        else -> "UNKNOWN"
    }
}
