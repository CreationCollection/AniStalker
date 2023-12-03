package com.redline.anistalker.viewModels

import androidx.lifecycle.ViewModel
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.OngoingEpisodeDownload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadDetailViewModel : ViewModel() {
    private var jobScope = CoroutineScope(Dispatchers.Main)

    private var currentAnimeId: Int? = null
    private val _animeDownload = MutableStateFlow<AnimeDownload?>(null)
    val animeDownload = _animeDownload.asStateFlow()

    private val _ongoingDownloads = MutableStateFlow<List<OngoingEpisodeDownload>>(emptyList())
    val ongoingDownloads = _ongoingDownloads.asStateFlow()
    private val _failedDownloads = MutableStateFlow<List<Int>>(emptyList())
    val failedDownloads = _failedDownloads.asStateFlow()

    fun initializeFor(animeId: Int) {
        if (currentAnimeId != animeId) {
            jobScope.cancel()
            jobScope = CoroutineScope(Dispatchers.Main)
            jobScope.launch {
                currentAnimeId = animeId
                launch {
                    UserData.animeDownload.collect {
                        it.find { d -> d.animeId.zoroId == currentAnimeId }?.let { d ->
                            _animeDownload.value = d
                        }
                    }
                }
                launch {
                    DownloadManager.Anime.getOngoingDownloads(animeId)?.collect {
                        _ongoingDownloads.value = it
                    }
                }
                launch {
                    DownloadManager.Anime.getFailedDownloads(animeId)?.collect {
                        _failedDownloads.value = it
                    }
                }
            }
        }
    }

    fun getContent(episodeId: Int): EpisodeDownload? {
        return UserData.getDownloadContent(episodeId)
    }
}