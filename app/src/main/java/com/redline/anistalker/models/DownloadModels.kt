package com.redline.anistalker.models


enum class DownloadStatus(val index: Int) {
    RUNNING(0),
    PAUSED(1),
    CANCELLED(2),
    WAITING(3),
    NETWORK_WAITING(4),
}

data class AnimeShort(
    val id: AnimeId = AnimeId(),
    val title: String = "Anime Short Title",
    val relation: String = "Anime Relation"
)

data class EpisodeDownload(
    val id: Int = 0,
    val animeId: AnimeId = AnimeId(),
    val title: String = "Episode Title",
    val num: Int = 0,
    val quality: VideoQuality = VideoQuality.HD,
    val language: AnimeTrack = AnimeTrack.ALL,
    val intro: VideoRange = VideoRange(),
    val outro: VideoRange = VideoRange(),
    val duration: Float = 0f,
    val size: Long = 0L
)

data class OngoingEpisodeDownload(
    val id: Int = 0,
    val status: DownloadStatus = DownloadStatus.WAITING,
    val downloadedDuration: Float = 0f,
    val downloadedSize: Long = 0L,
    val downloadSpeed: Long = 0L,
)

data class AnimeDownload (
    val dId: Int = 0,
    val title: String = "Anime Download Title",
    val images: List<String> = listOf(),
    val episodes: AnimeEpisode = AnimeEpisode(),
    val year: Int = 0,
    val anime: List<AnimeShort> = listOf(),
    val content: List<EpisodeDownload> = listOf(),
    val ongoingContent: List<EpisodeDownload> = listOf()
)

data class MangaDownload (
    val id: String = "",
    val title: String = "Manga Title",
    val image: String = "",
    val chapters: List<MangaChapter> = listOf(),
    val downloadableChapters: List<String> = listOf()
)

data class MangaDownloadContent (
    val id: String = "",
    val downloaded: Boolean = false,
    val totalPages: Int = 0,
    val downloadedPages: Int = 0,
    val status: DownloadStatus = DownloadStatus.WAITING,
)