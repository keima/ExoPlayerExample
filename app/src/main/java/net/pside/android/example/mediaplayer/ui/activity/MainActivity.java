package net.pside.android.example.mediaplayer.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import net.pside.android.example.mediaplayer.R;
import net.pside.android.example.mediaplayer.util.ExoPlayerUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String EXO_PLAYER_USER_AGENT = "mediaplayer-example";
    public static final String URL_HLS_CONTENT = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";

    @BindView(R.id.debugText)
    TextView debugText;

    @BindViews({R.id.playerViewOne, R.id.playerViewTwo})
    SimpleExoPlayerView[] playerViews;

    @BindViews({R.id.toggleButtonOne, R.id.toggleButtonTwo})
    ToggleButton[] toggleButtons;

    private SimpleExoPlayer exoPlayer;

    private Handler mainHandler;
    private DefaultBandwidthMeter bandwidthMeter;

    private final Runnable debugMessageRunnable = new Runnable() {
        @Override
        public void run() {
            handleMessageAction();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mainHandler = new Handler();
        bandwidthMeter = new DefaultBandwidthMeter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initPlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            initPlayer();
        }
        handleMessageAction();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
        mainHandler.removeCallbacks(debugMessageRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initPlayer() {
        if (exoPlayer == null) {
//            AdaptiveVideoTrackSelection.Factory trackSelection = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
//            TrackSelector trackSelector = new DefaultTrackSelector(mainHandler/*, trackSelection*/);
//            TODO: r2.0.2には存在している、 r2.1.1はここで定義しない
            TrackSelector trackSelector = new DefaultTrackSelector();
//            trackSelector.addListener(new TrackSelector.EventListener() {
//                @Override
//                public void onTrackSelectionsChanged(TrackSelections trackSelections) {
//                    Timber.d("onTrackSelectionsChanged: %d", trackSelections.length);
//                }
//            });
            LoadControl loadControl = new DefaultLoadControl();
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        }
        togglePlayer(0, true);
        togglePlayer(1, true);

        exoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Timber.d("onTimelineChanged: %h", timeline);
            }

            // TODO: r2.1.1 には存在。 r2.0.2では trackSelectorのListenerで。
            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Timber.d("onTracksChanged: %d %d", trackGroups.length, trackSelections.length);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
//                Timber.d("onLoadingChanged: %b", isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Timber.d("onPlayerStateChanged: %b, %s", playWhenReady, ExoPlayerUtil.getPlaybackStateString(playbackState));
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Timber.d(error, "onPlayerError: ");
            }

            @Override
            public void onPositionDiscontinuity() {
                Timber.d("onPositionDiscontinuity: ");
            }
        });

    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @OnClick(R.id.readyButton)
    void onClickReady() {
        String ua = Util.getUserAgent(this, EXO_PLAYER_USER_AGENT);
        Uri manifestUri = Uri.parse(URL_HLS_CONTENT);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, ua, bandwidthMeter);

//        MediaSource[] mediaSources = new MediaSource[2];
        MediaSource[] msChild1 = new MediaSource[1];
//        MediaSource[] msChild2 = new MediaSource[1];

        msChild1[0] = new HlsMediaSource(manifestUri, dataSourceFactory, mainHandler, new AdaptiveMediaSourceEventListener() {
            @Override
            public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {
                Timber.d("> onLoadStarted: ");
            }

            @Override
            public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
                Timber.d("> onLoadCompleted: ");
            }

            @Override
            public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
                Timber.d("> onLoadCanceled: ");
            }

            @Override
            public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
                Timber.d("> onLoadError: ");
            }

            @Override
            public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {
                Timber.d("> onUpstreamDiscarded: ");
            }

            @Override
            public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {
                Timber.d("> onDownstreamFormatChanged: ");
            }
        });
//        msChild1[1] = new HlsMediaSource(manifestUri, dataSourceFactory, null, null);
//        mediaSources[0] = new ConcatenatingMediaSource(msChild1);
//
//        msChild2[0] = new HlsMediaSource(manifestUri, dataSourceFactory, null, null);
//        mediaSources[1] = new ConcatenatingMediaSource(msChild2);

        exoPlayer.prepare(new ConcatenatingMediaSource(msChild1));
    }

    @OnCheckedChanged({R.id.toggleButtonOne, R.id.toggleButtonTwo})
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SimpleExoPlayerView view;
        String playerName;

        switch (buttonView.getId()) {
            case R.id.toggleButtonOne:
                view = playerViews[0];
                playerName = "One";
                break;
            case R.id.toggleButtonTwo:
                view = playerViews[1];
                playerName = "Two";
                break;
            default:
                return;
        }

        view.setPlayer(isChecked ? exoPlayer : null);
        Timber.d("onCheckedChanged: %s: %b", playerName, isChecked);
    }

    private void togglePlayer(int index, boolean isActive) {
        // ToggleButtonのstatusを変化させると対応するイベントが発火するという仕掛け
        toggleButtons[index].setChecked(isActive);
    }

    private void handleMessageAction() {
        long duration = exoPlayer == null ? 0 : exoPlayer.getDuration();
        long position = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();

        debugText.setText(String.format("%d / %d", position, duration));

        mainHandler.postDelayed(debugMessageRunnable, 1000);
    }
}
