package com.redline.anistalker.models

import com.redline.anistalker.utils.getSafeInt
import com.redline.anistalker.utils.getSafeString
import com.redline.anistalker.utils.map
import org.json.JSONArray
import org.json.JSONObject

private val monthCodes = arrayOf(
    "JAN", "FEB", "MAR", "APR", "JUN", "JUL", "AUG", "SEPT", "OCT", "NOV", "DEC"
)

// ========================
// Anime Enums
// ========================
// region Enums
enum class AnimeStatus(val value: String, val index: Int) {
    ALL("ALL", 0),
    COMPLETED("COMPLETED", 1),
    AIRING("AIRING", 2)
}

enum class AnimeType(val value: String, val index: Int) {
    ALL("ALL", 0),
    TV("TV", 1),
    MOVIE("MOVIE", 2),
    OVA("OVA", 3),
    ONA("ONA", 4),
    SPECIAL("SPECIAL", 5),

}

enum class AnimeSeason(val value: String, val index: Int) {
    ALL("ALL", 0),
    SPRING("SPRING", 1),
    SUMMER("SUMMER", 2),
    FALL("FALL", 3),
    WINTER("WINTER", 4),
}

enum class AnimeSort(val value: String, val index: Int) {
    DEFAULT("default", 0),
    RECENTLY_ADDED("recently_added", 1),
    RECENTLY_UPDATED("recently_updated", 2),
    SCORE("score", 3),
    NAME("name_az", 4),
    RELEASED_DATE("release_date", 5),
    MOST_WATCHED("most_watched", 6)
}

enum class AnimeScore(val value: String, val index: Int) {
    ALL("all", 0),
    APPEALING("appealing", 1),
    HORRIBLE("horrible", 2),
    VERY_BAD("very_bad", 3),
    BAD("bad", 4),
    AVERAGE("average", 5),
    FINE("fine", 6),
    GOOD("good", 7),
    VERY_GOOD("very_good", 8),
    GREAT("great", 9),
    MASTERPIECE("masterpiece", 10),
}

enum class AnimeCategory(val value: String, val label: String) {
    TOP_AIRING("top-airing", "Top Airing"),
    MOST_POPULAR("most-popular", "Most Popular"),
    MOST_FAVORITE("most-favorite", "Most Favorite"),
    COMPLETED("completed", "Completed"),
    RECENTLY_UPDATED("recently-updated", "Recents"),
}

enum class AnimeTrack(val value: String, val index: Int) {
    ALL("ALL", 0),
    SUB("SUB", 1),
    DUB("DUB", 2)
}


enum class WatchlistPrivacy(val value: String, val index: Int) {
    PUBLIC("PUBLIC", 0),
    SHARED("SHARED", 1),
    PRIVATE("PRIVATE", 2)
}
// endregion

// =========================
// Helping Data Objects
// =========================
// region Helping Data Objects
data class AnimeId(val zoroId: Int = 0, val anilistId: Int = 0, val malId: Int = 0)

data class AnimeTitle(val english: String = "English Title", val userPreferred: String = "UserPreferred Title")

data class AnimeEpisode(val sub: Int = 0, val dub: Int = 0, val total: Int = 0)

data class AnimeDate(val date: Int = 0, val month: Int = 0, val year: Int = 0) {
    fun toDateString(): String {
        return "$date ${ monthCodes[month % 12] } $year"
    }

    fun isValid(): Boolean {
        return !(date == 0 && month == 0 && year == 0)
    }
}

data class AnimeRelation(
    val zoroId: Int = 0,
    val image: String = "",
    val title: String = "Anime Relation"
)
// endregion


data class AnimeCard(
    val id: Int = 0,
    val name: AnimeTitle = AnimeTitle(),
    val image: String = "",
    val status: AnimeStatus = AnimeStatus.ALL,
    val type: AnimeType = AnimeType.ALL,
    val episodes: AnimeEpisode = AnimeEpisode(),
    val isAdult: Boolean = false,
    val year: Int = 0,
    val owner: String = "Owner here"
)

