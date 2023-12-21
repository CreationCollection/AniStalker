package com.redline.anistalker.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.redline.anistalker.R
import com.redline.anistalker.managements.FileMaster
import com.redline.anistalker.managements.downloadSystem.DownloadMaster
import com.redline.anistalker.managements.downloadSystem.DownloadTask
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.utils.ExecutionFlow
import com.redline.anistalker.utils.toSizeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

enum class DownloadCommands(val id: Int, val action: String) {
    NONE(-1, "none"),
    ADD(0, "add"),
    PAUSE(1, "pause"),
    RESUME(2, "resume"),
    TOGGLE(3, "toggle"),
    CANCEL(4, "cancel"),
    RESTART(5, "restart"),
    PAUSE_ALL(6, "pause_all"),
    CANCEL_ALL(7, "cancel_all"),
    RESUME_ALL(8, "resume_all")
}

class DownloadService : Service() {
    companion object {
        val ACTION_STATE_CHANGE = "ACTION_STATE_CHANGE"

        fun commandDownload(
            context: Context,
            downloadId: Int,
            animeId: Int,
            episodeId: Int,
            fileName: String,
            track: AnimeTrack,
            quality: VideoQuality
        ) = commandStartService(context) {
            action = DownloadCommands.ADD.name

            putExtra(DownloadTask.DOWNLOAD_ID, downloadId)
            putExtra(DownloadTask.ANIME_ID, animeId)
            putExtra(DownloadTask.EPISODE_ID, episodeId)
            putExtra(DownloadTask.FILENAME, fileName)
            putExtra(DownloadTask.TRACK, track.name)
            putExtra(DownloadTask.QUALITY, quality.name)
        }

        fun commandPause(context: Context, episodeId: Int) = commandStartService(context) {
            action = DownloadCommands.PAUSE.name

            putExtra(DownloadTask.DOWNLOAD_ID, episodeId)
        }

        fun commandResume(context: Context, episodeId: Int) = commandStartService(context) {
            action = DownloadCommands.RESUME.name

            putExtra(DownloadTask.DOWNLOAD_ID, episodeId)
        }

        fun commandCancel(context: Context, episodeId: Int) = commandStartService(context) {
            action = DownloadCommands.CANCEL.name

            putExtra(DownloadTask.DOWNLOAD_ID, episodeId)
        }

        fun commandPauseAll(context: Context) = commandStartService(context) {
            action = DownloadCommands.PAUSE_ALL.name
        }

        fun commandResumeAll(context: Context) = commandStartService(context) {
            action = DownloadCommands.RESUME_ALL.name
        }

        fun commandCancelAll(context: Context) = commandStartService(context) {
            action = DownloadCommands.CANCEL_ALL.name
        }

        fun commandStartService(context: Context, applyOnIntent: Intent.() -> Unit) {
            val intent = Intent(context, DownloadService::class.java).apply{ applyOnIntent() }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else
                context.startService(intent)
        }
    }

    private val downloadTasks = mutableListOf<DownloadTask>()

    private val IDLE_TIMEOUT = 60 * 1000L
    private val PROCESSING_LIMIT = 2

    private lateinit var checkHandle: Handler
    private lateinit var serviceScope: CoroutineScope
    private lateinit var downloadProcessingFlow: ExecutionFlow
    private lateinit var downloadingFlow: ExecutionFlow

    private val notifications = ConcurrentHashMap<Int, NotificationCompat.Builder>()
    private lateinit var nfManager: NotificationManagerCompat
    private val downloadChannelId = "CHANNEL_DOWNLOAD"
    private var fgNotification = 0


    override fun onBind(intent: Intent?): IBinder? = null

    private fun idleCheck(handler: Handler) {
        handler.postDelayed({
            if (
                downloadProcessingFlow.isEmpty() &&
                downloadingFlow.isEmpty()
            ) {
                stopSelf()
            }
            else {
                idleCheck(handler)
            }
        }, IDLE_TIMEOUT)
    }

