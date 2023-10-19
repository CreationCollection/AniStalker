package com.redline.anistalker.viewModels.pages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.managements.StalkMedia
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.HistoryEntry
import com.redline.anistalker.models.Manga
import com.redline.anistalker.models.MangaChapter
import kotlinx.coroutines.launch

class MangaPageViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val stateId = "STATE_ID"
    private val stateManga = "STATE_MANGA"
    private val stateImages = "STATE_IMAGES"
    private val stateInLibrary = "STATE_LIBRARY_AVAIL"
    private val stateHistory = "STATE_HISTORY"
    private val stateChapters = "STATE_CHAPTERS"

    private var currentMangaId = savedStateHandle.getStateFlow(stateId, "")

    val manga = savedStateHandle.getStateFlow<Manga?>(stateManga, null)
    val images = savedStateHandle.getStateFlow<List<String>?>(stateImages, null)
    val isInLibrary = savedStateHandle.getStateFlow(stateInLibrary, false)
    val history = savedStateHandle.getStateFlow<HistoryEntry?>(stateHistory, null)
    val chapters = savedStateHandle.getStateFlow<List<MangaChapter>?>(stateChapters, null)

    init {
        viewModelScope.launch {
            UserData.mangaList.collect {
                checkInLibrary()
            }
        }
    }

    fun initialize(mangaId: String) {
        if (mangaId != currentMangaId.value) viewModelScope.launch {
            savedStateHandle[stateId] = mangaId
            val result = StalkMedia.Manga.getMangaDetail(mangaId)
            savedStateHandle[stateManga] = result
            launch { savedStateHandle[stateImages] = StalkMedia.Manga.getMangaImages(result.malId) }
            launch { savedStateHandle[stateHistory] = UserData.getHistoryEntry(mangaId) }
            launch { checkInLibrary() }
            launch { savedStateHandle[stateChapters] = StalkMedia.Manga.getMangaChapters(mangaId) }
        }
    }

    fun addMangaToLibrary() {
        UserData.addManga(currentMangaId.value)
    }

    fun removeMangaFromLibrary() {
        UserData.removeManga(currentMangaId.value)
    }

    fun downloadChapters(chapters: List<String>) {
        DownloadManager.Manga.downloadChapter(currentMangaId.value, chapters)
    }

    fun updateEvent(chapterEvent: Boolean, completionEvent: Boolean) {
        history.value?.run {
            savedStateHandle[stateHistory] = UserData.updateHistory(
                currentMangaId.value,
                copy(
                    contentEvent = chapterEvent,
                    completionEvent = completionEvent
                )
            )

        }
    }

    fun updateLastEpisode(lastChapter: Int) {
        history.value?.run {
            savedStateHandle[stateHistory] = UserData.updateHistory(
                currentMangaId.value,
                copy(lastContent = lastChapter)
            )

        }
    }

    private fun checkInLibrary() {
        savedStateHandle[stateInLibrary] =
            UserData.mangaList.value.find { it.id == currentMangaId.value } != null
    }
}