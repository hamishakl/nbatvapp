package com.example.nba

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide

class MainActivity : FragmentActivity() {

    private lateinit var pager: ViewPager2
    private lateinit var status: TextView
    private val mainHandler = Handler(Looper.getMainLooper())
    private var games: List<Game> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pager = findViewById(R.id.pager)
        status = findViewById(R.id.status)
        loadGames()
    }

    private fun loadGames() {
        showStatus("Loading games…")
        Thread {
            val result = runCatching { GameScraper.fetchGames() }
            mainHandler.post {
                result.onSuccess { fetched ->
                    games = fetched
                    if (fetched.isEmpty()) {
                        showStatus("No NBA games listed today.")
                    } else {
                        hideStatus()
                        pager.adapter = GamesAdapter(fetched)
                    }
                }.onFailure { e ->
                    showStatus("Couldn't load games: ${e.message}")
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
            putExtra(GameActivity.EXTRA_URL, game.url)
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && games.isNotEmpty()) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (pager.currentItem > 0) pager.currentItem -= 1
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (pager.currentItem < games.size - 1) pager.currentItem += 1
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    openGame(games[pager.currentItem])
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private class GamesAdapter(private val games: List<Game>) :
        RecyclerView.Adapter<GamesAdapter.PageHolder>() {

        class PageHolder(view: View) : RecyclerView.ViewHolder(view) {
            val left: ImageView = view.findViewById(R.id.left_image)
            val right: ImageView = view.findViewById(R.id.right_image)
            val matchup: TextView = view.findViewById(R.id.matchup)
            val time: TextView = view.findViewById(R.id.time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.page_game, parent, false)
            view.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT,
            )
            return PageHolder(view)
        }

        override fun onBindViewHolder(holder: PageHolder, position: Int) {
            val game = games[position]
            holder.matchup.text =
                "${TeamImages.mascot(game.awayTeam)} vs ${TeamImages.mascot(game.homeTeam)}"
            holder.time.text = game.displayTime

            Glide.with(holder.left).load(TeamImages.urlFor(game.awayTeam)).into(holder.left)
            Glide.with(holder.right).load(TeamImages.urlFor(game.homeTeam)).into(holder.right)
        }

        override fun getItemCount(): Int = games.size
    }
}
