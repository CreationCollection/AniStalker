package com.redline.anistalker.managements

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.redline.anistalker.managements.downloadSystem.DownloadTask
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.OngoingEpisodeDownload
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.services.DownloadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.redline.anistalker.models.Anime as AnimeFull

object DownloadManager {
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return

        context.applicationContext.registerReceiver(
            Anime.animeBroadcastReceiver,
            IntentFilter(DownloadService.ACTION_STATE_CHANGE)
        )
        Anime.initialize()
        DownloadService.commandStartService(context) { }

        initialized = true
    }

    object Anime {
        fun initialize() {
            if (initialized) return

            UserData.animeDownload.value.forEach { anime ->
//                val list = mutableListOf<OngoingEpisodeDownload>()
//                anime.ongoingContent.forEach { episode ->
//                    UserData.getDownloadContent(episode)?.run {
//                        val v = OngoingEpisodeDownload(
//                            id = id,
//                            num = num,
//                        )
//                        list.add(v)
//                    }
//                }
                ongoingContent[anime.animeId.zoroId] = MutableStateFlow(emptyList())
                failedContent[anime.animeId.zoroId] = MutableStateFlow(emptyList())
            }
        }

        private val ongoingContent: MutableMap<Int, MutableStateFlow<List<OngoingEpisodeDownload>>> =
            mutableMapOf()
        private val failedContent: MutableMap<Int, MutableStateFlow<List<OngoingEpisodeDownload>>> =
            mutableMapOf()
        val animeDownloads = UserData.animeDownload

        val animeBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.run {
                    val animeId = getIntExtra(DownloadTask.ANIME_ID, 0)
                    val id = getIntExtra(DownloadTask.DOWNLOAD_ID, 0)
                    val status = DownloadStatus.valueOf(
                        getStringExtra(DownloadTask.STATUS)
                            ?: DownloadStatus.PROCESSING.name
                    )

                    ongoingContent[animeId]?.apply {
                        value = value.ownDownload(id).map { item ->
                            if (item.id != id) item
                            else updateOngoingContent(animeId, item, status)
                        }.let {
                            when (status) {
                                DownloadStatus.COMPLETED,
                                DownloadStatus.CANCELLED -> {
                                    val duration = getFloatExtra(DownloadTask.DURATION, 0f)
                                    val size = getLongExtra(DownloadTask.SIZE, 0)

                                    UserData.completeDownload(animeId, id, duration, size)

                                    it.filterNot { item -> item.id == id }
                                }
                                DownloadStatus.FAILED -> {
                                    it.filterNot { item ->
                                        if (item.id == id) {
                                            failedContent[animeId]?.apply { value += item }
                                            true
                                        } else false
                                    }
                                }
                                else -> it
                            }
                        }
                    }
                }
            }

            private fun List<OngoingEpisodeDownload>.ownDownload(id: Int): List<OngoingEpisodeDownload> =
                when (none { it.id == id }) {
                    true -> UserData.getDownloadContent(id)?.let { download ->
                        this + OngoingEpisodeDownload(
                            id = download.id,
                            num = download.num,
                            size = download.size,
                            duration = download.duration
                        )
                    } ?: this

                    false -> this
                }

            private fun Intent.updateOngoingContent(
                animeId: Int,
                item: OngoingEpisodeDownload,
                status: DownloadStatus
            ): OngoingEpisodeDownload {
                var download = item.copy(status = status)

                when (status) {
                    DownloadStatus.RUNNING -> {
                        val duration =
                            getFloatExtra(DownloadTask.DOWNLOADED_DURATION, 0f)
                        val size = getLongExtra(DownloadTask.DOWNLOADED_SIZE, 0)
                        val speed = getLongExtra(DownloadTask.DOWNLOAD_SPEED, 0)

                        download = download.copy(
                            downloadedDuration = duration,
                            downloadedSize = size,
                            downloadSpeed = speed
                        )
                    }

                    DownloadStatus.WRITING -> {
                        val size = getLongExtra(DownloadTask.SIZE, 0)
                        val downloadedSize =
                            getLongExtra(DownloadTask.DOWNLOADED_SIZE, 0)

                        download = download.copy(
                            size = size,
                            downloadedSize = downloadedSize,
                        )
                    }

                    else -> {
                        val duration =
                            getFloatExtra(DownloadTask.DURATION, 0f)
                        val downloadedDuration =
                            getFloatExtra(DownloadTask.DOWNLOADED_DURATION, 0f)
                        val size = getLongExtra(DownloadTask.DOWNLOADED_SIZE, 0)

                        download = download.copy(
                            duration = duration,
                            downloadedDuration = downloadedDuration,
                            downloadedSize = size,
                        )
                    }
                }

                return download
            }
        }

        fun getOngoingDownloads(animeDID: Int): StateFlow<List<OngoingEpisodeDownload>>? {
            return ongoingContent[animeDID]
        }

        fun download(
            context: Context,
            anime: AnimeFull,
            episode: AnimeEpisodeDetail,
            track: AnimeTrack = AnimeTrack.SUB
        ) {
            if (!UserData.canDownload(episode.id, track)) return

            val episodeDownload = UserData.addAnimeDownload(anime, episode, track)
            val ongoingDownload = OngoingEpisodeDownload(
                id = episodeDownload.id,
                num = episodeDownload.num,
                status = DownloadStatus.PROCESSING,
            )

            ongoingContent.getOrPut(anime.id.zoroId) {
                MutableStateFlow(emptyList())
            }.apply {
                value = value.toMutableList().apply { add(ongoingDownload) }
            }

            DownloadService.commandDownload(
                context = context,
                episodeDownload.id,
                animeId = anime.id.zoroId,
                episodeId = episodeDownload.episodeId,
                fileName = episodeDownload.file,
                track = track,
                quality = VideoQuality.UHD,
            )
        }

        fun cancel(context: Context, epId: Int) {
            DownloadService.commandCancel(context, epId)
        }

        fun pause(context: Context, epId: Int) {
            DownloadService.commandPause(context, epId)
        }

        fun resume(context: Context, epId: Int) {
            DownloadService.commandResume(context, epId)
        }

        fun restart(epId: Int) {

        }

        fun cancelAll(context: Context) {
            DownloadService.commandCancelAll(context)
        }

        fun cancelAll(context: Context, animeId: Int) {

        }

        fun pauseAll(context: Context) {
            DownloadService.commandPauseAll(context)
        }

        fun pauseAll(context: Context, animeId: Int) {
            DownloadService.commandPauseAll(context)
        }

        fun resumeAll(context: Context) {
            DownloadService.commandResumeAll(context)
        }

        fun resumeAll(animeId: Int) {

        }

        fun removeEpisode(animeId: Int, epId: Int) {

        }

        fun removeAnime(animeId: Int) {

        }
    }

