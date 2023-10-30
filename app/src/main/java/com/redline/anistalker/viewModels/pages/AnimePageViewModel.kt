package com.redline.anistalker.viewModels.pages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.HistoryEntry
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.models.Watchlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimePageViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val stateId = "STATE_ID"

    private var currentAnimeId = savedStateHandle.getStateFlow(stateId, 0)

    private val _anime = MutableStateFlow<Anime?>(null)
    val anime = _anime.asStateFlow()
    private val _watchlist = MutableStateFlow<List<Watchlist>?>(null)
    val watchlist = _watchlist.asStateFlow()
    private val _images = MutableStateFlow(emptyList<String>())
    val images = _images.asStateFlow()


    init {
        viewModelScope.launch {
            UserData.watchlist.collect { updateWatchlist() }
        }
    }

    fun initialize(animeId: Int) {
        if (currentAnimeId.value != animeId) viewModelScope.launch {
            savedStateHandle[stateId] = animeId
            val result = StalkMedia.Anime.getAnimeDetail(animeId)
            _anime.value = result
//            launch { _images.value = StalkMedia.Anime.getAnimeImageList(result.id) }
            launch { updateWatchlist() }
        }
    }

    fun addAnimeToWatchlist(watchId: Int): AniResult<Boolean> {
        return UserData.addAnime(watchId, currentAnimeId.value)
    }

    fun removeAnimeFromWatchlist(watchId: Int): AniResult<Boolean> {
        return UserData.removeAnime(watchId, currentAnimeId.value)
    }

    fun downloadEpisodes(episodes: List<Int>, quality: VideoQuality, track: AnimeTrack) {
        DownloadManager.Anime.download(currentAnimeId.value, episodes, quality, track)
    }

    fun updateEvent(episodeEvent: Boolean, completionEvent: Boolean) {
//        history.value?.run {
//            savedStateHandle[stateHistory] = UserData.updateHistory(
//                currentAnimeId.value,
//                copy(
//                    contentEvent = episodeEvent,
//                    completionEvent = completionEvent
//                )
//            )
//        }
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
            if (it.series.contains(currentAnimeId.value)) list.add(it)
        }
        _watchlist.value = list
    }
}