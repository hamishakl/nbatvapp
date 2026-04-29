package com.example.nba

import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GameScraper {

    private const val BASE_URL = "https://thetvapp.to"
    private const val NBA_URL = "$BASE_URL/nba"
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 12; AndroidTV) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private val ITEM_REGEX = Regex(
        """<a\s+href="(/event/[^"]+)"\s+class="list-group-item">\s*([^<]+?)\s*<span>([^<]+)</span>\s*</a>""",
        RegexOption.DOT_MATCHES_ALL
    )

    fun fetchGames(): List<Game> {
        val html = downloadHtml(NBA_URL)
        return ITEM_REGEX.findAll(html).map { match ->
            val href = match.groupValues[1].trim()
            val rawTeamsAndTime = match.groupValues[2].replace(Regex("\\s+"), " ").trim()
            val iso = match.groupValues[3].trim()

            val teams = rawTeamsAndTime.substringBefore(" @ ").trim()
            val display = formatLocalTime(iso) ?: rawTeamsAndTime.substringAfter(" @ ", "").trim()

            Game(
                teams = teams,
                displayTime = display,
                isoTimestamp = iso,
                url = BASE_URL + href
            )
        }.toList()
    }

    private fun downloadHtml(urlString: String): String {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "text/html,application/xhtml+xml")
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            return stream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun formatLocalTime(iso: String): String? {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date: Date = parser.parse(iso) ?: return null
            val out = SimpleDateFormat("EEE MMM d, h:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            out.format(date)
        } catch (_: Exception) {
            null
        }
    }
}