//    object Manga {
//
//        private val _mangaDownloads: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
//        val mangaDownloads = _mangaDownloads.asStateFlow()
//
//        private val content: MutableMap<String, MutableStateFlow<MangaDownload>> = mutableMapOf()
//        private val ongoingContent: MutableMap<String, MutableStateFlow<MangaDownloadContent>> =
//            mutableMapOf()
//
//        fun getContent(mangaId: String): StateFlow<MangaDownload>? {
//            return content[mangaId]
//        }
//
//        fun getOngoingContent(mangaId: String): StateFlow<MangaDownloadContent>? {
//            return ongoingContent[mangaId]
//        }
//
//        fun downloadChapter(mangaId: String, chId: List<String>) {
//            if (!_mangaDownloads.value.contains(mangaId)) {
//                _mangaDownloads.run {
//                    value = mutableListOf<String>().apply { addAll(value); add(mangaId) }
//                }
//
//                content[mangaId] = MutableStateFlow(MangaDownload())
//                ongoingContent[mangaId] = MutableStateFlow(MangaDownloadContent())
//            }
//
//            content[mangaId]?.run {
//                value = value.copy(downloadableChapters = value.downloadableChapters.toMutableList().apply {
//                    addAll(chId)
//                })
//            }
//        }
//
//        fun removeChapter(mangaId: String, chId: List<String>) {
//            content[mangaId]?.run {
//                value = value.copy(downloadableChapters = value.downloadableChapters.filter {
//                    !chId.contains(it)
//                })
//            }
//        }
//
//        fun removeManga(mangaId: String) {
//            _mangaDownloads.run {
//                value = value.filter {
//                    it != mangaId
//                }
//            }
//
//            content.remove(mangaId)
//            ongoingContent.remove(mangaId)
//        }
//
//        fun pause(mangaId: String) {
//            ongoingContent[mangaId]?.apply {
//                value = MangaDownloadContent(status = DownloadStatus.PAUSED)
//            }
//        }
//
//        fun pauseAll() {
//            ongoingContent.values.forEach {
//                it.value = it.value.copy(status = DownloadStatus.PAUSED)
//            }
//        }
//
//        fun cancel(mangaId: String) {
//            ongoingContent[mangaId]?.apply {
//                value = MangaDownloadContent(status = DownloadStatus.CANCELLED)
//            }
//        }
//
//        fun cancelAll() {
//            ongoingContent.values.forEach {
//                it.value = it.value.copy(status = DownloadStatus.CANCELLED)
//            }
//        }
//
//        fun resume(mangaId: String) {
//            ongoingContent[mangaId]?.apply {
//                value = MangaDownloadContent(status = DownloadStatus.WAITING)
//            }
//        }
//
//        fun resumeAll() {
//            ongoingContent.values.forEach {
//                it.value = it.value.copy(status = DownloadStatus.WAITING)
//            }
//        }
//    }
}

