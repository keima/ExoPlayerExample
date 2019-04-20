package net.pside.android.example.mediaplayer.util

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*

object ExoPlayerUtil {
    fun getPlaybackStateString(playbackState: Int): String = when (playbackState) {
        STATE_BUFFERING -> "BUFFERING"
        STATE_ENDED -> "ENDED"
        STATE_IDLE -> "IDLE"
        STATE_READY -> "READY"
        else -> "?"
    }

    fun getDiscontinuityReasonString(@DiscontinuityReason reason: Int): String = when (reason) {
        DISCONTINUITY_REASON_PERIOD_TRANSITION -> "PERIOD_TRANSITION"
        DISCONTINUITY_REASON_SEEK -> "SEEK"
        DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> "SEEK_ADJUSTMENT"
        DISCONTINUITY_REASON_AD_INSERTION -> "AD_INSERTION"
        DISCONTINUITY_REASON_INTERNAL -> "INTERNAL"
        else -> "?"
    }

    fun getTimelineChangeReasonString(@TimelineChangeReason reason: Int): String = when (reason) {
        TIMELINE_CHANGE_REASON_PREPARED -> "PREPARED"
        TIMELINE_CHANGE_REASON_RESET -> "RESET"
        TIMELINE_CHANGE_REASON_DYNAMIC -> "DYNAMIC"
        else -> "?"
    }

    fun getRepeatModeString(@RepeatMode repeatMode: Int): String = when (repeatMode) {
        REPEAT_MODE_OFF -> "OFF"
        REPEAT_MODE_ONE -> "ONE"
        REPEAT_MODE_ALL -> "ALL"
        else -> "?"
    }



}
