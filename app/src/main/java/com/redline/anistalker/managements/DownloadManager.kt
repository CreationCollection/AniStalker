package com.redline.anistalker.managements

import android.annotation.SuppressLint
import android.content.Context
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.OngoingEpisodeDownload
import com.redline.anistalker.models.VideoQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("StaticFieldLeak")
object DownloadManager {
    private lateinit var context: Context

    fun initialize(context: Context) {
        DownloadManager.context = context.applicationContext
    }

    object Anime {

        private val contentMap = mutableMapOf<Int, MutableStateFlow<List<EpisodeDownload>>>()
        private val ongoingContent: MutableMap<Int, MutableStateFlow<List<OngoingEpisodeDownload>>> = mutableMapOf()

        val animeDownloads = UserData.animeDownload

        fun getContent(animeDID: Int): StateFlow<List<EpisodeDownload>>? {
            return contentMap[animeDID]
        }

        fun getOngoingDownloads(animeDID: Int): StateFlow<List<OngoingEpisodeDownload>>? {
            return ongoingContent[animeDID]
        }

        fun download(animeId: Int, epId: List<Int>, quality: VideoQuality = VideoQuality.UHD, track: AnimeTrack = AnimeTrack.SUB) {

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

