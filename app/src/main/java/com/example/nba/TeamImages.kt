package com.example.nba

object TeamImages {

    private const val CLOUD_NAME = "df7rg1mde"

    private data class Info(val publicId: String, val mascot: String)

    private val teams = mapOf(
        "Atlanta Hawks" to Info("hawks_yiz3wk", "Hawks"),
        "Boston Celtics" to Info("celtics_y9lq59", "Celtics"),
        "Brooklyn Nets" to Info("nets_bbshcd", "Nets"),
        "Charlotte Hornets" to Info("hornets_shwkv0", "Hornets"),
        "Chicago Bulls" to Info("bulls_re2ved", "Bulls"),
        "Cleveland Cavaliers" to Info("cavs_mlosch", "Cavaliers"),
        "Dallas Mavericks" to Info("mavs_oq3kwa", "Mavericks"),
        "Denver Nuggets" to Info("nuggets_txnvhz", "Nuggets"),
        "Detroit Pistons" to Info("pistons_hcg6bk", "Pistons"),
        "Golden State Warriors" to Info("warriors_b1bonm", "Warriors"),
        "Houston Rockets" to Info("rockets_wvdpts", "Rockets"),
        "Indiana Pacers" to Info("pacers_yidmuo", "Pacers"),
        "LA Clippers" to Info("clippers_hvfcdv", "Clippers"),
        "Los Angeles Clippers" to Info("clippers_hvfcdv", "Clippers"),
        "Los Angeles Lakers" to Info("lakers_ynkv8z", "Lakers"),
        "Memphis Grizzlies" to Info("grizzlies_zaditw", "Grizzlies"),
        "Miami Heat" to Info("miami_rjlox8", "Heat"),
        "Milwaukee Bucks" to Info("bucks_uvchia", "Bucks"),
        "Minnesota Timberwolves" to Info("timberwolves_e5yesr", "Timberwolves"),
        "New Orleans Pelicans" to Info("pelicans_u5iogs", "Pelicans"),
        "New York Knicks" to Info("knicks_vgi4tq", "Knicks"),
        "Oklahoma City Thunder" to Info("thunder_zs7krn", "Thunder"),
        "Orlando Magic" to Info("magic_wawz6n", "Magic"),
        "Philadelphia 76ers" to Info("sixers_dxkxvr", "76ers"),
        "Phoenix Suns" to Info("suns_ditmkj", "Suns"),
        "Portland Trail Blazers" to Info("trailblazers_ozvrqg", "Trail Blazers"),
        "Sacramento Kings" to Info("kings_suhhde", "Kings"),
        "San Antonio Spurs" to Info("spurs_bti943", "Spurs"),
        "Toronto Raptors" to Info("raptors_z7x2mt", "Raptors"),
        "Utah Jazz" to Info("jazz_lrnlqc", "Jazz"),
        "Washington Wizards" to Info("wizards_mfidj5", "Wizards"),
    )

    fun urlFor(teamName: String, width: Int = 1080, height: Int = 1350): String? {
        val info = teams[teamName] ?: return null
        return "https://res.cloudinary.com/$CLOUD_NAME/image/upload/" +
            "f_auto,q_auto,w_$width,h_$height,c_fill,g_auto/${info.publicId}"
    }

    fun mascot(teamName: String): String =
        teams[teamName]?.mascot ?: teamName.substringAfterLast(' ', teamName)
}
