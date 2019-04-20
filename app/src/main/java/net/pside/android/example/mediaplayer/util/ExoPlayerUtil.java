package net.pside.android.example.mediaplayer.util;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;

public class ExoPlayerUtil {

    private ExoPlayerUtil() {
    }

    @NonNull
    public static String getPlaybackStateString(int playbackState) {
        String state;
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                state = "BUFFERING";
                break;
            case ExoPlayer.STATE_ENDED:
                state = "ENDED";
                break;
            case ExoPlayer.STATE_IDLE:
                state = "IDLE";
                break;
            case ExoPlayer.STATE_READY:
                state = "READY";
                break;
            default:
                state = "UNKNOWN";
        }
        return state;
    }

}
