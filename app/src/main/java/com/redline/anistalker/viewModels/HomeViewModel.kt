package com.redline.anistalker.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeSpotlight
import com.redline.anistalker.models.IMediaPage
import com.redline.anistalker.utils.fill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel() : ViewModel() {
    private var isSpotlightLoaded = false
    private var page: IMediaPage<AnimeCard>? = null
    private var jobScope = CoroutineScope(Dispatchers.Main)

    private val _spotlightAnime = MutableStateFlow(emptyList<AnimeSpotlight>())
    val spotlightAnime = _spotlightAnime.asStateFlow()
    val currentAnime = UserData.getCurrentWatchAnime()

    private val _animeList = MutableStateFlow(listOf<AnimeCard?>())
    val animeList = _animeList.asStateFlow()
    private val _spotlightError = MutableStateFlow<Pair<AniErrorCode, String>?>(null)
    val spotlightError = _spotlightError.asStateFlow()
    private val _error = MutableStateFlow<Pair<AniErrorCode, String>?>(null)
    val error = _error.asStateFlow()

    val animeCategories = listOf(
        AnimeCategory.RECENTLY_UPDATED,
        AnimeCategory.TOP_AIRING,
        AnimeCategory.COMPLETED,
        AnimeCategory.MOST_FAVORITE,
        AnimeCategory.MOST_POPULAR,
    )

    init {
        loadSpotlightAnime()
        changeCategory(animeCategories[0])
    }

    override fun onCleared() {
        super.onCleared()
        jobScope.cancel()
    }

    fun changeCategory(category: AnimeCategory) {
        jobScope.cancel()
        jobScope = CoroutineScope(Dispatchers.Main)

        page = StalkMedia.Anime.getAnimeByCategory(category)
        _animeList.value = listOf()
        loadNextPage()
    }

    fun loadNextPage() {
        page?.run {
            if (isLoading() || !hasNextPage()) return
            jobScope.launch {
                safeExecute(
                    catch = { _error.value = it }
                ) {
                    updateAnimeList(emptyAnimeList())
                    val list = mutableListOf<AnimeCard>()
                    nextPage().toCollection(list)
                    updateAnimeList(list)
                }
            }
        }
    }

    fun loadSpotlightAnime() {
        if (isSpotlightLoaded) return

        viewModelScope.launch {
            safeExecute(
                catch = { _spotlightError.value = it }
            ) {
                _spotlightAnime.value = StalkMedia.Anime.getSpotlightAnime()
                isSpotlightLoaded = true
            }
        }
    }

    private fun updateAnimeList(values: List<AnimeCard?>) {
        _animeList.value = values.toCollection(_animeList.value.filterNotNull().toMutableList())
    }

    private suspend fun safeExecute(
        catch: (Pair<AniErrorCode, String>) -> Unit,
        calculation: suspend () -> Unit
    ) {
        try {
            calculation()
        } catch (ex: AniError) {
            ex.printStackTrace()
            var message = ""
            message = when (ex.errorCode) {
                AniErrorCode.SERVER_ERROR ->
                    "Something is wrong at server size."

                AniErrorCode.SLOW_NETWORK_ERROR ->
                    "Unstable Internet Connection detected"

                AniErrorCode.CONNECTION_ERROR ->
                    "Unable to reach server, connection problem happened"

                else ->
                    "Unrecognized Error: ${ex.message}"
            }
            catch(Pair(ex.errorCode, message))
        } catch (ex: Exception) {
            ex.printStackTrace()
            catch(
                Pair(
                    AniErrorCode.UNKNOWN,
                    ex.message ?: AniErrorCode.UNKNOWN.message
                )
            )
        }
    }

    private fun emptyAnimeList(): List<AnimeCard?> {
        return mutableListOf<AnimeCard?>().fill(10) { null }
    }
}