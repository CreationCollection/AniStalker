package com.redline.anistalker.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeSpotlight
import com.redline.anistalker.models.IMediaPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val stateSpotlight = "STATE_SPOTLIGHT"
    private val stateCurrentAnime = "STATE_CURRENT_ANIME"
    private val stateAnimeCategory = "STATE_ANIME_CATEGORY"

    private val isSpotlightLoading = false
    private val pageForCategory = mutableMapOf<String, IMediaPage<AnimeCard>>()

    private val _spotlightImages = MutableStateFlow<List<Bitmap?>>(listOf())

    val spotlightAnime =
        savedStateHandle.getStateFlow<List<AnimeSpotlight>>(stateSpotlight, listOf())
    val spotlightImages = _spotlightImages.asStateFlow()
    val currentAnime = savedStateHandle.getStateFlow<Anime?>(stateCurrentAnime, null)

    fun getAnimeListFor(category: AnimeCategory): StateFlow<List<AnimeCard?>> {
        return savedStateHandle.getStateFlow(
            animeCategoryTag(category),
            listOf()
        )
    }

    fun hasNextPageFor(category: AnimeCategory): Boolean {
        return getPageFor(category).hasNextPage()
    }

    fun loadNextPageFor(category: AnimeCategory) {
        val page = getPageFor(category)

        if (page.isLoading()) return

        val offset = updateStateValueFor(
            category, mutableListOf<AnimeCard?>().apply {
                for (i in 1..10) {
                    add(null)
                }
            }
        )
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000)
            updateStateValueFor(category, page.nextPage(), offset)
        }
    }

    fun loadSpotlightAnime() {
        if (isSpotlightLoading) return
        viewModelScope.launch {
            delay(5000)
            val values = StalkMedia.Anime.getSpotlightAnime()
            savedStateHandle[stateSpotlight] = values
            _spotlightImages.value = mutableListOf<Bitmap?>().apply {
                for (i in 1..10) add(null)
            }
        }
    }

    private fun getPageFor(category: AnimeCategory): IMediaPage<AnimeCard> {
        return pageForCategory.getOrPut(category.value) {
            StalkMedia.Anime.getAnimeByCategory(category)
        }
    }

    private fun updateStateValueFor(category: AnimeCategory, values: List<AnimeCard?>, startOffset: Int = 1): Int {
        var offset = 1
        savedStateHandle.get<List<AnimeCard?>>(animeCategoryTag(category))?.run {
            offset = size
            savedStateHandle[animeCategoryTag(category)] =
                this.subList(0, startOffset).toMutableList().addAll(values)
        }
        return offset
    }

    private fun animeCategoryTag(category: AnimeCategory): String =
        "${stateAnimeCategory}_${category.value}"
}