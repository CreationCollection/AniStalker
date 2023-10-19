package com.redline.anistalker.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.IMediaPage
import com.redline.anistalker.models.MangaCard
import kotlinx.coroutines.launch

class SearchViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val stateAnime = "STATE_ANIME"
    private val stateManga = "STATE_MANGA"

    val animeList = savedStateHandle.getStateFlow<List<AnimeCard?>>(stateAnime, listOf())
    val mangaList = savedStateHandle.getStateFlow<List<MangaCard?>>(stateManga, listOf())

    var animePage: IMediaPage<AnimeCard>? = null
    var mangaPage: IMediaPage<MangaCard>? = null

    fun searchAnime(keyword: String, filter: AnimeSearchFilter = AnimeSearchFilter()) {
        savedStateHandle[stateAnime] = emptyList<AnimeCard?>()
        animePage = StalkMedia.Anime.search(keyword, filter)
    }

    fun filterAnime(filter: AnimeSearchFilter) {
        savedStateHandle[stateAnime] = emptyList<AnimeCard?>()
        animePage = StalkMedia.Anime.filter(filter)
    }

    fun searchManga(keyword: String) {
        savedStateHandle[stateManga] = emptyList<MangaCard?>()
        mangaPage = StalkMedia.Manga.search(keyword)
    }

    fun loadNextAnimePage() {
        if (animePage?.isLoading() != true) return

        val offset = updateAnimeStateValue(emptyAnimeList())
        viewModelScope.launch {
            updateAnimeStateValue(animePage!!.nextPage(), offset)
        }
    }

    fun loadNextMangaPage() {
        if (mangaPage?.isLoading() != true) return

        val offset = updateMangaStateValue(emptyMangaList())
        viewModelScope.launch {
            updateMangaStateValue(mangaPage!!.nextPage(), offset)
        }
    }

    private fun updateAnimeStateValue(values: List<AnimeCard?>, startOffset: Int = 1): Int {
        var offset = 1
        savedStateHandle.get<List<AnimeCard?>>(stateAnime)?.run {
            offset = size
            savedStateHandle[stateAnime] =
                this.subList(0, startOffset).toMutableList().addAll(values)
        }
        return offset
    }

    private fun updateMangaStateValue(values: List<MangaCard?>, startOffset: Int = 1): Int {
        var offset = 1
        savedStateHandle.get<List<MangaCard?>>(stateManga)?.run {
            offset = size
            savedStateHandle[stateManga] =
                this.subList(0, startOffset).toMutableList().addAll(values)
        }
        return offset
    }

    private fun emptyAnimeList(): List<AnimeCard?> {
        return mutableListOf<AnimeCard?>().apply {
            for (i in 1..10) {
                add(null)
            }
        }
    }

    private fun emptyMangaList(): List<MangaCard?> {
        return mutableListOf<MangaCard?>().apply {
            for (i in 1..10) {
                add(null)
            }
        }
    }
}