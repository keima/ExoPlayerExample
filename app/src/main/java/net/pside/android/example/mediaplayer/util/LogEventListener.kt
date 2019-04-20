package net.pside.android.example.mediaplayer.util

import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import timber.log.Timber

class LogEventListener(
        private val player: ExoPlayer
): Player.EventListener {
    private var previousPeriodIndex = 0
    
    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        Timber.d("onTimelineChanged: timeline: %h, manifest: %h, reason: %s",
                timeline, manifest, ExoPlayerUtil.getTimelineChangeReasonString(reason))
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        val empty = if (trackGroups == TrackGroupArray.EMPTY) "(empty)" else ""
        val currentPeriodIndex = player.currentPeriodIndex
        val currentWindowIndex = player.currentWindowIndex

        Timber.d("onTracksChanged: groups: %h%s, selections: %h, period: %d, window: %d",
                trackGroups, empty, trackSelections, currentPeriodIndex, currentWindowIndex)

        if (currentPeriodIndex > previousPeriodIndex) {
            onTracksChangedFastForward()
        } else if (currentPeriodIndex < previousPeriodIndex) {
            onTracksChangedRewind()
        }
        previousPeriodIndex = currentPeriodIndex
    }
    
    private fun onTracksChangedFastForward() {
        Timber.d("onTracksChangedFastForward: ")
    }
    
    private fun onTracksChangedRewind() {
        Timber.d("onTracksChangedRewind: ")
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Timber.d("onLoadingChanged: %b", isLoading)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Timber.d("onPlayerStateChanged: playWhenReady: %b, state: %s",
                playWhenReady, ExoPlayerUtil.getPlaybackStateString(playbackState))
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        Timber.d("onRepeatModeChanged: %s", ExoPlayerUtil.getRepeatModeString(repeatMode))
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        Timber.d("onShuffleModeEnabledChanged: %b", shuffleModeEnabled)
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Timber.d(error, "onPlayerError")
    }

    override fun onPositionDiscontinuity(reason: Int) {
        Timber.d("onPositionDiscontinuity: %s",
                ExoPlayerUtil.getDiscontinuityReasonString(reason))
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        Timber.d("onPlaybackParametersChanged: %h", playbackParameters)
    }

    override fun onSeekProcessed() {
        Timber.d("onSeekProcessed")
    }
}
