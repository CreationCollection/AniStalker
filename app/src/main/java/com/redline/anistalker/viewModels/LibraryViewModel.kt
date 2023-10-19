package com.redline.anistalker.viewModels

import androidx.lifecycle.ViewModel
import com.redline.anistalker.managements.UserData

class LibraryViewModel() : ViewModel() {
    private val stateWatchlist = "STATE_WATCHLIST"
    private val stateAnime = "STATE_ANIME"

    val watchlist = UserData.watchlist
    val animeList = UserData.animeList
    val mangaList = UserData.mangaList

    fun sync() {

    }
}