    override fun onCreate() {
        nfManager = NotificationManagerCompat.from(applicationContext)

        checkHandle = Handler(Looper.getMainLooper())
        idleCheck(checkHandle)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                downloadChannelId,
                "Download Notification Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            nfManager.createNotificationChannel(channel)

            val nf = NotificationCompat.Builder(this, downloadChannelId)
            nf.setSmallIcon(R.drawable.logo)
            nf.setContentTitle("Checking Downloads.")
            nf.setOngoing(true)

            startForeground(1, nf.build())

            checkHandle.postDelayed({
                if (downloadingFlow.isEmpty() && downloadProcessingFlow.isEmpty()) stopSelf()
                else nfManager.cancel(1)
            }, 2000)
        }

        serviceScope = CoroutineScope(Dispatchers.Default)

        downloadProcessingFlow = ExecutionFlow(PROCESSING_LIMIT, serviceScope)
        downloadingFlow = ExecutionFlow(Runtime.getRuntime().availableProcessors(), serviceScope)

        FileMaster.initialize(this)

        downloadTasks += DownloadMaster.restoreDownloads().onEach { task ->
            downloadTasks += task
            broadCastDownloadTask(task)
            if (
                task.status() == DownloadStatus.RUNNING ||
                task.status() == DownloadStatus.WRITING ||
                task.status() == DownloadStatus.WAITING ||
                task.status() == DownloadStatus.NETWORK_WAITING
            ) {
                processDownload(task)
            }
        }

    }

    override fun onDestroy() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {

            val id = getIntExtra(DownloadTask.DOWNLOAD_ID, 0)
            if (id == 0) return@run
            val task = getDownloadTask(id)

            when (DownloadCommands.valueOf(action ?: "NONE")) {
                DownloadCommands.ADD -> {
                    val animeId = getIntExtra(DownloadTask.ANIME_ID, 0)
                    val fileName = getStringExtra(DownloadTask.FILENAME) ?: "animeEpisode-$id"
                    val track = AnimeTrack.valueOf(getStringExtra(DownloadTask.TRACK) ?: "DUB")
                    val quality = VideoQuality.valueOf(getStringExtra(DownloadTask.QUALITY) ?: "UHD")
                    val episodeId = getIntExtra(DownloadTask.EPISODE_ID, 0)

                    addDownload(id, animeId, episodeId, fileName, track, quality)
                }

                DownloadCommands.PAUSE -> serviceScope.launch {
                    task?.run {
                        if (
                            status() != DownloadStatus.PAUSED &&
                            status() != DownloadStatus.COMPLETED &&
                            status() != DownloadStatus.CANCELLED
                        ) {
                            stop()
                        }
                    }
                }

                DownloadCommands.RESUME -> serviceScope.launch {
                    task?.run {
                        if (task.status() == DownloadStatus.PAUSED || task.status() == DownloadStatus.WAITING)
                            processDownload(this, task.status() == DownloadStatus.WAITING)
                    }
                }

                DownloadCommands.TOGGLE -> serviceScope.launch {
                    when (task?.status()) {
                        DownloadStatus.RUNNING,
                        DownloadStatus.WAITING,
                        DownloadStatus.NETWORK_WAITING -> { task.stop() }
                        DownloadStatus.PAUSED -> { processDownload(task) }
                        else -> { }
                    }
                }

                DownloadCommands.CANCEL -> serviceScope.launch {
                    task?.run {
                        cancel()
                        downloadTasks.remove(this)
                        broadCastDownloadTask(this)
                    }
                }

                DownloadCommands.RESTART -> serviceScope.launch {
                    task?.run {
                        cancel()
                        downloadTasks.remove(this)
                        addDownload(downloadId, animeId, episodeId, fileName, track, quality)
                        broadCastDownloadTask(this)
                    }
                }

                DownloadCommands.PAUSE_ALL -> serviceScope.launch {
                    downloadTasks.forEach {
                        if (
                            it.status() != DownloadStatus.PAUSED &&
                            it.status() != DownloadStatus.COMPLETED &&
                            it.status() != DownloadStatus.CANCELLED
                        ) {
                            it.stop()
                        }
                    }
                }
                DownloadCommands.CANCEL_ALL -> serviceScope.launch {
                    downloadTasks.forEach {
                        it.cancel()
                    }
                }
                DownloadCommands.RESUME_ALL -> serviceScope.launch {
                    downloadTasks.forEach {
                        if (it.status() == DownloadStatus.PAUSED || it.status() == DownloadStatus.WAITING)
                            processDownload(it, it.status() == DownloadStatus.WAITING)
                    }
                }
                DownloadCommands.NONE -> {}
            }

        }

        return START_NOT_STICKY
    }

    private fun addDownload(
        downloadId: Int,
        animeId: Int,
        epId: Int,
        fileName: String,
        track: AnimeTrack,
        quality: VideoQuality
    ) {
        downloadProcessingFlow.execute {
            DownloadMaster.download(
                downloadId,
                animeId,
                epId,
                fileName = fileName,
                track = track,
                quality = quality,
            )?.also {
                downloadTasks += it
                processDownload(it)
            } ?: broadcastFailure(animeId, downloadId)
        }
    }

    private fun processDownload(task: DownloadTask, forced: Boolean = false) {
        task.onStatusChange { broadCastDownloadTask(this) }
        task.activate()

        downloadingFlow.execute(forced) {
            val runStateIntent = Intent(this@DownloadService, DownloadService::class.java).apply {
                action = DownloadCommands.TOGGLE.name
                putExtra(DownloadTask.DOWNLOAD_ID, task.downloadId)
            }

            val cancelIntent = Intent(this@DownloadService, DownloadService::class.java).apply {
                action = DownloadCommands.CANCEL.name
                putExtra(DownloadTask.DOWNLOAD_ID, task.downloadId)
            }

            val runStateAction = PendingIntent.getService(
                this@DownloadService,
                0,
                runStateIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val cancelAction = PendingIntent.getService(
                this@DownloadService,
                0,
                cancelIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val nf = registerNotification(task.downloadId).apply {
                setOngoing(true)
                setSmallIcon(android.R.drawable.stat_sys_download)
                setContentTitle(task.fileName)
                setContentText("Waiting...")
                setProgress(0, 0, true)
            }

            updateNotification(task.downloadId, nf)

            task.onStatusChange {
                val progress =
                    if (it == DownloadStatus.WRITING) {
                        if (size() <= 0) 0f
                        else downloadedSize().toFloat() / size().toFloat() * 100f
                    } else {
                        if (duration <= 0) 0f
                        else downloadedDuration() / duration * 100f
                    }

                val progressString = String.format("%.2f%% \u2022 ${downloadSpeed().toSizeFormat()}", progress)

                nf.clearActions()

                if (status() == DownloadStatus.PAUSED) nf.setSmallIcon(android.R.drawable.ic_media_pause)
                else if (status() == DownloadStatus.COMPLETED) nf.setSmallIcon(android.R.drawable.stat_sys_download_done)
                else nf.setSmallIcon(android.R.drawable.stat_sys_download)

                when (it) {
                    DownloadStatus.PROCESSING -> {}
                    DownloadStatus.RUNNING -> {
                        nf.apply {
                            setProgress(100, progress.roundToInt(), false)
                            setContentText(progressString)
                            setOngoing(true)

                            addAction(R.drawable.pause, "Pause", runStateAction)
                            addAction(R.drawable.close, "Cancel", cancelAction)
                        }
                    }

                    DownloadStatus.WRITING -> {
                        nf.apply {
                            setProgress(100, progress.roundToInt(), false)
                            setContentText("Writing...")
                            setOngoing(true)
                        }
                    }

                    DownloadStatus.PAUSED -> {
                        nf.apply {
                            setProgress(100, progress.roundToInt(), false)
                            setContentText("PAUSED (${ String.format("%.2f%%", progress) })")
                            setOngoing(false)

                            addAction(R.drawable.play, "Resume", runStateAction)
                            addAction(R.drawable.close, "Cancel", cancelAction)
                        }

                        updateNotification(downloadId, nf)
                        unRegisterNotification(downloadId)
                    }

                    DownloadStatus.WAITING -> {
                        nf.apply {
                            setProgress(0, 0, true)
                            setContentText("Waiting...")
                            setOngoing(true)

                            addAction(android.R.drawable.ic_media_pause, "Pause", runStateAction)
                            addAction(R.drawable.close, "Cancel", cancelAction)
                        }
                    }

                    DownloadStatus.NETWORK_WAITING -> {
                        nf.apply {
                            setProgress(0, 0, true)
                            setContentText("Waiting for Network...")
                            setOngoing(true)

                            addAction(R.drawable.pause, "Pause", runStateAction)
                            addAction(R.drawable.close, "Cancel", cancelAction)
                        }
                    }

                    else -> {
                        unRegisterNotification(downloadId)
                        nfManager.cancel(downloadId)
                    }
                }
                updateNotification(downloadId, nf)
            }

            task.start()
        }
    }

    private fun getDownloadTask(id: Int): DownloadTask? {
        return downloadTasks.find { it.downloadId == id }
    }

    private fun broadCastDownloadTask(task: DownloadTask) {
        Intent(ACTION_STATE_CHANGE).also {
            it.setPackage(packageName)

            it.putExtra(DownloadTask.DOWNLOAD_ID, task.downloadId)
            it.putExtra(DownloadTask.ANIME_ID, task.animeId)
            it.putExtra(DownloadTask.EPISODE_ID, task.episodeId)
            it.putExtra(DownloadTask.STATUS, task.status().name)

            when (task.status()) {
                DownloadStatus.RUNNING -> {
                    it.putExtra(DownloadTask.DOWNLOADED_DURATION, task.downloadedDuration())
                    it.putExtra(DownloadTask.DOWNLOADED_SIZE, task.downloadedSize())
                    it.putExtra(DownloadTask.DOWNLOAD_SPEED, task.downloadSpeed())
                }

                DownloadStatus.WRITING -> {
                    it.putExtra(DownloadTask.SIZE, task.size())
                    it.putExtra(DownloadTask.DOWNLOADED_SIZE, task.downloadedSize())
                }

                DownloadStatus.COMPLETED -> {
                    it.putExtra(DownloadTask.DURATION, task.duration)
                    it.putExtra(DownloadTask.SIZE, task.size())
                }

                else -> {
                    it.putExtra(DownloadTask.DURATION, task.duration)
                    it.putExtra(DownloadTask.DOWNLOADED_DURATION, task.downloadedDuration())
                    it.putExtra(DownloadTask.DOWNLOADED_SIZE, task.downloadedSize())
                }
            }

            sendBroadcast(it)
        }
    }

    private fun broadcastFailure(animeId: Int, id: Int) {
        Intent(ACTION_STATE_CHANGE).also {
            it.setPackage(packageName)

            it.putExtra(DownloadTask.DOWNLOAD_ID, id)
            it.putExtra(DownloadTask.ANIME_ID, animeId)
            it.putExtra(DownloadTask.STATUS, DownloadStatus.FAILED.name)

            sendBroadcast(it)
        }
    }


    // ========================================
    // Notification Service

    private fun registerNotification(id: Int): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, downloadChannelId).apply {
            notifications[id] = this
            setSmallIcon(R.drawable.notification_fill)
            priority = NotificationCompat.PRIORITY_LOW

            if (fgNotification == 0) {
                fgNotification = id
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(id, build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(id, build())
                }
            }
        }
    }

    private fun unRegisterNotification(id: Int): NotificationCompat.Builder? {
        return notifications[id]?.apply {
            notifications.remove(id)
            if (notifications.size > 0) {
                val next = notifications.entries.first()
                fgNotification = next.key
                startForeground(next.key, next.value.build())
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    stopForeground(false)
                }
            }

        }
    }

    private fun updateNotification(id: Int, notification: NotificationCompat.Builder) {
        if (!notifications.containsKey(id)) return
        if (ActivityCompat.checkSelfPermission(
                this@DownloadService,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            nfManager.notify(id, notification.build())
        }
    }
}

private fun NotificationCompat.Builder.setInfoText(text: String) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N)
        setSubText(text)
    else
        setContentInfo(text)
}
