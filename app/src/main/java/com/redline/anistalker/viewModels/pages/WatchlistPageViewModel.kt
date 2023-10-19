package com.redline.anistalker.viewModels.pages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.Watchlist
import kotlinx.coroutines.launch

class WatchlistPageViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val stateId = "STATE_ID"
    private val stateWatchlist = "STATE_WATCHLIST"
    private val stateAnimeList = "STATE_ANIME_LIST"

    private val currentWatchId = savedStateHandle.getStateFlow(stateId, 0)

    val watchlist = savedStateHandle.getStateFlow<Watchlist?>(stateWatchlist, null)
    val animeList = savedStateHandle.getStateFlow<List<AnimeCard>?>(stateAnimeList, null)

    init {
        viewModelScope.launch {
            UserData.watchlist.collect {
                updateInfo()
            }
        }
    }

    fun initialize(watchId: Int) {
        if (currentWatchId.value != watchId) viewModelScope.launch {
            savedStateHandle[stateId] = watchId
            updateInfo()
        }
    }

    fun updateWatchlist(watchlist: Watchlist) {
        UserData.updateWatchlist(currentWatchId.value, watchlist)
        updateInfo()
    }

    fun removeAnime(values: List<Int>) {
        val watch = watchlist.value?.let {
            it.copy(series = it.series.filter { i -> !values.contains(i) })
        } ?: return

        updateWatchlist(watch)
        updateInfo()
    }

    fun shareLink(): String {
        return ""
    }

    private fun updateInfo() {
        viewModelScope.launch {
            val watch = UserData.watchlist.value.find { it.id == currentWatchId.value }
            val anime = UserData.animeList.value.filter {
                watch?.let { it.series.contains(it.id) } ?: false
            }

            savedStateHandle[stateWatchlist] = watch
            savedStateHandle[stateAnimeList] = anime
        }
    }
}