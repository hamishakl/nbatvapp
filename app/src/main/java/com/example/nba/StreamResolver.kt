package com.example.nba

import java.net.HttpURLConnection
import java.net.URL

object StreamResolver {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 12; AndroidTV) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private val STREAM_NAME_REGEX =
        Regex("""id="stream_name"\s+name="([^"]+)"""")

    private val URL_REGEX = Regex(""""url"\s*:\s*"([^"]+)"""")

    fun resolve(eventUrl: String): String {
        val (pageHtml, cookies) = fetch(eventUrl)
        val streamName = STREAM_NAME_REGEX.find(pageHtml)?.groupValues?.get(1)
            ?: error("Stream name not found on event page")

        val origin = URL(eventUrl).let { "${it.protocol}://${it.host}" }
        val tokenUrl = "$origin/token/$streamName"
        val (tokenJson, _) = fetch(tokenUrl, cookies = cookies, referer = eventUrl, asJson = true)

        return URL_REGEX.find(tokenJson)?.groupValues?.get(1)?.replace("\\/", "/")
            ?: error("Stream URL not found in token response")
    }

    private fun fetch(
        urlString: String,
        cookies: String? = null,
        referer: String? = null,
        asJson: Boolean = false
    ): Pair<String, String> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty(
                "Accept",
                if (asJson) "application/json, text/plain, */*" else "text/html,application/xhtml+xml"
            )
            if (asJson) setRequestProperty("X-Requested-With", "XMLHttpRequest")
            if (cookies != null) setRequestProperty("Cookie", cookies)
            if (referer != null) setRequestProperty("Referer", referer)
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            val newCookies = conn.headerFields["Set-Cookie"].orEmpty()
                .joinToString("; ") { it.substringBefore(";") }
            val merged = listOfNotNull(
                cookies?.takeIf { it.isNotBlank() },
                newCookies.takeIf { it.isNotBlank() }
            ).joinToString("; ")
            return body to merged
        } finally {
            conn.disconnect()
        }
    }
}
