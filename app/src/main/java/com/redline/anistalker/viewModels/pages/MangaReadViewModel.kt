package com.redline.anistalker.viewModels.pages
//
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.redline.anistalker.managements.StalkMedia
//import com.redline.anistalker.models.MangaChapter
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.launch
//
//class MangaReadViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
//    private val stateChapter = "STATE_MANGA_CHAPTER"
//    private val stateChapters = "STATE_MANGA_CHAPTERS"
//    private val statePages = "STATE_PAGES"
//
//    val chapter = savedStateHandle.getStateFlow<MangaChapter?>(stateChapter, null)
//    val chapters = savedStateHandle.getStateFlow<List<MangaChapter>>(stateChapters, emptyList())
//    val pages = savedStateHandle.getStateFlow<List<String>>(statePages, emptyList())
//
//    private var dataScope: CoroutineScope? = null
//    private var chapterIndex = -1
//
//    fun loadChapter(mangaChapter: MangaChapter) {
//        dataScope?.run { cancel() }
//        dataScope = CoroutineScope(Dispatchers.Default + viewModelScope.coroutineContext)
//
//        dataScope?.launch {
//            val mangaId = mangaChapter.mangaId
//            fetchAllChapters(mangaId)
//            savedStateHandle[stateChapter] = mangaChapter
//            fetchPages()
//        }
//    }
//
//    fun loadNextPage() {
//        val index = getChapterIndex() + 1
//        if (index >= chapters.value.size) return
//        loadChapter(chapters.value[index])
//    }
//
//    fun loadPreviousPage() {
//        val index = getChapterIndex() - 1
//        if (index < 0) return
//        loadChapter(chapters.value[index])
//    }
//
//    private suspend fun fetchAllChapters(mangaId: String) {
////        savedStateHandle[stateChapters] = StalkMedia.Manga.getMangaChapters(mangaId)
//    }
//
//    private suspend fun fetchPages() {
//        val chapter = chapter.value ?: return
////        savedStateHandle[statePages] = StalkMedia.Manga.getMangaPages(chapter.id)
//    }
//
//    private fun getChapterIndex(): Int {
//        if (chapterIndex < 0)
//            chapterIndex = chapters.value.indexOfFirst { it.id == chapter.value?.id }.coerceAtLeast(0)
//        return chapterIndex
//    }
//}