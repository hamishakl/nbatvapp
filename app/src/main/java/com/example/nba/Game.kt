package com.example.nba

data class Game(
    val awayTeam: String,
    val homeTeam: String,
    val displayTime: String,
    val isoTimestamp: String,
    val url: String,
) {
    val matchup: String get() = "$awayTeam vs $homeTeam"
}
