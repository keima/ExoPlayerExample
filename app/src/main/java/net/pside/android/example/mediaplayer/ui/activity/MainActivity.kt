package net.pside.android.example.mediaplayer.ui.activity

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.CompoundButton

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

import net.pside.android.example.mediaplayer.R
import net.pside.android.example.mediaplayer.databinding.ActivityMainBinding
import net.pside.android.example.mediaplayer.util.ExoPlayerUtil

import java.io.IOException

import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var exoPlayer: SimpleExoPlayer? = null

    private val debugMessageRunnable = Runnable { handleMessageAction() }

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainHandler: Handler
    private lateinit var bandwidthMeter: DefaultBandwidthMeter

    companion object {
        const val EXO_PLAYER_USER_AGENT = "mediaplayer-example"
        // public static final String URL_HLS_CONTENT = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
        const val URL_HLS_CONTENT = "https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        mainHandler = Handler()
        bandwidthMeter = DefaultBandwidthMeter()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            initPlayer()
        }
        handleMessageAction()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
        mainHandler.removeCallbacks(debugMessageRunnable)
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initPlayer() {
        if (exoPlayer == null) {
            //            AdaptiveVideoTrackSelection.Factory trackSelection = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
            //            TrackSelector trackSelector = new DefaultTrackSelector(mainHandler/*, trackSelection*/);
            //            TODO: r2.0.2には存在している、 r2.1.1はここで定義しない
            val trackSelector = DefaultTrackSelector()
            //            trackSelector.addListener(new TrackSelector.EventListener() {
            //                @Override
            //                public void onTrackSelectionsChanged(TrackSelections trackSelections) {
            //                    Timber.d("onTrackSelectionsChanged: %d", trackSelections.length);
            //                }
            //            });
            val loadControl = DefaultLoadControl()
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl)
        }

        exoPlayer!!.addListener(object : ExoPlayer.EventListener {
            fun onTimelineChanged(timeline: Timeline, manifest: Any) {
                Timber.d("onTimelineChanged: %h", timeline)
            }

            // TODO: r2.1.1 には存在。 r2.0.2では trackSelectorのListenerで。
            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                Timber.d("onTracksChanged: %d %d", trackGroups!!.length, trackSelections!!.length)
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                //                Timber.d("onLoadingChanged: %b", isLoading);
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Timber.d("onPlayerStateChanged: %b, %s", playWhenReady, ExoPlayerUtil.getPlaybackStateString(playbackState))
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.d(error, "onPlayerError: ")
            }

            fun onPositionDiscontinuity() {
                Timber.d("onPositionDiscontinuity: ")
            }
        })

    }

    private fun releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer!!.release()
            exoPlayer = null
        }
    }

    fun onClickReady(view: View) {
        Timber.d("onClickReady")
        val ua = Util.getUserAgent(this, EXO_PLAYER_USER_AGENT)
        val manifestUri = Uri.parse(URL_HLS_CONTENT)
        val dataSourceFactory = DefaultDataSourceFactory(this, ua, bandwidthMeter)

        //        MediaSource[] mediaSources = new MediaSource[2];
        val msChild1 = arrayOfNulls<MediaSource>(1)
        //        MediaSource[] msChild2 = new MediaSource[1];

        msChild1[0] = HlsMediaSource(manifestUri, dataSourceFactory, mainHandler, object : AdaptiveMediaSourceEventListener {
            fun onLoadStarted(dataSpec: DataSpec, dataType: Int, trackType: Int, trackFormat: Format, trackSelectionReason: Int, trackSelectionData: Any, mediaStartTimeMs: Long, mediaEndTimeMs: Long, elapsedRealtimeMs: Long) {
                Timber.d("> onLoadStarted: ")
            }

            fun onLoadCompleted(dataSpec: DataSpec, dataType: Int, trackType: Int, trackFormat: Format, trackSelectionReason: Int, trackSelectionData: Any, mediaStartTimeMs: Long, mediaEndTimeMs: Long, elapsedRealtimeMs: Long, loadDurationMs: Long, bytesLoaded: Long) {
                Timber.d("> onLoadCompleted: ")
            }

            fun onLoadCanceled(dataSpec: DataSpec, dataType: Int, trackType: Int, trackFormat: Format, trackSelectionReason: Int, trackSelectionData: Any, mediaStartTimeMs: Long, mediaEndTimeMs: Long, elapsedRealtimeMs: Long, loadDurationMs: Long, bytesLoaded: Long) {
                Timber.d("> onLoadCanceled: ")
            }

            fun onLoadError(dataSpec: DataSpec, dataType: Int, trackType: Int, trackFormat: Format, trackSelectionReason: Int, trackSelectionData: Any, mediaStartTimeMs: Long, mediaEndTimeMs: Long, elapsedRealtimeMs: Long, loadDurationMs: Long, bytesLoaded: Long, error: IOException, wasCanceled: Boolean) {
                Timber.d(error, "> onLoadError: ")
            }

            fun onUpstreamDiscarded(trackType: Int, mediaStartTimeMs: Long, mediaEndTimeMs: Long) {
                Timber.d("> onUpstreamDiscarded: ")
            }

            fun onDownstreamFormatChanged(trackType: Int, trackFormat: Format, trackSelectionReason: Int, trackSelectionData: Any, mediaTimeMs: Long) {
                Timber.d("> onDownstreamFormatChanged: ")
            }
        })
        //        msChild1[1] = new HlsMediaSource(manifestUri, dataSourceFactory, null, null);
        //        mediaSources[0] = new ConcatenatingMediaSource(msChild1);
        //
        //        msChild2[0] = new HlsMediaSource(manifestUri, dataSourceFactory, null, null);
        //        mediaSources[1] = new ConcatenatingMediaSource(msChild2);

        exoPlayer!!.prepare(ConcatenatingMediaSource(*msChild1))
    }

    fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Timber.d("onCheckedChanged: ${buttonView.id}")

        val view: SimpleExoPlayerView
        val playerName: String

        when (buttonView.id) {
            R.id.toggleButtonOne -> {
                view = binding.playerViewOne
                playerName = "One"
            }
            R.id.toggleButtonTwo -> {
                view = binding.playerViewTwo
                playerName = "Two"
            }
            else -> return
        }

        view.player = if (isChecked) exoPlayer else null
        Timber.d("onCheckedChanged: %s: %b", playerName, isChecked)
    }

    private fun handleMessageAction() {
        val duration = if (exoPlayer == null) 0 else exoPlayer!!.duration
        val position = if (exoPlayer == null) 0 else exoPlayer!!.currentPosition

        binding.debugText.text = String.format("%d / %d", position, duration)

        mainHandler.postDelayed(debugMessageRunnable, 1000)
    }
}
