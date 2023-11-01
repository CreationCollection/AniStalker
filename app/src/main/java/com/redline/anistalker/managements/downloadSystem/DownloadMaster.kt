package com.redline.anistalker.managements.downloadSystem

import androidx.core.os.CancellationSignal
import com.google.common.util.concurrent.AtomicDouble
import com.redline.anistalker.managements.FileMaster
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.downloadSystem.DownloadTask
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.Subtitle
import com.redline.anistalker.models.Video
import com.redline.anistalker.models.VideoFile
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.utils.getSafeFloat
import com.redline.anistalker.utils.map
import com.redline.anistalker.utils.utilize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

data class DownloadTaskModel(
    val url: String,
    val isDownloaded: Boolean,
    val length: Float,
    val file: String,
    val order: Int
)

class DownloadTaskImpl(
    override val fileName: String,
    override val episodeId: Int,
    override val duration: Float,
    private val links: List<DownloadTaskModel>,
    private val subtitle: String,
    private val workerThread: ExecutorService,
) : DownloadTask {
    private var _status = DownloadStatus.PROCESSING
    private var _downloadedSize = AtomicLong(0L)
    private var _downloadedDuration = AtomicDouble(0.0)
    private var _downloadSpeed = 0L

    private var _cancelSignal: CancellationSignal? = null
    private var _downloadPool: ExecutorService? = null
    private val statusListeners = mutableListOf<(DownloadStatus) -> Unit>()

    override fun status() = _status

    override fun downloadedSize() = _downloadedSize.get()

    override fun downloadedDuration() = _downloadedDuration.get().toFloat()

    override fun downloadSpeed() = _downloadSpeed

    override fun start() {
        statusChange(DownloadStatus.WAITING)
        _cancelSignal = CancellationSignal()
        run()
    }

    override fun stop() {
        _cancelSignal?.cancel()
        _cancelSignal = null
        statusChange(DownloadStatus.PAUSED)
        _downloadSpeed = 0
    }

    override fun cancel() {
        _cancelSignal?.cancel()
        _cancelSignal = null

        statusChange(DownloadStatus.CANCELLED)

        _downloadedDuration.set(0.0)
        _downloadSpeed = 0
        _downloadedSize.set(0L)

        FileMaster.deleteDownloadSegments(links.map { it.file })
    }

    override fun restart() {
        cancel()
        start()
    }

    override fun onStatusChange(callback: (DownloadStatus) -> Unit) {
        statusListeners.add(callback)
    }

    override fun removeStatusChangeListener(callback: (DownloadStatus) -> Unit) {
        statusListeners.remove(callback)
    }

    override fun toString(): String {
        return JSONObject().apply {
            put("filename", fileName)
        }.toString(4)
    }

    private fun statusChange(status: DownloadStatus) {
        _status = status
        statusListeners.forEach { it(_status) }
    }

    private fun run() {
        workerThread.execute {
            _downloadPool = Executors.newFixedThreadPool(4)
            val cancelSignal = _cancelSignal ?: CancellationSignal()
            var downloadedPerUnit = 0L

            statusChange(DownloadStatus.RUNNING)
            links.forEach {
                if (!it.isDownloaded) _downloadPool?.execute {
                    if (!cancelSignal.isCanceled) {
                        try {
                            val stream = Net.getStream(it.url)
                            FileMaster.writeDownloadSegment(it.file, stream, cancelSignal) { len ->
                                downloadedPerUnit += len
                                _downloadedSize.addAndGet(len)
                            }
                            _downloadedDuration.addAndGet(it.length.toDouble())
                        } catch (ex: AniError) {
                            ex.printStackTrace()
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }
                    }

                }
            }

            _downloadPool?.shutdown()
            while (_downloadPool?.isTerminated != true) {
                try {
                    _downloadPool?.awaitTermination(100, TimeUnit.MILLISECONDS)
                } catch (_: Exception) {
                }

                _downloadSpeed = downloadedPerUnit * 10
                downloadedPerUnit = 0
            }

            if (!cancelSignal.isCanceled) {
                cancelSignal.cancel()
                statusChange(DownloadStatus.WRITING)
                _downloadedSize.set(0L)

                FileMaster.segmentsIntoFile(links.map { it.file }, fileName) {
                    _downloadedSize.addAndGet(it)
                }

                FileMaster.deleteDownloadSegments(links.map { it.file })

                statusChange(DownloadStatus.COMPLETED)
            }
        }
    }
}

object DownloadMaster {
    private val threadCount = Runtime.getRuntime().availableProcessors()
    private val processingPool = Executors.newFixedThreadPool(threadCount)
    private val downloadPool = Executors.newFixedThreadPool(threadCount)

    fun download(
        epId: Int,
        fileName: String,
        track: AnimeTrack,
        quality: VideoQuality
    ): AniResult<DownloadTask> {
        val result = AniResult<DownloadTask>()

        processingPool.execute {
            var video: VideoFile? = null
            var subtitle: Subtitle? = null
            do {
                try {
                    runBlocking {
                        val v = StalkMedia.Anime.getEpisodeLinks(epId, track, true)
                        video =
                            if (quality == VideoQuality.HD) v.hd
                            else v.uhd
                        subtitle = v.subtitle[0]
                    }
                }
                catch (ex: IOException) {
                    ex.printStackTrace()
                    break;
                }
            } while (video == null)

            video?.let {

                val task = DownloadTaskImpl(
                    "${quality.value}-${track.value}_$fileName",
                    episodeId = epId,
                    duration = it.files.utilize(0f) { i, r -> r + i.length },
                    links = it.files.mapIndexed() { i, x ->
                        DownloadTaskModel(
                            url = x.url,
                            isDownloaded = false,
                            length = x.length,
                            file = UUID.randomUUID().toString(),
                            order = i,
                        )
                    },
                    subtitle = subtitle?.url ?: "",
                    workerThread = downloadPool
                )

                result.pass(task)
            }
        }

        return result
    }

    fun restoreDownloads(): List<DownloadTask> {
        val list = mutableListOf<DownloadTask>()
        FileMaster.readAllDownloadSources {
            val json = JSONObject(it)
            val links = json.getJSONArray("links").map { index ->
                val obj = getJSONObject(index)
                DownloadTaskModel(
                    url = obj.getString("url"),
                    isDownloaded = obj.getBoolean("isDownloaded"),
                    length = obj.getSafeFloat("length"),
                    file = obj.getString("file"),
                    order = obj.getInt("order"),
                )
            }

            list += DownloadTaskImpl(
                fileName = json.getString("fileName"),
                episodeId = json.getInt("epId"),
                duration = json.getDouble("duration").toFloat(),
                links = links,
                subtitle = json.getString("subtitle"),
                workerThread = downloadPool
            )
        }

        return list
    }
}