package com.redline.anistalker.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.EpisodeRange
import com.redline.anistalker.models.OngoingEpisodeDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DownloadViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val stateCollection =
        mutableMapOf<Int, MutableStateFlow<Map<String, List<EpisodeRange>>>>()

    init {
        viewModelScope.launch {
            DownloadManager.Anime.animeDownloads.collect {
                it.forEach { d ->
                    stateCollection[d.dId]?.value = calculateEpisodeRange(d)
                }
            }
        }
    }

    val animeDownloads = DownloadManager.Anime.animeDownloads

    fun getAnimeDownloadContent(animeDID: Int): StateFlow<Map<String, List<EpisodeRange>>> {
        return stateCollection.getOrPut(animeDID) {
            MutableStateFlow<Map<String, List<EpisodeRange>>>(mapOf()).also {
                stateCollection[animeDID] = it

                viewModelScope.launch {
                    DownloadManager.Anime.animeDownloads.value
                        .find { d -> d.dId == animeDID }
                        .let { d ->
                            calculateEpisodeRange(d)
                        }
                        .also { r ->
                            it.value = r
                        }
                }
            }
        }
    }

    fun getAnimeOngoingDownload(animeDID: Int): StateFlow<List<OngoingEpisodeDownload>>? =
        DownloadManager.Anime.getOngoingDownloads(animeDID)

    private fun calculateEpisodeRange(anime: AnimeDownload?): Map<String, List<EpisodeRange>> {
        if (anime == null) return emptyMap()

        val result = mutableMapOf<String, MutableList<EpisodeRange>>()

        val relations = anime.anime.map {
            it.relation
        }

        anime.content.onEachIndexed { i, entry ->
            result[relations[i]] = entry.value.let {
                val list = mutableListOf<EpisodeRange>()

                var range = EpisodeRange(start = 1)
                it.forEach { d ->
                    range = if (range.end + 1 == d.num) {
                        range.copy(end = range.end + 1)
                    } else {
                        list.add(range)
                        EpisodeRange(start = d.num, end = d.num)
                    }
                }

                list
            }
        }

        return result
    }

    // MANGA
    val mangaList = DownloadManager.Manga.mangaDownloads

    fun getMangaContentInfo(mangaId: String) = DownloadManager.Manga.getContent(mangaId)
    fun getMangaDownloadInfo(mangaId: String) = DownloadManager.Manga.getOngoingContent(mangaId)
}