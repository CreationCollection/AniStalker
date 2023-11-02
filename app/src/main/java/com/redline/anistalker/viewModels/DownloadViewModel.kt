package com.redline.anistalker.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.models.EpisodeRange
import com.redline.anistalker.models.OngoingEpisodeDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DownloadViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val stateCollection =
        mutableMapOf<Int, MutableStateFlow<Map<String, List<EpisodeRange>>>>()

    init {
    }

    val animeDownloads = DownloadManager.Anime.animeDownloads

    fun getAnimeOngoingDownload(animeDID: Int): StateFlow<List<OngoingEpisodeDownload>>? =
        DownloadManager.Anime.getOngoingDownloads(animeDID)

    // MANGA
//    val mangaList = DownloadManager.Manga.mangaDownloads

//    fun getMangaContentInfo(mangaId: String) = DownloadManager.Manga.getContent(mangaId)
//    fun getMangaDownloadInfo(mangaId: String) = DownloadManager.Manga.getOngoingContent(mangaId)
}