package com.example.nba

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.ui.PlayerView

class GameActivity : FragmentActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var playerView: PlayerView
    private lateinit var status: TextView
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        playerView = findViewById(R.id.player_view)
        status = findViewById(R.id.stream_status)

        val eventUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (eventUrl.isBlank()) {
            status.text = "No event URL provided."
            return
        }

        Thread {
            val result = runCatching { StreamResolver.resolve(eventUrl) }
            mainHandler.post {
                result.onSuccess { startPlayback(it, eventUrl) }
                    .onFailure { status.text = "Couldn't load stream: ${it.message}" }
            }
        }.start()
    }

    private fun startPlayback(m3u8: String, refererUrl: String) {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(USER_AGENT)
            .setDefaultRequestProperties(mapOf("Referer" to refererUrl))

        val mediaItem = MediaItem.Builder()
            .setUri(m3u8)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        val source = HlsMediaSource.Factory(httpFactory).createMediaSource(mediaItem)

        val exo = ExoPlayer.Builder(this).build().also { player = it }
        playerView.player = exo
        exo.setMediaSource(source)
        exo.playWhenReady = true
        exo.prepare()

        status.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        player?.play()
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 12; AndroidTV) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
}
