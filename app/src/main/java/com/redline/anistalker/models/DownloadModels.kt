package com.redline.anistalker.models


enum class DownloadStatus(val index: Int) {
    PROCESSING(-1),
    RUNNING(0),
    PAUSED(1),
    CANCELLED(2),
    WAITING(3),
    NETWORK_WAITING(4),
    WRITING(5),
    COMPLETED(10)
}

data class AnimeShort(
    val id: AnimeId = AnimeId(),
    val title: String = "Anime Short Title",
    val relation: String = "Anime Relation"
)

data class AnimeDownloadContentInfo(
    val series: Int = 0,
    val episodes: Int = 0,
    val duration: Float = 0f,
    val size: Long = 0
)

data class EpisodeRange (val start: Int = 0, val end: Int = 0)

data class EpisodeDownload(
    val id: Int = 0,
    val animeId: AnimeId = AnimeId(),
    val title: String = "Episode Title",
    val num: Int = 0,
    val relation: String = "Season 1",
    val quality: VideoQuality = VideoQuality.HD,
    val language: AnimeTrack = AnimeTrack.ALL,
    val intro: VideoRange = VideoRange(),
    val outro: VideoRange = VideoRange(),
    val duration: Float = 0f,
    val size: Long = 0L
)

data class OngoingEpisodeDownload(
    val id: Int = 0,
    val num: Int = 0,
    val status: DownloadStatus = DownloadStatus.WAITING,
    val duration: Float = 0f,
    val downloadedDuration: Float = 0f,
    val downloadedSize: Long = 0L,
    val downloadSpeed: Long = 0L,
)

//data class AnimeDownload (
//    val dId: Int = 0,
//    val title: String = "Anime Download Title",
//    val images: List<String> = emptyList(),
//    val episodes: AnimeEpisode = AnimeEpisode(),
//    val year: Int = 0,
//    val anime: List<AnimeShort> = emptyList(),
//    val downloadStats: AnimeDownloadContentInfo = AnimeDownloadContentInfo(),
//    val content: Map<Int, List<Int>> = emptyMap(),
//    val ongoingContent: List<Int> = emptyList()
//)

data class AnimeDownload (
    val animeId: AnimeId = AnimeId(),
    val title: String = "",
    val image: String = "",
    val episodes: AnimeEpisode = AnimeEpisode(),
    val type: AnimeType = AnimeType.ALL,
    val duration: Float = 0f,
    val size: Long = 0,
    val content: List<Int> = emptyList(),
    val ongoingContent: List<Int> = emptyList(),
)



//data class MangaDownload (
//    val id: String = "",
//    val title: String = "Manga Title",
//    val image: String = "",
//    val chapters: List<MangaChapter> = listOf(),
//    val downloadableChapters: List<String> = listOf(),
//    val downloadedChapters: List<String> = listOf(),
//)
//
//data class MangaDownloadContent (
//    val id: String = "",
//    val downloaded: Boolean = false,
//    val totalPages: Int = 0,
//    val downloadedPages: Int = 0,
//    val status: DownloadStatus = DownloadStatus.WAITING,
//)