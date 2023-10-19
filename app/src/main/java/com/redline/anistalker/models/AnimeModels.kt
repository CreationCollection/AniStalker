package com.redline.anistalker.models

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

enum class AnimeCategory(val value: String) {
    TOP_AIRING("top-airing"),
    MOST_POPULAR("most-popular"),
    MOST_FAVORITE("most-favorite"),
    COMPLETED("completed"),
    RECENTLY_UPDATED("recently-updated"),
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

data class AnimeDate(val date: Int = 0, val month: Int = 0, val year: Int = 0)

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