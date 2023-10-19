package com.redline.anistalker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.OngoingEpisodeDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadDetailViewModel : ViewModel() {
    private var currentAnimeId: Int? = null
    private val _animeDownload = MutableStateFlow<AnimeDownload?>(null)
    val animeDownload = _animeDownload.asStateFlow()

    fun initializeFor(animeDID: Int) {
        val temp = currentAnimeId
        currentAnimeId = animeDID

        if (temp == null) viewModelScope.launch {
            DownloadManager.Anime.animeDownloads.collect {
                it.find { d -> d.dId == currentAnimeId }?.let { d ->
                    _animeDownload.value = d
                }
            }
        }
    }

    fun getOngoingDownloads(animeDID: Int): StateFlow<List<OngoingEpisodeDownload>>? =
        DownloadManager.Anime.getOngoingDownloads(animeDID)
}