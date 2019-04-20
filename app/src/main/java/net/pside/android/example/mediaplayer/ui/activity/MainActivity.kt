package net.pside.android.example.mediaplayer.ui.activity

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.DefaultMediaSourceEventListener
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import net.pside.android.example.mediaplayer.R
import net.pside.android.example.mediaplayer.databinding.ActivityMainBinding
import net.pside.android.example.mediaplayer.util.LogEventListener
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
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
            exoPlayer!!.addListener(LogEventListener(exoPlayer!!))
//            exoPlayer!!.addAnalyticsListener(EventLogger(null))
        }
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
        val hlsFactory = HlsMediaSource.Factory(dataSourceFactory)

        exoPlayer!!.prepare(ConcatenatingMediaSource(
                hlsFactory.createMediaSource(manifestUri).apply {
                    addEventListener(mainHandler, object : DefaultMediaSourceEventListener() {})
                },
                hlsFactory.createMediaSource(manifestUri),
                hlsFactory.createMediaSource(manifestUri)
        ))
    }

    fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Timber.d("onCheckedChanged: ${buttonView.id}")


        val view: PlayerView
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

//        PlayerView.switchTargetView(exoPlayer, )

        view.player = if (isChecked) exoPlayer else null
        Timber.d("onCheckedChanged: %s: %b", playerName, isChecked)
    }

    private fun handleMessageAction() {
        val player = exoPlayer
        val (duration, position) = if (player == null) {
            0 to 0
        } else {
            player.duration to player.currentPosition
        }

        binding.debugText.text = String.format("%d / %d", position, duration)

        mainHandler.postDelayed(debugMessageRunnable, 1000)
    }
}
