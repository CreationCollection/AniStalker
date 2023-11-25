package com.redline.anistalker.viewModels.pages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.Watchlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimePageViewModel(
    private val application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val stateAnimeTrack = "STATE_ANIME_TRACK"

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

    private val _episodeList = MutableStateFlow<List<AnimeEpisodeDetail>?>(null)
    val episodeList = _episodeList.asStateFlow()

    val animeTrack = savedStateHandle.getStateFlow(stateAnimeTrack, AnimeTrack.SUB)

    init {
        viewModelScope.launch {
            UserData.watchlist.collect { updateWatchlist() }
        }
        viewModelScope.launch {
            UserData.currentAnime.collect {
                _currentAnime.value = it?.id?.zoroId == currentAnimeId
            }
        }
    }

    override fun onCleared() {
        jobScope.cancel()
        super.onCleared()
    }

    fun cleanUp() {
        jobScope.cancel()
        _anime.value = null
        _watchlist.value = null
        _images.value = emptyList()
        _currentAnime.value = false
    }

    fun initialize(animeId: Int) {
        if (currentAnimeId != animeId) {
            cleanUp()

            jobScope = CoroutineScope(Dispatchers.IO)
            jobScope.launch {
                currentAnimeId = animeId
                val result = StalkMedia.Anime.getAnimeDetail(animeId)
                _anime.value = result
                launch { _images.value = StalkMedia.Anime.getAnimeImageList(result.id) }
                launch { updateWatchlist() }
                launch {
                    _episodeList.value = StalkMedia.Anime.getAnimeEpisodes(animeId)
                }
                _currentAnime.value = animeId == UserData.currentAnime.value?.id?.zoroId
            }
        }
    }

    fun removeAnimeFromWatchlist(watchId: Int): AniResult<Boolean> {
        return UserData.removeAnime(watchId, currentAnimeId)
    }

    fun downloadEpisodes(episode: AnimeEpisodeDetail, track: AnimeTrack) {
        anime.value?.also {
            DownloadManager.Anime.download(
                application.applicationContext,
                it,
                episode,
                track
            )
        }
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