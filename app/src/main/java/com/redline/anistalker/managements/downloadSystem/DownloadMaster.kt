package com.redline.anistalker.managements.downloadSystem

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
import com.redline.anistalker.utils.getSafeFloat
import com.redline.anistalker.utils.map
import com.redline.anistalker.utils.utilize
import org.json.JSONArray
import org.json.JSONObject
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
    override val track: AnimeTrack,
    override val quality: VideoQuality,
    private val links: List<DownloadTaskModel>,
    private val subtitle: String,
) : DownloadTask {

    constructor(
        fileName: String,
        episodeId: Int,
        duration: Float,
        track: AnimeTrack,
        quality: VideoQuality,
        size: Long,
        status: DownloadStatus,
        links: List<DownloadTaskModel>,
        subtitle: String,
    ): this(fileName, episodeId, duration, track, quality, links, subtitle) {
        _size = size
        _status = status
    }

    private var _status = DownloadStatus.PROCESSING
    private var _size: Long = 0L
    private var _downloadedSize = AtomicLong(0L)
    private var _downloadedDuration = AtomicDouble(0.0)
    private var _downloadSpeed = 0L

    private var _cancelSignal: CancellationSignal? = null
    private var _downloadPool: ExecutorService? = null
    private val statusListeners = mutableListOf<DownloadTask.(DownloadStatus) -> Unit>()

    override fun status() = _status

    override fun size() = _size

    override fun downloadedSize() = _downloadedSize.get()

    override fun downloadedDuration() = _downloadedDuration.get().toFloat()

    override fun downloadSpeed() = _downloadSpeed

    override fun activate() {
        statusChange(DownloadStatus.WAITING)
    }

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

        _downloadedDuration.set(0.0)
        _downloadSpeed = 0
        _downloadedSize.set(0L)

        cleanUp()
        statusChange(DownloadStatus.CANCELLED)
    }

    override fun onStatusChange(callback: DownloadTask.(DownloadStatus) -> Unit) {
        statusListeners.add(callback)
    }

    override fun removeStatusChangeListener(callback: DownloadTask.(DownloadStatus) -> Unit) {
        statusListeners.remove(callback)
    }

    override fun toString(): String {
        return JSONObject().apply {
            put("episodeId", episodeId)
            put("filename", fileName)
            put("duration", duration.toDouble())
            put("size", _size)
            put("status", _status.name)
            put("track", track.name)
            put("quality", quality.name)
            put("subtitle", subtitle)
            put("links", JSONArray().apply {
                links.forEach {
                    put(JSONObject().apply {
                        put("url", it.url)
                        put("isDownloaded", it.isDownloaded)
                        put("file", it.file)
                        put("length", it.length)
                        put("order", it.order)
                    })
                }
            })
        }.toString(4)
    }

    private fun statusChange(status: DownloadStatus) {
        _status = status
        statusListeners.forEach { it(_status) }
    }

    private fun run() {
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

            } ?: throw AniError(AniErrorCode.INVALID_VALUE)
        }

        _downloadPool?.shutdown()
        while (_downloadPool?.isTerminated != true) {
            try {
                _downloadPool?.awaitTermination(100, TimeUnit.MILLISECONDS)
            } catch (_: Exception) {
            }

            _downloadSpeed = downloadedPerUnit * 10
            downloadedPerUnit = 0

            statusChange(DownloadStatus.RUNNING)

            FileMaster.write(this)
        }

        if (!cancelSignal.isCanceled) {
            cancelSignal.cancel()
            statusChange(DownloadStatus.WRITING)
            _size = _downloadedSize.get()
            _downloadedSize.set(0L)

            FileMaster.segmentsIntoFile(links.map { it.file }, fileName) {
                _downloadedSize.addAndGet(it)
            }

            cleanUp()
            statusChange(DownloadStatus.COMPLETED)
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
        epId: Int,
        fileName: String,
        track: AnimeTrack,
        quality: VideoQuality
    ): DownloadTask {
        var video: VideoFile? = null
        var subtitle: Subtitle? = null
        do {
            try {
                val v = StalkMedia.Anime.getEpisodeLinks(epId, track, true)
                video =
                    if (quality == VideoQuality.HD) v.hd
                    else v.uhd
                subtitle = if (v.subtitle.isNotEmpty()) v.subtitle[0] else null
            } catch (ex: IOException) {
                ex.printStackTrace()
                break;
            }
        } while (video == null)

        return video?.let {

            val task = DownloadTaskImpl(
                "${quality.value}-${track.value}_$fileName",
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
        } ?: throw AniError(AniErrorCode.NOT_FOUND)
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
                track = AnimeTrack.valueOf(json.getString("track")),
                quality = VideoQuality.valueOf(json.getString("quality")),
                size = json.getLong("size"),
                status = DownloadStatus.valueOf(json.getString("status")),
                links = links,
                subtitle = json.getString("subtitle"),
            )
        }

        return list
    }
}