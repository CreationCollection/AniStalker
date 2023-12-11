package com.redline.anistalker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.AnimeSpotlight
import com.redline.anistalker.models.IMediaPage
import com.redline.anistalker.utils.fill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class HomeViewModel() : ViewModel() {

    val animeCategories = listOf(
        AnimeCategory.RECENTLY_UPDATED,
        AnimeCategory.TOP_AIRING,
        AnimeCategory.COMPLETED,
        AnimeCategory.MOST_FAVORITE,
        AnimeCategory.MOST_POPULAR,
    )

    // Browsing items
    private var currentCategory = AnimeCategory.MOST_FAVORITE
    private var browsingPage: IMediaPage<AnimeCard>? = null
    private val _browseList = MutableStateFlow(emptyAnimeList())
    val browseList = _browseList.asStateFlow()

    private var browsingJob = CoroutineScope(Dispatchers.IO)


    // Search items
    private val _searchResult = MutableStateFlow(emptyAnimeList())
    val searchResult = _searchResult.asStateFlow()

    private var searchQuery = ""
    private var searchFilter = AnimeSearchFilter()
    private var searchPage: IMediaPage<AnimeCard>? = StalkMedia.Anime.filter(searchFilter)

    private var searchingJob = CoroutineScope(Dispatchers.IO)


    // Suggestions
    private val _suggestions = MutableStateFlow(emptyList<AnimeSpotlight?>())
    val suggestions = _suggestions.asStateFlow()

    // Current And Recent Animes
    val currentAnime = UserData.currentAnime
    val recentAnime = MutableStateFlow(emptyList<AnimeCard>()).asStateFlow()

    init {
        _suggestions.value = listOf<AnimeSpotlight?>().fill(10) { null }
        viewModelScope.launch(Dispatchers.IO) {
            do try {
                _suggestions.value = StalkMedia.Anime.getSpotlightAnime()
                break
            }
            catch (err: AniError) {
                err.printStackTrace()
            }
            catch (err: IOException) {
                err.printStackTrace()
                break
            } while (true)
        }
        changeCategory(AnimeCategory.RECENTLY_UPDATED)
    }

    fun changeCategory(category: AnimeCategory) {
        if (currentCategory != category) {
            browsingPage = null
            revalidateBrowsingJob()
            _browseList.value = emptyAnimeList()
            browsingPage = StalkMedia.Anime.getAnimeByCategory(category)
        }
    }

    fun loadNextPage() {
        if (browsingPage != null && !browsingPage!!.isLoading() && browsingPage!!.hasNextPage()) {
            fillEmptyAnime { _browseList }
            browsingJob.launch {
                try {
                    fillAnime(browsingPage!!.nextPage()) { _browseList }
                } catch (ex: Exception) { ex.printStackTrace() }
            }
        }
    }

    fun searchAnime(query: String, filter: AnimeSearchFilter) {
        if (query != searchQuery || filter != searchFilter) {
            searchQuery = query
            searchFilter = filter
            searchPage = null
            revalidateSearchingJob()
            _searchResult.value = emptyAnimeList()
            searchPage =
                if (query.isBlank()) StalkMedia.Anime.filter(filter)
                else StalkMedia.Anime.search(query, filter)
        }
    }

    fun loadNextSearchPage() {
        if (searchPage != null && !searchPage!!.isLoading() && searchPage!!.hasNextPage()) {
            fillEmptyAnime { _searchResult }
            searchingJob.launch {
               fillAnime(searchPage!!.nextPage()) { _searchResult }
            }
        }
    }

    private fun fillEmptyAnime(list: () -> MutableStateFlow<List<AnimeCard?>>) {
        list().apply {
            value = value.filterNotNull() + emptyAnimeList()
        }
    }

    private fun fillAnime(items: List<AnimeCard>, list: () -> MutableStateFlow<List<AnimeCard?>>) {
        list().apply {
            value = value.filterNotNull() + items
        }
    }

    private fun emptyAnimeList(): List<AnimeCard?> {
        return mutableListOf<AnimeCard?>().fill(10) { null }
    }

    private fun revalidateBrowsingJob() {
        browsingJob.cancel()
        browsingJob = CoroutineScope(Dispatchers.IO)
    }

    private fun revalidateSearchingJob() {
        searchingJob.cancel()
        searchingJob = CoroutineScope(Dispatchers.IO)
    }
}