package com.redline.anistalker.managements.downloadSystem

import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.VideoQuality

interface DownloadTask {
    companion object {
        const val ANIME_ID = "TASK_ANIME_ID"
        const val DOWNLOADED_DURATION = "TASK_DOWNLOADED_DURATION"
        const val DOWNLOADED_SIZE = "TASK_DOWNLOADED_SIZE"
        const val DOWNLOAD_SPEED = "DOWNLOAD_SPEED"
        const val STATUS = "TASK_STATUS"
        const val EPISODE_ID = "TASK_EPISODE_ID"
        const val DOWNLOAD_ID = "TASK_ID"
        const val SIZE = "TASK_SIZE"
        const val DURATION = "TASK_DURATION"
        const val FILENAME = "TASK_FILENAME"
        const val TRACK = "TASK_TRACK"
        const val QUALITY = "TASK_QUALITY"
    }

    val downloadId: Int
    val fileName: String
    val animeId: Int
    val episodeId: Int
    val duration: Float

    val quality: VideoQuality
    val track: AnimeTrack

    fun status(): DownloadStatus
    fun size(): Long
    fun downloadedSize(): Long
    fun downloadedDuration(): Float
    fun downloadSpeed(): Long

    fun activate()
    suspend fun start()
    fun stop()
    fun cancel()

    fun onStatusChange(callback: DownloadTask.(DownloadStatus) -> Unit)
    fun removeStatusChangeListener(callback: DownloadTask.(DownloadStatus) -> Unit)

    override fun toString(): String
}