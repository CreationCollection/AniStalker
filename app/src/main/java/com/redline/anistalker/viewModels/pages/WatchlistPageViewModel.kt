package com.redline.anistalker.viewModels.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.Watchlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WatchlistPageViewModel : ViewModel() {

    private var currentWatchId = 0
    private var jobScope = CoroutineScope(Dispatchers.Default)

    private val _watchlist = MutableStateFlow<Watchlist?>(null)
    val watchlist = _watchlist.asStateFlow()
    private val _animeList = MutableStateFlow(emptyList<AnimeCard>())
    val animeList = _animeList.asStateFlow()
    private val _images = MutableStateFlow(emptyList<String>())
    val images = _images.asStateFlow()

    init {
        viewModelScope.launch {
            UserData.watchlist.collect {
                _watchlist.value = it.find { watch -> watch.id == currentWatchId }
                updateAnimeList()
            }
        }
    }

    fun initialize(watchId: Int) {
        if (currentWatchId != watchId) {
            jobScope.cancel()
            jobScope = CoroutineScope(Dispatchers.Default)
            jobScope.launch {
                currentWatchId = watchId
                _watchlist.value = UserData.watchlist.value.find { it.id == watchId }
                updateAnimeList()
            }
        }
    }

    fun shareLink(): String {
        return ""
    }

    private fun updateAnimeList() {
        _animeList.value = _watchlist.value?.run {
            UserData.animeList.value.filter { series.contains(it.id) }
        } ?: emptyList()

        _images.value = _animeList.value.map { it.image }
    }
}