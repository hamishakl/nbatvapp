package com.example.nba

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class GameActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        findViewById<TextView>(R.id.game_title).text =
            intent.getStringExtra(EXTRA_TEAMS).orEmpty()
    }

    companion object {
        const val EXTRA_TEAMS = "extra_teams"
        const val EXTRA_URL = "extra_url"
    }
}
