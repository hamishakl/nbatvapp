package com.example.nba

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private lateinit var listView: ListView
    private lateinit var status: TextView
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.games)
        listView.itemsCanFocus = true
        status = findViewById(R.id.status)
        loadGames()
    }

    private fun loadGames() {
        showStatus("Loading games…")
        Thread {
            val result = runCatching { GameScraper.fetchGames() }
            mainHandler.post {
                result.onSuccess { games ->
                    if (games.isEmpty()) {
                        showStatus("No NBA games listed today.")
                        listView.adapter = null
                    } else {
                        hideStatus()
                        listView.adapter = GameAdapter(games)
                        listView.post { listView.requestFocus() }
                    }
                }.onFailure { e ->
                    showStatus("Couldn't load games: ${e.message}")
                    listView.adapter = null
                }
            }
        }.start()
    }

    private fun showStatus(text: String) {
        status.text = text
        status.visibility = View.VISIBLE
    }

    private fun hideStatus() {
        status.visibility = View.GONE
    }

    private fun openGame(game: Game) {
        startActivity(Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_TEAMS, game.teams)
            putExtra(GameActivity.EXTRA_URL, game.url)
        })
    }

    private inner class GameAdapter(private val games: List<Game>) :
        ArrayAdapter<Game>(this@MainActivity, R.layout.list_item_game, games) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.list_item_game, parent, false)
            val game = games[position]
            view.findViewById<TextView>(R.id.teams).text = game.teams
            view.findViewById<TextView>(R.id.time).text = game.displayTime
            view.setOnClickListener { openGame(game) }
            return view
        }
    }
}
