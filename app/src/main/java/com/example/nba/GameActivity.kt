package com.example.nba

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class GameActivity : FragmentActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val teams = intent.getStringExtra(EXTRA_TEAMS).orEmpty()
        val eventUrl = intent.getStringExtra(EXTRA_URL).orEmpty()

        findViewById<TextView>(R.id.game_title).text = teams

        val status = findViewById<TextView>(R.id.stream_status)
        val urlView = findViewById<TextView>(R.id.stream_url)

        if (eventUrl.isBlank()) {
            status.text = "No event URL provided."
            return
        }

        Thread {
            val result = runCatching { StreamResolver.resolve(eventUrl) }
            mainHandler.post {
                result.onSuccess { m3u8 ->
                    status.text = "Stream URL:"
                    urlView.text = m3u8
                    urlView.visibility = View.VISIBLE
                }.onFailure { e ->
                    status.text = "Couldn't load stream: ${e.message}"
                }
            }
        }.start()
    }

    companion object {
        const val EXTRA_TEAMS = "extra_teams"
        const val EXTRA_URL = "extra_url"
    }
}
