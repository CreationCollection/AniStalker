package com.redline.anistalker.viewModels.pages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.models.Watchlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimePageViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private var currentAnimeId = 0
    private var jobScope = CoroutineScope(Dispatchers.IO)

    private val _anime = MutableStateFlow<Anime?>(null)
    val anime = _anime.asStateFlow()
    private val _watchlist = MutableStateFlow<List<Watchlist>?>(null)
    val watchlist = _watchlist.asStateFlow()
    private val _images = MutableStateFlow(emptyList<String>())
    val images = _images.asStateFlow()
    private val _currentAnime = MutableStateFlow(false)
    val currentAnime = _currentAnime.asStateFlow()


    init {
        viewModelScope.launch {
            UserData.watchlist.collect { updateWatchlist() }
        }
//        viewModelScope.launch {
//            UserData.getCurrentWatchAnime().collect {
//                _currentAnime.value = it.id.zoroId == currentAnimeId.value
//            }
//        }
    }

    override fun onCleared() {
        jobScope.cancel()
        super.onCleared()
    }

    fun initialize(animeId: Int) {
        if (currentAnimeId != animeId) {
            jobScope.cancel()
            jobScope = CoroutineScope(Dispatchers.IO)
            jobScope.launch {
                currentAnimeId = animeId
                val result = StalkMedia.Anime.getAnimeDetail(animeId)
                _anime.value = result
                launch { _images.value = StalkMedia.Anime.getAnimeImageList(result.id) }
                launch { updateWatchlist() }
            }
        }
    }

    fun removeAnimeFromWatchlist(watchId: Int): AniResult<Boolean> {
        return UserData.removeAnime(watchId, currentAnimeId)
    }

    fun downloadEpisodes(episodes: List<Int>, quality: VideoQuality, track: AnimeTrack) {
        DownloadManager.Anime.download(currentAnimeId, episodes, quality, track)
    }

    fun toggleCurrentAnime() {
        _currentAnime.value = !_currentAnime.value
        UserData.setCurrentAnime(
            if (_currentAnime.value) _anime.value
            else null
        )
    }

    fun updateLastEpisode(lastEpisode: Int) {
//        history.value?.run {
//            savedStateHandle[stateHistory] = UserData.updateHistory(
//                currentAnimeId.value,
//                copy(lastContent = lastEpisode)
//            )
//        }
    }

    private fun updateWatchlist() {
        val list = mutableListOf<Watchlist>()
        UserData.watchlist.value.forEach {
            if (it.series.contains(currentAnimeId)) list.add(it)
        }
        _watchlist.value = list
    }
}