package com.redline.anistalker.managements.downloadSystem

import com.redline.anistalker.models.DownloadStatus

interface DownloadTask {
    val fileName: String
    val episodeId: Int
    val duration: Float

    fun status(): DownloadStatus
    fun downloadedSize(): Long
    fun downloadedDuration(): Float
    fun downloadSpeed(): Long

    fun start()
    fun stop()
    fun cancel()
    fun restart()

    fun onStatusChange(callback: (DownloadStatus) -> Unit)
    fun removeStatusChangeListener(callback: (DownloadStatus) -> Unit)

    override fun toString(): String
}