package com.redline.anistalker.viewModels

import androidx.lifecycle.ViewModel
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.IMediaPage
import com.redline.anistalker.utils.fillList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel() : ViewModel() {
    private var jobScope = newJobScope()

//    private val _mangaList = MutableStateFlow(listOf<MangaCard>())
//    val mangaList = _mangaList.asStateFlow()
//    private val _mangaImages = MutableStateFlow(listOf<String?>())
//    val mangaImages = _mangaImages.asStateFlow()
//    private val _isMangaLoading = MutableStateFlow(false)
//    val isMangaLoading = _isMangaLoading.asStateFlow()

    private val _animeList = MutableStateFlow(listOf<AnimeCard?>())
    val animeList = _animeList.asStateFlow()
    private val _error = MutableStateFlow<Pair<AniErrorCode, String>?>(null)
    val error = _error.asStateFlow()

    private var animePage: IMediaPage<AnimeCard>? = null
//    private var mangaPage: IMediaPage<MangaCard>? = null

    init {
        jobScope.launch {
            animePage = StalkMedia.Anime.filter(AnimeSearchFilter())
            loadNextAnimePage()

//            mangaPage = StalkMedia.Manga.getTrendingManga()
//            loadNextMangaPage()
        }
    }

    fun filter(filter: AnimeSearchFilter) {
        jobScope.cancel()
        jobScope = newJobScope()

        _animeList.value = emptyList()
        animePage = StalkMedia.Anime.filter(filter)
        loadNextAnimePage()
    }

    fun search(keyword: String, animeFilter: AnimeSearchFilter) {
        jobScope.cancel()
        jobScope = newJobScope()

        _animeList.value = emptyList()
        animePage = StalkMedia.Anime.search(keyword, animeFilter)
        loadNextAnimePage()
    }

    fun loadNextAnimePage() {
        animePage?.run {
            jobScope.launch {
                if (isLoading()) return@launch

                updateAnimeStateValue(emptyAnimeList())
                try {
                    updateAnimeStateValue(nextPage())
                } catch(ex: AniError) {
                    val message = when (ex.errorCode) {
                        AniErrorCode.SERVER_ERROR ->
                            "Something is wrong at server size."

                        AniErrorCode.SLOW_NETWORK_ERROR ->
                            "Unstable Internet Connection detected"

                        AniErrorCode.CONNECTION_ERROR ->
                            "Unable to reach server, connection problem happened"

                        else ->
                            "Unrecognized Error: ${ ex.message }"
                    }
                    _error.value = Pair(ex.errorCode, message)
                } catch (ex: Exception) {
                    _error.value = Pair(
                        AniErrorCode.UNKNOWN,
                        ex.message ?: AniErrorCode.UNKNOWN.message
                    )
                }
            }
        }
    }

//    fun loadNextMangaPage() {
//        mangaPage?.run {
//            jobScope.launch {
//                if (isLoading()) return@launch
//
//                _isMangaLoading.value = true
//                delay(Random.nextLong(1000, 3000))
//                val mangas = nextPage()
//                updateMangaStateValue(mangas)
//                loadMangaImages(mangas)
//                _isMangaLoading.value = false
//            }
//        }
//    }

//    private fun loadMangaImages(mangas: List<MangaCard>) {
//        _mangaImages.value = _mangaImages.value.fill(mangas.size) { null }
//    }

    private fun updateAnimeStateValue(values: List<AnimeCard?>) {
        _animeList.run {
            value = values.toCollection(value.filterNotNull().toMutableList())
        }
    }

//    private fun updateMangaStateValue(values: List<MangaCard>) {
//        _mangaList.run {
//            value = values.toCollection(value.filterNotNull().toMutableList())
//        }
//    }

    private fun emptyAnimeList(): List<AnimeCard?> {
        return fillList(10) { null }
    }

    private fun newJobScope() = CoroutineScope(Dispatchers.Main)

    override fun onCleared() {
        super.onCleared()
        jobScope.cancel()
    }
}