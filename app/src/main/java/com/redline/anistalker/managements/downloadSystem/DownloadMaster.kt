package com.redline.anistalker.managements.downloadSystem

import android.util.Log
import androidx.core.os.CancellationSignal
import com.google.common.util.concurrent.AtomicDouble
import com.redline.anistalker.managements.FileMaster
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.Subtitle
import com.redline.anistalker.models.VideoFile
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.utils.ExecutionFlow
import com.redline.anistalker.utils.getSafeFloat
import com.redline.anistalker.utils.map
import com.redline.anistalker.utils.utilize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import okhttp3.internal.closeQuietly
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

private const val DOWNLOAD_TASK_SUBTITLE = "TASK_SUBTITLE"
private const val DOWNLOAD_TASK_LINKS = "TASK_LINKS"

data class DownloadTaskModel(
    val url: String,
    var isDownloaded: Boolean,
    val length: Float,
    val file: String,
    val order: Int
) {
    companion object {
        const val URL = "LINK_URL"
        const val DOWNLOADED = "LINK_DOWNLOADED"
        const val LENGTH = "LINK_LENGTH"
        const val FILE = "LINK_FILE"
        const val ORDER = "LINK_ORDER"
    }
}

class DownloadTaskImpl(
    override val downloadId: Int,
    override val fileName: String,
    override val animeId: Int,
    override val episodeId: Int,
    override val duration: Float,
    override val track: AnimeTrack,
    override val quality: VideoQuality,
    private val links: List<DownloadTaskModel>,
    private val subtitle: String,
) : DownloadTask {

    constructor(
        id: Int,
        fileName: String,
        animeId: Int,
        episodeId: Int,
        duration: Float,
        track: AnimeTrack,
        quality: VideoQuality,
        status: DownloadStatus,
        links: List<DownloadTaskModel>,
        subtitle: String,
    ) : this(id, fileName, animeId, episodeId, duration, track, quality, links, subtitle) {
        _status = status

        links.forEach {
            if (it.isDownloaded) {
                val length = FileMaster.getDownloadSegmentSize(it.file)
                _downloadedSize.addAndGet(length)
                _size += length
                _downloadedDuration.addAndGet(it.length.toDouble())
            }
        }
    }

    private val concurrentDownloads = 8
    private val updateFlow = ExecutionFlow(1, CoroutineScope(Dispatchers.IO))
    private val speedMonitor = SpeedMonitor()

    private var _status = DownloadStatus.PROCESSING
    private var _size: Long = 0L
    private var _downloadedSize = AtomicLong(0L)
    private var _downloadedDuration = AtomicDouble(0.0)
    private var _downloadSpeed = AtomicLong(0)

    private var _cancelSignal: CancellationSignal? = null
    private var _downloadFlow: ExecutionFlow? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val statusListeners = mutableListOf<DownloadTask.(DownloadStatus) -> Unit>()

    override fun status() = _status

    override fun size() = _size

    override fun downloadedSize() = _downloadedSize.get()

    override fun downloadedDuration() = _downloadedDuration.get().toFloat()

    override fun downloadSpeed() = _downloadSpeed.get()

    override fun activate() {
        statusChange(DownloadStatus.WAITING)
    }

    override suspend fun start() {
        if (_cancelSignal != null) return

        statusChange(DownloadStatus.WAITING)
        _cancelSignal = CancellationSignal()
        run()
    }

    override fun stop() {
        _cancelSignal?.cancel()
        _cancelSignal = null
        statusChange(DownloadStatus.PAUSED)
        _downloadSpeed.set(0)
    }

    override fun cancel() {
        _cancelSignal?.cancel()
        _cancelSignal = null

        _downloadedDuration.set(0.0)
        _downloadSpeed.set(0)
        _downloadedSize.set(0L)

        statusChange(DownloadStatus.CANCELLED)
        cleanUp()
    }

    override fun onStatusChange(callback: DownloadTask.(DownloadStatus) -> Unit) {
        statusListeners.add(callback)
    }

    override fun removeStatusChangeListener(callback: DownloadTask.(DownloadStatus) -> Unit) {
        statusListeners.remove(callback)
    }

    override fun toString(): String {
        return JSONObject().apply {
            put(DownloadTask.DOWNLOAD_ID, downloadId)
            put(DownloadTask.ANIME_ID, animeId)
            put(DownloadTask.EPISODE_ID, episodeId)
            put(DownloadTask.FILENAME, fileName)
            put(DownloadTask.DURATION, duration.toDouble())
            put(DownloadTask.SIZE, _size)
            put(DownloadTask.STATUS, _status.name)
            put(DownloadTask.TRACK, track.name)
            put(DownloadTask.QUALITY, quality.name)
            put(DOWNLOAD_TASK_SUBTITLE, subtitle)
            put(DOWNLOAD_TASK_LINKS, JSONArray().apply {
                links.forEach {
                    put(JSONObject().apply {
                        put(DownloadTaskModel.URL, it.url)
                        put(DownloadTaskModel.DOWNLOADED, it.isDownloaded)
                        put(DownloadTaskModel.FILE, it.file)
                        put(DownloadTaskModel.LENGTH, it.length)
                        put(DownloadTaskModel.ORDER, it.order)
                    })
                }
            })
        }.toString(4)
    }

    private fun statusChange(status: DownloadStatus) {
        _status = status
        statusListeners.forEach { it(_status) }
        updateFlow.execute {
            FileMaster.write(this@DownloadTaskImpl)
        }
    }

    private suspend fun run() {
        _downloadFlow = ExecutionFlow(concurrentDownloads, scope)
        val cancelSignal = _cancelSignal ?: CancellationSignal()

        cancelSignal.setOnCancelListener {
            _downloadFlow?.reset()
            speedMonitor.reset()
        }
        links.forEach {
            if (!it.isDownloaded) _downloadFlow?.execute {
                try {

                    if (!cancelSignal.isCanceled) downloadLink(it, cancelSignal)

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    cancelSignal.cancel()
                }
            } ?: throw AniError(AniErrorCode.INVALID_VALUE)
        }

        monitorDownloading(cancelSignal)
        finalizeDownload(cancelSignal)
    }

    private fun downloadLink(link: DownloadTaskModel, cancelSignal: CancellationSignal) {
        do {
            try {
                var length = 0L
                val stream = Net.getStream(link.url) { l -> length = l }
                FileMaster.writeDownloadSegment(
                    link.file,
                    stream,
                    cancelSignal
                ) { len ->
                    _downloadedSize.addAndGet(len)
                    if (length > 0) {
                        val value = (len / (length * 1.0)) * link.length
                        _downloadedDuration.addAndGet(value)
                    }
                }
                stream.closeQuietly()
                if (cancelSignal.isCanceled) break

                if (length <= 0L) _downloadedDuration.addAndGet(link.length.toDouble())
                link.isDownloaded = true
            } catch (ex: AniError) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
                break
            }
        } while (!cancelSignal.isCanceled && !link.isDownloaded)
    }

    private suspend fun monitorDownloading(cancelSignal: CancellationSignal) {
        var lastDownloadedBytes = _downloadedSize.get()
        val tick = 200L
        while (_downloadFlow?.isEmpty() != true && !cancelSignal.isCanceled) {
            delay(tick)

            val bytes = _downloadedSize.get() - lastDownloadedBytes
            lastDownloadedBytes = _downloadedSize.get()

            speedMonitor.consumeBytes(bytes / (tick / 1000))
            _downloadSpeed.set(speedMonitor.get())

            if (!cancelSignal.isCanceled) statusChange(DownloadStatus.RUNNING)
        }
        speedMonitor.reset()
    }

    private suspend fun finalizeDownload(cancelSignal: CancellationSignal) {
        if (!cancelSignal.isCanceled) {
            cancelSignal.cancel()
            statusChange(DownloadStatus.WRITING)
            _size = _downloadedSize.get()
            _downloadedSize.set(0L)

            try {
                _downloadFlow?.execute {
                    FileMaster.segmentsIntoFile(links.map { it.file }, fileName) {
                        _downloadedSize.addAndGet(it)
                    }
                }

                while (_downloadFlow?.isEmpty() != true) {
                    delay(200)
                    statusChange(DownloadStatus.WRITING)
                }

                statusChange(DownloadStatus.COMPLETED)
                cleanUp()
            } catch (err: AniError) {
                Log.e("DownloadTask $episodeId", err.message ?: err.errorCode.message)
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
    }

    private fun cleanUp() {
        FileMaster.deleteDownloadSegments(links.map { it.file })
        FileMaster.delete(this)

        statusListeners.clear()
    }
}

object DownloadMaster {

    suspend fun download(
        downloadId: Int,
        animeId: Int,
        epId: Int,
        fileName: String,
        track: AnimeTrack,
        quality: VideoQuality
    ): DownloadTask? {
        var video: VideoFile? = null
        var subtitle: Subtitle? = null
        var offset = 1L
        do try {
            val v = StalkMedia.Anime.getEpisodeLinks(epId, track, true)
            video =
                if (quality == VideoQuality.HD) v.hd
                else v.uhd
            subtitle = if (v.subtitle.isNotEmpty()) v.subtitle[0] else null
            break
        } catch (err: AniError) {
            if (err.errorCode == AniErrorCode.NOT_FOUND) {
                return null
            }

            err.printStackTrace()
            delay(1000 * offset)
            offset = (offset + 1).coerceAtMost(10)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        } while (true)

        return video?.let {

            val task = DownloadTaskImpl(
                downloadId,
                fileName,
                animeId = animeId,
                episodeId = epId,
                duration = it.files.utilize(0f) { i, r -> r + i.length },
                track,
                quality,
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
            )

            task
        }
    }

    fun restoreDownloads(): List<DownloadTask> {
        val list = mutableListOf<DownloadTask>()
        FileMaster.readAllDownloadSources {
            val json = JSONObject(it)
            val links = json.getJSONArray(DOWNLOAD_TASK_LINKS).map { index ->
                val obj = getJSONObject(index)
                DownloadTaskModel(
                    url = obj.getString(DownloadTaskModel.URL),
                    isDownloaded = obj.getBoolean(DownloadTaskModel.DOWNLOADED),
                    length = obj.getSafeFloat(DownloadTaskModel.LENGTH),
                    file = obj.getString(DownloadTaskModel.FILE),
                    order = obj.getInt(DownloadTaskModel.ORDER),
                )
            }

            list += DownloadTaskImpl(
                id = json.getInt(DownloadTask.DOWNLOAD_ID),
                fileName = json.getString(DownloadTask.FILENAME),
                animeId = json.getInt(DownloadTask.ANIME_ID),
                episodeId = json.getInt(DownloadTask.EPISODE_ID),
                duration = json.getDouble(DownloadTask.DURATION).toFloat(),
                track = AnimeTrack.valueOf(json.getString(DownloadTask.TRACK)),
                quality = VideoQuality.valueOf(json.getString(DownloadTask.QUALITY)),
                status = DownloadStatus.valueOf(json.getString(DownloadTask.STATUS)),
                links = links,
                subtitle = json.getString(DOWNLOAD_TASK_SUBTITLE),
            )
        }

        return list
    }
}