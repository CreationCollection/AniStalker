package com.redline.anistalker.models


enum class EventType(val index: Int) {
    ALL(0),
    NEW_EPISODE(1),
    NEW_CHAPTER(2),
    ANIME_COMPLETE(3),
//    MANGA_COMPLETE(4)
}

sealed class Event(
    var id: Int = 0,
    var type: EventType = EventType.ALL,
    var image: String = "",
    var title: String = "Event Title",
) {
    data class AnimeEvent(
        var animeId: AnimeId = AnimeId(),
        var episodeNum: Int = 0,
        var episodeId: Int = 0,
    ) : Event()

//    data class MangaEvent(
//        var mangaId: String = "",
//        var chapterNum: Int = 0,
//        var chapterId: String = "",
//    ) : Event()

    val heading: String = when (type) {
        EventType.ALL -> "General Event Triggered"
        EventType.NEW_EPISODE -> "New Episode Released"
        EventType.NEW_CHAPTER -> "New Chapter Released"
        EventType.ANIME_COMPLETE -> "Anime Completed"
//        EventType.MANGA_COMPLETE -> "Manga Completed"
    }
}

data class HistoryEntry(
    val contentEvent: Boolean = false,
    val completionEvent: Boolean = false,
    val lastContent: Int = 0
)