data class Anime(
    val id: AnimeId = AnimeId(),
    val title: AnimeTitle = AnimeTitle(),
    val type: AnimeType = AnimeType.ALL,
    val year: Int = 0,
    val end: AnimeDate = AnimeDate(),
    val season: AnimeSeason = AnimeSeason.ALL,
    val status: AnimeStatus = AnimeStatus.ALL,
    val image: String = "",
    val description: String = "",
    val otherNames: List<String> = listOf(),
    val episodes: AnimeEpisode = AnimeEpisode(),
    val relations: List<AnimeRelation> = listOf(),
    val isAdult: Boolean = false,
    val genres: List<String> = listOf()
) {
    val relation: String = relations.find {
        it.zoroId == id.zoroId
    }?.title ?: "Main"

    companion object {
        fun getRelationCode(relation: String) {
            val rel = relation.lowercase()
            if (rel.contains("season"))
                "S${rel.replace("season ", "")}"
            else rel.uppercase()
        }
        fun toAnime(value: String): Anime {
            val json = JSONObject(value)
            return Anime(
                id = json.getJSONObject("id").toAnimeId(),
                title = json.getJSONObject("title").toAnimeTitle(),
                type = AnimeType.valueOf(json.getString("type")),
                year = json.getInt("year"),
                end = json.getJSONObject("end").toAnimeDate(),
                season = AnimeSeason.valueOf(json.getString("season")),
                status = AnimeStatus.valueOf(json.getString("status")),
                image = json.getString("image"),
                description = json.getString("desc"),
                otherNames = json.getJSONArray("otherNames").map { getString(it) },
                episodes = json.getJSONObject("episodes").toEpisodes(),
                relations = json.getJSONArray("relations").map { getJSONObject(it).toAnimeRelation() },
                isAdult = json.getBoolean("isAdult"),
                genres = json.getJSONArray("genres").map { getString(it) }
            )
        }
    }


    override fun toString(): String {
        return JSONObject().apply {
            put("id", id.toJSON())
            put("title", title.toJSON())
            put("type", type.name)
            put("year", year)
            put("end", end.toJSON())
            put("season", season.name)
            put("status", status.name)
            put("image", image)
            put("desc", description)
            put("otherNames", JSONArray().apply {
                otherNames.forEach { put(it) }
            })
            put("episodes", episodes.toJSON())
            put("relations", JSONArray().apply {
                relations.forEach { put(it.toJSON()) }
            })
            put("isAdult", isAdult)
            put("genres", JSONArray().apply { genres.forEach { put(it) } })
        }.toString()
    }
}

data class AnimeSpotlight(
    val id: Int = 0,
    val title: AnimeTitle = AnimeTitle(),
    val image: String = "",
    val episodes: AnimeEpisode = AnimeEpisode(),
    val type: AnimeType = AnimeType.ALL,
    val rank: Int = 0,
)

data class AnimeEpisodeDetail(
    val id: Int = 0,
    val episode: Int = 0,
    val title: String = "Episode Title",
    val isFiller: Boolean = false
)

data class Watchlist (
    val id: Int = 0,
    val title: String = "Watchlist title",
    val image: String = "",
    val privacy: WatchlistPrivacy = WatchlistPrivacy.PRIVATE,
    val owner: String = "Owner here",
    val provider: String = "Provider here",
    val series: List<Int> = emptyList(),
    val following: Int = 0
)

data class AnimeSearchFilter(
    val sort: AnimeSort = AnimeSort.DEFAULT,
    val score: AnimeScore = AnimeScore.ALL,
    val type: AnimeType = AnimeType.ALL,
    val track: AnimeTrack = AnimeTrack.ALL,
)

interface IMediaPage<T> {
    fun isLoading(): Boolean
    fun page(): Int
    fun hasNextPage(): Boolean
    suspend fun nextPage(): List<T>
}

fun AnimeEpisode.toJSON(): JSONObject {
    return JSONObject().apply {
        put("total", total)
        put("sub", sub)
        put("dub", dub)
    }
}

fun JSONObject.toEpisodes(): AnimeEpisode {
    return AnimeEpisode(getInt("sub"), getInt("dub"), getInt("total"))
}

fun AnimeId.toJSON(): JSONObject {
    return JSONObject().apply {
        put("zoroId", zoroId)
        put("aniId", anilistId)
        put("malId", malId)
    }
}

fun JSONObject.toAnimeId(): AnimeId {
    return AnimeId(
        zoroId = getInt("zoroId"),
        anilistId = getInt("aniId"),
        malId = getInt("malId")
    )
}

fun AnimeTitle.toJSON(): JSONObject {
    return JSONObject().apply {
        put("english", english)
        put("userPreferred", userPreferred)
    }
}

fun JSONObject.toAnimeTitle(): AnimeTitle {
    return AnimeTitle(
        english = getSafeString("english", ""),
        userPreferred = getSafeString("userPreferred", "")
    )
}

fun AnimeDate.toJSON(): JSONObject {
    return JSONObject().apply {
        put("date", date)
        put("month", month)
        put("year", year)
    }
}

fun JSONObject.toAnimeDate(): AnimeDate {
    return AnimeDate(
        date = getSafeInt("date"),
        month = getSafeInt("month"),
        year = getSafeInt("year")
    )
}

fun AnimeRelation.toJSON(): JSONObject {
    return JSONObject().apply {
        put("id", zoroId)
        put("image", image)
        put("relation", title)
    }
}

fun JSONObject.toAnimeRelation(): AnimeRelation {
    return AnimeRelation(
        zoroId = getSafeInt("id"),
        image = getSafeString("image"),
        title = getSafeString("relation")
    )
}