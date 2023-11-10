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
        context.applicationContext.registerReceiver(
            Anime.animeBroadcastReceiver,
            IntentFilter(DownloadService.ACTION_STATE_CHANGE)
        )
        DownloadService.commandStartService(context) { }
        initialized = true
    }

    object Anime {
        private val ongoingContent: MutableMap<Int, MutableStateFlow<List<OngoingEpisodeDownload>>> =
            mutableMapOf()
        val animeDownloads = UserData.animeDownload

        val animeBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.run {
                    val animeId = getIntExtra(DownloadTask.ANIME_ID, 0)
                    val episodeId = getIntExtra(DownloadTask.EPISODE_ID, 0)
                    val status = DownloadStatus.valueOf(
                        getStringExtra(DownloadTask.STATUS)
                            ?: DownloadStatus.PROCESSING.name
                    )

                    ongoingContent[animeId]?.apply {
                        value = value.map { item ->
                            if (item.id != episodeId) item
                            else updateOngoingContent(item, status)
                        }
                    }
                }
            }

            private fun Intent.updateOngoingContent(
                item: OngoingEpisodeDownload,
                status: DownloadStatus
            ): OngoingEpisodeDownload {
                var download = item

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

                    DownloadStatus.COMPLETED -> {
                        val duration = getFloatExtra(DownloadTask.DURATION, 0f)
                        val size = getLongExtra(DownloadTask.SIZE, 0)

                        download = download.copy(
                            duration = duration,
                            size = size,
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

            val episodeDownload = UserData.addAnimeDownload(anime, episode)
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
                animeId = anime.id.zoroId,
                episodeId = episodeDownload.id,
                fileName =
                if (track == AnimeTrack.SUB) episodeDownload.subFile
                else episodeDownload.dubFile,
                track = track,
                quality = VideoQuality.UHD,
            )
        }

        fun cancel(epId: Int) {

        }

        fun pause(epId: Int) {

        }

        fun resume(epId: Int) {

        }

        fun restart(epId: Int) {

        }

        fun cancelAll() {

        }

        fun cancelAll(animeId: Int) {

        }

        fun pauseAll() {

        }

        fun pauseAll(animeId: Int) {

        }

        fun resumeAll() {

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

