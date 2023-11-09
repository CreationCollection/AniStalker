package com.redline.anistalker.services

import android.Manifest
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
            episodeId: Int,
            fileName: String,
            track: AnimeTrack,
            quality: VideoQuality
        ) = commandStartService(context) {
            action = DownloadCommands.ADD.name

            putExtra(DownloadTask.EPISODE_ID, episodeId)
            putExtra(DownloadTask.FILENAME, fileName)
            putExtra(DownloadTask.TRACK, track.name)
            putExtra(DownloadTask.QUALITY, quality.name)
        }

        fun commandPause(context: Context, episodeId: Int) = commandStartService(context) {
            action = DownloadCommands.PAUSE.name

            putExtra(DownloadTask.EPISODE_ID, episodeId)
        }

        fun commandResume(context: Context, episodeId: Int) = commandStartService(context) {
            action = DownloadCommands.RESUME.name

            putExtra(DownloadTask.EPISODE_ID, episodeId)
        }

        fun commandCancel(context: Context, episodeId: Int) = commandStartService(context) {
            action = DownloadCommands.CANCEL.name

            putExtra(DownloadTask.EPISODE_ID, episodeId)
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

    override fun onCreate() {
        nfManager = NotificationManagerCompat.from(applicationContext)

        checkHandle = Handler(Looper.getMainLooper())
        checkHandle.postDelayed({
            if (
                downloadProcessingFlow.isEmpty() &&
                downloadingFlow.isEmpty()
            ) {
                stopSelf()
            }
        }, IDLE_TIMEOUT)

        serviceScope = CoroutineScope(Dispatchers.Default)

        downloadProcessingFlow = ExecutionFlow(PROCESSING_LIMIT, serviceScope)
        downloadingFlow = ExecutionFlow(Runtime.getRuntime().availableProcessors(), serviceScope)

        FileMaster.initialize()

        downloadTasks += DownloadMaster.restoreDownloads().onEach { task ->
            if (task.status() == DownloadStatus.RUNNING) {
                processDownload(task)
            }
        }
    }

    override fun onDestroy() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {

            val id = getIntExtra(DownloadTask.EPISODE_ID, 0)
            if (id == 0) return@run
            val task = getDownloadTask(id)

            when (DownloadCommands.valueOf(action ?: "NONE")) {
                DownloadCommands.ADD -> {
                    val fileName = getStringExtra(DownloadTask.FILENAME) ?: "animeEpisode-$id"
                    val track = AnimeTrack.valueOf(getStringExtra(DownloadTask.TRACK) ?: "DUB")
                    val quality = VideoQuality.valueOf(getStringExtra(DownloadTask.QUALITY) ?: "UHD")

                    addDownload(id, fileName, track, quality)
                }

                DownloadCommands.PAUSE -> serviceScope.launch {
                    task?.stop()
                }

                DownloadCommands.RESUME -> serviceScope.launch {
                    task?.start()
                }

                DownloadCommands.TOGGLE -> serviceScope.launch {
                    when (task?.status()) {
                        DownloadStatus.RUNNING -> { task.stop() }
                        DownloadStatus.PAUSED -> { task.start() }
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
                        addDownload(episodeId, fileName, track, quality)
                        broadCastDownloadTask(this)
                    }
                }

                DownloadCommands.PAUSE_ALL -> serviceScope.launch {
                    downloadTasks.forEach {
                        it.stop()
                    }
                }
                DownloadCommands.CANCEL_ALL -> serviceScope.launch {
                    downloadTasks.forEach {
                        it.cancel()
                    }
                }
                DownloadCommands.RESUME_ALL -> serviceScope.launch {
                    downloadTasks.forEach {
                        it.start()
                    }
                }
                DownloadCommands.NONE -> {}
            }

        }

        return START_NOT_STICKY
    }

    private fun addDownload(epId: Int, fileName: String, track: AnimeTrack, quality: VideoQuality) {
        downloadProcessingFlow.execute {
            downloadTasks += DownloadMaster.download(
                epId,
                fileName = fileName,
                track = track,
                quality = quality,
            ).also {
                processDownload(it)
            }
        }
    }

    private fun processDownload(task: DownloadTask) {
        task.onStatusChange { broadCastDownloadTask(this) }
        task.activate()

        downloadingFlow.execute {
            val runStateIntent = Intent(this@DownloadService, DownloadService::class.java).apply {
                action = DownloadCommands.TOGGLE.name
                putExtra(DownloadTask.EPISODE_ID, task.episodeId)
            }

            val cancelIntent = Intent(this@DownloadService, DownloadService::class.java).apply {
                action = DownloadCommands.CANCEL.name
                putExtra(DownloadTask.EPISODE_ID, task.episodeId)
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

            val nf = registerNotification(task.episodeId).apply {
                setOngoing(true)
                setSmallIcon(android.R.drawable.stat_sys_download)
                setContentTitle(task.fileName)
                setContentText("Waiting...")
                setProgress(0, 0, true)
            }

            updateNotification(task.episodeId, nf)

            task.onStatusChange {
                val progress =
                    if (task.status() == DownloadStatus.WRITING) {
                        if (task.size() <= 0) 0f
                        else task.downloadedSize() / (task.size() * 100f)
                    } else {
                        if (task.duration <= 0) 0f
                        else task.downloadedDuration() / task.duration * 100f
                    }

                val progressString = progress.toString().format("%.2f") + " %"

                nf.clearActions()
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

                    DownloadStatus.PAUSED -> {
                        nf.apply {
                            setProgress(100, progress.roundToInt(), false)
                            setContentText(progressString)
                            setOngoing(false)
                            setAutoCancel(true)

                            addAction(R.drawable.play, "Resume", runStateAction)
                            addAction(R.drawable.close, "Cancel", cancelAction)
                        }
                    }

                    DownloadStatus.WAITING -> {
                        nf.apply {
                            setProgress(0, 0, true)
                            setContentText("Waiting...")
                            setOngoing(true)

                            addAction(R.drawable.pause, "Pause", runStateAction)
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

                    DownloadStatus.WRITING -> {
                        nf.apply {
                            setProgress(100, progress.roundToInt(), false)
                            setContentText("Writing...")
                            setOngoing(true)
                        }
                    }

                    else -> {
                        unRegisterNotification(episodeId)
                        nfManager.cancel(episodeId)
                    }
                }
                updateNotification(episodeId, nf)
            }

            task.start()
        }
    }

    private fun getDownloadTask(id: Int): DownloadTask? {
        return downloadTasks.find { it.episodeId == id }
    }

    private fun broadCastDownloadTask(task: DownloadTask) {
        Intent(ACTION_STATE_CHANGE).also {
            it.setPackage(packageName)

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
                    it.putExtra(DownloadTask.DOWNLOADED_DURATION, task.downloadedDuration())
                    it.putExtra(DownloadTask.DOWNLOADED_SIZE, task.downloadedSize())
                }
            }

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
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    stopForeground(true)
                }
            }

        }
    }

    private fun updateNotification(id: Int, notification: NotificationCompat.Builder) {
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
