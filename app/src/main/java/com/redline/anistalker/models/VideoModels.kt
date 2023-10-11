package com.redline.anistalker.models

enum class VideoQuality(val value: String, val index: Int) { HD("HD", 1), UHD("UHD", 2) }
data class VideoRange ( val start: Int = 0, val end: Int = 0)

data class VideoFile(
    val master: String = "",
    val files: List<VideoSegment> = listOf()
)

data class VideoSegment (
    val length: Float = 0f,
    val at: Float = 0f,
    val url: String = "",
    val file: String = ""
)

data class Subtitle (
    val lang: String = "",
    val url: String = ""
)

data class Video (
    val intro: VideoRange = VideoRange(),
    val outro: VideoRange = VideoRange(),
    val track: AnimeTrack = AnimeTrack.ALL,
    val subtitle: List<Subtitle> = listOf(),
    val hd: VideoFile = VideoFile(),
    val uhd: VideoFile = VideoFile()
)