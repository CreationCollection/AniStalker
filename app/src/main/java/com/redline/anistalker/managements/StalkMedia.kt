package com.redline.anistalker.managements

import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.IMediaPage
import com.redline.anistalker.models.MangaCard
import com.redline.anistalker.models.MangaChapter
import com.redline.anistalker.models.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.redline.anistalker.models.Anime as AnimeFull
import com.redline.anistalker.models.Manga as MangaFull

private class MediaPage<T>(val url: String, val dummy: () -> T) : IMediaPage<T> {
    var hasMore: Boolean = true
    var currentPage: Int = 0

    override fun page(): Int = currentPage

    override fun hasNextPage(): Boolean = hasMore

    override suspend fun nextPage(): List<T> {
        delay(1000)
        return mutableListOf<T>().apply {
            for (i in 1..10) {
                add(dummy())
            }
        }
    }

}

object StalkMedia {
    object Anime {

        fun search(query: String, filter: AnimeSearchFilter): IMediaPage<AnimeCard> =
            MediaPage("") { AnimeCard() }

        fun filter(filter: AnimeSearchFilter): IMediaPage<AnimeCard> =
            MediaPage("") { AnimeCard() }

        fun getAnimeByCategory(category: AnimeCategory): IMediaPage<AnimeCard> =
            MediaPage("") { AnimeCard() }

        suspend fun getAnimeDetail(animeId: Int): AnimeFull {
            return withContext(Dispatchers.IO) {
                // Fetch Anime data from server
                delay(1000)
                AnimeFull()
            }
        }

        suspend fun getAnimeEpisodes(animeId: Int): List<AnimeEpisodeDetail> {
            return withContext(Dispatchers.IO) {
                delay(1200)
                mutableListOf<AnimeEpisodeDetail>().apply {
                    for (i in 1..12) {
                        add(AnimeEpisodeDetail())
                    }
                }
            }
        }

        suspend fun getEpisodeLinks(epId: Int): Video {
            return withContext(Dispatchers.IO) {
                delay(1500)
                Video()
            }
        }
    }

    object Manga {

        fun search(query: String): IMediaPage<MangaCard> =
            MediaPage("") { MangaCard() }

        fun getTrendingManga(): IMediaPage<MangaCard> =
            MediaPage("") { MangaCard() }

        suspend fun getMangaDetail(): MangaFull {
            return withContext(Dispatchers.IO) {
                delay(1000)
                MangaFull()
            }
        }

        suspend fun getMangaChapters(mangaId: String): List<MangaChapter> {
            return withContext(Dispatchers.IO) {
                delay(1000)
                mutableListOf<MangaChapter>().apply {
                    for (i in 1..20) {
                        add(MangaChapter())
                    }
                }
            }
        }

        suspend fun getMangaPages(chId: String): List<String> {
            return withContext(Dispatchers.IO) {
                delay(1000)
                mutableListOf<String>().apply {
                    for (i in 1..20) {
                        add("")
                    }
                }
            }
        }
    }
}