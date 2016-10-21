package net.pside.android.example.mediaplayer.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import net.pside.android.example.mediaplayer.R;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String EXO_PLAYER_USER_AGENT = "mediaplayer-example";
    public static final String URL_HLS_CONTENT = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";

    @BindViews({R.id.playerViewOne, R.id.playerViewTwo})
    SimpleExoPlayerView[] playerViews;

    @BindViews({R.id.toggleButtonOne, R.id.toggleButtonTwo})
    ToggleButton[] toggleButtons;

    private SimpleExoPlayer exoPlayer;

    private Handler mainHandler;
    private DefaultBandwidthMeter bandwidthMeter;

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
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
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        }
        togglePlayer(0, true);
        togglePlayer(1, true);
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

        HlsMediaSource mediaSource = new HlsMediaSource(manifestUri, dataSourceFactory, null, null);

        exoPlayer.prepare(mediaSource);
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
}
