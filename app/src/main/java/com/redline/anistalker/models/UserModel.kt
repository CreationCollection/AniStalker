package com.redline.anistalker.models


enum class EventType(val index: Int) {
    ALL(0),
    NEW_EPISODE(1),
    NEW_CHAPTER(2),
    ANIME_COMPLETE(3),
    MANGA_COMPLETE(4)
}

data class Event<T> (
    val id: Int = 0,
    val type: EventType = EventType.ALL,
    val image: String = "",
    val content: T,
    val contentId: T
)

data class HistoryEntry (
    val contentEvent: Boolean = false,
    val completionEvent: Boolean = false,
    val lastContent: Int = 0
)