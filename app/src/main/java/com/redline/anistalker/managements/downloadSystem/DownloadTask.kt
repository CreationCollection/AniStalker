package com.redline.anistalker.managements.downloadSystem

import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.VideoQuality

interface DownloadTask {
    companion object {
        val DOWNLOADED_DURATION = "TASK_DOWNLOADED_DURATION"
        val DOWNLOADED_SIZE = "TASK_DOWNLOADED_SIZE"
        val DOWNLOAD_SPEED = "DOWNLOAD_SPEED"
        val STATUS = "TASK_STATUS"
        val EPISODE_ID = "TASK_EPISODE_ID"
        val SIZE = "TASK_SIZE"
        val DURATION = "TASK_DURATION"
        val FILENAME = "TASK_FILENAME"
        val TRACK = "TASK_TRACK"
        val QUALITY = "TASK_QUALITY"
    }

    val fileName: String
    val episodeId: Int
    val duration: Float

    val quality: VideoQuality
    val track: AnimeTrack

    fun status(): DownloadStatus
    fun size(): Long
    fun downloadedSize(): Long
    fun downloadedDuration(): Float
    fun downloadSpeed(): Long

    fun wait()
    fun start()
    fun stop()
    fun cancel()

    fun onStatusChange(callback: DownloadTask.(DownloadStatus) -> Unit)
    fun removeStatusChangeListener(callback: DownloadTask.(DownloadStatus) -> Unit)

    override fun toString(): String
}