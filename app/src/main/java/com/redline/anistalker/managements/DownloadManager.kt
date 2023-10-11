package com.redline.anistalker.managements

import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.MangaDownload
import com.redline.anistalker.models.MangaDownloadContent
import com.redline.anistalker.models.OngoingEpisodeDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DownloadManager {
    init {

    }

    object Anime {

        private val _animeDownloads: MutableStateFlow<List<Int>> = MutableStateFlow(listOf())
        val animeDownloads = _animeDownloads.asStateFlow()

        private val contents: MutableMap<Int, MutableStateFlow<List<Int>>> = mutableMapOf()
        private val contentDetail: MutableMap<Int, EpisodeDownload> = mutableMapOf()

        private val ongoingContent: MutableMap<Int, MutableStateFlow<List<Int>>> = mutableMapOf()
        private val ongoingContentDetails: MutableMap<Int, MutableStateFlow<OngoingEpisodeDownload>> =
            mutableMapOf()

        fun getContents(animeDID: Int): StateFlow<List<Int>>? {
            return contents[animeDID]
        }

        fun getContentDetail(epId: Int): EpisodeDownload? {
            return contentDetail[epId]
        }

        fun getOngoingContents(animeDID: Int): StateFlow<List<Int>>? {
            return ongoingContent[animeDID]
        }

        fun getOngoingContentDetail(epId: Int): StateFlow<OngoingEpisodeDownload>? {
            return ongoingContentDetails[epId]
        }

        fun Download(animeId: Int, epId: Int) {
            val items: MutableList<Int> = mutableListOf<Int>().apply {
                addAll(_animeDownloads.value)
                add(animeId)
            }
            _animeDownloads.apply {
                value = mutableListOf<Int>().apply {
                    addAll(value)
                    add(animeId)
                }
            }

            val contentFlow = contents[animeId] ?: MutableStateFlow(listOf())
            if (contents[animeId] == null)
                contents[animeId] = contentFlow

            contentFlow.apply {
                value = mutableListOf<Int>().apply {
                    addAll(value)
                    add(epId)
                }
            }

            contentDetail[epId] = EpisodeDownload(id = epId)
        }

        fun cancel(epId: Int) {
            ongoingContentDetails[epId]?.apply {
                value = value.copy(id = epId, status = DownloadStatus.CANCELLED)
            }
        }

        fun pause(epId: Int) {
            ongoingContentDetails[epId]?.apply {
                value = value.copy(id = epId, status = DownloadStatus.PAUSED)
            }
        }

        fun resume(epId: Int) {
            ongoingContentDetails[epId]?.apply {
                value = value.copy(id = epId, status = DownloadStatus.WAITING)
            }
        }

        fun restart(epId: Int) {

        }

        fun cancelAll() {
            ongoingContentDetails.values.forEach {
                it.value =
                    it.value.copy(id = it.value.id, status = DownloadStatus.CANCELLED)
            }
        }

        fun cancelAll(animeId: Int) {
            ongoingContent[animeId]?.run {
                value.forEach {
                    ongoingContentDetails[it]?.run {
                        value = value.copy(id = it, status = DownloadStatus.CANCELLED)
                    }
                }
            }
        }

        fun pauseAll() {
            ongoingContentDetails.values.forEach {
                it.value = it.value.copy(id = it.value.id, status = DownloadStatus.PAUSED)
            }
        }

        fun pauseAll(animeId: Int) {
            ongoingContent[animeId]?.run {
                value.forEach {
                    ongoingContentDetails[it]?.run {
                        value = value.copy(id = it, status = DownloadStatus.PAUSED)
                    }
                }
            }
        }

        fun resumeAll() {
            ongoingContentDetails.values.forEach {
                it.value = it.value.copy(id = it.value.id, status = DownloadStatus.WAITING)
            }
        }

        fun resumeAll(animeId: Int) {
            ongoingContent[animeId]?.run {
                value.forEach {
                    ongoingContentDetails[it]?.run {
                        value = value.copy(id = it, status = DownloadStatus.WAITING)
                    }
                }
            }
        }

        fun removeEpisode(animeId: Int, epId: Int) {
            contentDetail.remove(epId)
            contents[animeId]?.run {
                value = value.filter {
                    it != epId
                }
            }

            if (contents[animeId]?.value?.isEmpty() == true)
                removeAnime(animeId)
        }

        fun removeAnime(animeId: Int) {
            _animeDownloads.run {
                value = value.filter {
                    it != animeId
                }
            }
            contents.remove(animeId)
        }
    }

    object Manga {

        private val _mangaDownloads: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
        val mangaDownloads = _mangaDownloads.asStateFlow()

        private val content: MutableMap<String, MutableStateFlow<MangaDownload>> = mutableMapOf()
        private val ongoingContent: MutableMap<String, MutableStateFlow<MangaDownloadContent>> =
            mutableMapOf()

        fun getContent(mangaId: String): StateFlow<MangaDownload>? {
            return content[mangaId]
        }

        fun getOngoingContent(mangaId: String): StateFlow<MangaDownloadContent>? {
            return ongoingContent[mangaId]
        }

        fun downloadChapter(mangaId: String, chId: List<String>) {
            if (!_mangaDownloads.value.contains(mangaId)) {
                _mangaDownloads.run {
                    value = mutableListOf<String>().apply { addAll(value); add(mangaId) }
                }

                content[mangaId] = MutableStateFlow(MangaDownload())
                ongoingContent[mangaId] = MutableStateFlow(MangaDownloadContent())
            }

            content[mangaId]?.run {
                value = value.copy(downloadableChapters = value.downloadableChapters.toMutableList().apply {
                    addAll(chId)
                })
            }
        }

        fun removeChapter(mangaId: String, chId: List<String>) {
            content[mangaId]?.run {
                value = value.copy(downloadableChapters = value.downloadableChapters.filter {
                    !chId.contains(it)
                })
            }
        }

        fun removeManga(mangaId: String) {
            _mangaDownloads.run {
                value = value.filter {
                    it != mangaId
                }
            }

            content.remove(mangaId)
            ongoingContent.remove(mangaId)
        }

        fun pause(mangaId: String) {
            ongoingContent[mangaId]?.apply {
                value = MangaDownloadContent(status = DownloadStatus.PAUSED)
            }
        }

        fun pauseAll() {
            ongoingContent.values.forEach {
                it.value = it.value.copy(status = DownloadStatus.PAUSED)
            }
        }

        fun cancel(mangaId: String) {
            ongoingContent[mangaId]?.apply {
                value = MangaDownloadContent(status = DownloadStatus.CANCELLED)
            }
        }

        fun cancelAll() {
            ongoingContent.values.forEach {
                it.value = it.value.copy(status = DownloadStatus.CANCELLED)
            }
        }

        fun resume(mangaId: String) {
            ongoingContent[mangaId]?.apply {
                value = MangaDownloadContent(status = DownloadStatus.WAITING)
            }
        }

        fun resumeAll() {
            ongoingContent.values.forEach {
                it.value = it.value.copy(status = DownloadStatus.WAITING)
            }
        }
    }
}

