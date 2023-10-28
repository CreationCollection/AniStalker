package com.redline.anistalker.managements

import com.redline.anistalker.managements.helper.AnimeMediaExtractor.extractEpisodeDetails
import com.redline.anistalker.managements.helper.AnimeMediaExtractor.extractStreamingVideo
import com.redline.anistalker.managements.helper.AnimeMediaExtractor.makeAnime
import com.redline.anistalker.managements.helper.AnimeMediaExtractor.makeAnimeCard
import com.redline.anistalker.managements.helper.AnimeMediaExtractor.makeAnimeSpotlight
import com.redline.anistalker.managements.helper.AniLinks
import com.redline.anistalker.managements.helper.MangaMediaExtractor.makeMangaCard
import com.redline.anistalker.managements.helper.MangaMediaExtractor.extractMangaDetails
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeId
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.AnimeSpotlight
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.IMediaPage
import com.redline.anistalker.models.MangaCard
import com.redline.anistalker.models.MangaChapter
import com.redline.anistalker.models.Video
import com.redline.anistalker.utils.getSafeInt
import com.redline.anistalker.utils.getSafeString
import com.redline.anistalker.utils.getStringOrNull
import com.redline.anistalker.utils.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.redline.anistalker.models.Anime as AnimeFull
import com.redline.anistalker.models.Manga as MangaFull

private class MediaPage<T>(val url: String, initialPage: Int = 0, val factory: (JSONObject) -> T) :
    IMediaPage<T> {
    var hasMore: Boolean = true
    var currentPage: Int = initialPage
    var loading: Boolean = false
    override fun isLoading(): Boolean = loading

    override fun page(): Int = currentPage

    override fun hasNextPage(): Boolean = hasMore

    override suspend fun nextPage(): List<T> {
        return withContext(Dispatchers.IO) {
            loading = true

            val value = Net.get(url.replace(":page", "${currentPage + 1}"))
            val json = JSONObject(value)

            val status = json.getInt("status")

            if (status == 404) {
                throw AniError(AniErrorCode.NOT_FOUND, "No Content Found.")
            } else if (status != 200) {
                throw AniError(AniErrorCode.SERVER_ERROR)
            }

            hasMore = json.getSafeInt("lastPage", currentPage + 1) > currentPage + 1
            currentPage = json.getSafeInt("page", currentPage)

            loading = false
            json.getJSONArray("data").run {
                val list = mutableListOf<T>()
                for (i in 0 until length()) {
                    list.add(factory(getJSONObject(i)))
                }
                list
            }
        }
    }

}

object StalkMedia {
    private fun fetchData(url: String): JSONObject {
        val value = Net.get(url)
        val json = JSONObject(value)

        val status = json.getInt("status")

        if (status == 404) {
            throw AniError(AniErrorCode.NOT_FOUND, "No Content Found.")
        } else if (status != 200) {
            throw AniError(AniErrorCode.SERVER_ERROR)
        }

        return json
    }

    object Anime {

        fun search(query: String, filter: AnimeSearchFilter): IMediaPage<AnimeCard> =
            MediaPage(AniLinks.Anime.makeSearchLink(query, filter)) { makeAnimeCard(it) }

        fun filter(filter: AnimeSearchFilter): IMediaPage<AnimeCard> =
            MediaPage(AniLinks.Anime.makeFilterLink(filter)) { makeAnimeCard(it) }

        fun getAnimeByCategory(category: AnimeCategory): IMediaPage<AnimeCard> =
            MediaPage(AniLinks.Anime.makeCategoryLink(category)) { makeAnimeCard(it) }

        suspend fun getSpotlightAnime(): List<AnimeSpotlight> {
            return withContext(Dispatchers.IO) {
                val json = fetchData(AniLinks.Anime.spotlight)

                val list = mutableListOf<AnimeSpotlight>()
                json.getJSONArray("data").run {
                    for (i in 0 until length()) {
                        list.add(makeAnimeSpotlight(getJSONObject(i)))
                    }
                }

                list
            }
        }

        suspend fun getAnimeDetail(animeId: Int): AnimeFull {
            return withContext(Dispatchers.IO) {
                val json = fetchData(AniLinks.Anime.makeAnimeDetailLink(animeId))
                makeAnime(json.getJSONObject("data"))
            }
        }

        suspend fun getAnimeEpisodes(animeId: Int): List<AnimeEpisodeDetail> {
            return withContext(Dispatchers.IO) {
                val json = fetchData(AniLinks.Anime.makeEpisodesLink(animeId))

                json.getJSONArray("data").map {
                    extractEpisodeDetails(getJSONObject(it))
                }
            }
        }

        suspend fun getEpisodeLinks(epId: Int, track: AnimeTrack, files: Boolean = false): Video {
            return withContext(Dispatchers.IO) {
                val json = fetchData(AniLinks.Anime.makeEpisodeVideoLink(epId, track, files))
                extractStreamingVideo(json.getJSONObject("data"))
            }
        }

        suspend fun getAnimeImageList(id: AnimeId): List<String> {
            return withContext(Dispatchers.IO) {
                val malId =
                    if (id.malId > 0) id.malId
                    else throw AniError(
                        AniErrorCode.INVALID_VALUE,
                        "Invalid Anime MalId passed"
                    )
                val json = fetchData(AniLinks.Anime.makeImagesLink(malId))
                json.getJSONArray("data").map { getString(it) }
            }
        }
    }

    object Manga {

        fun search(query: String): IMediaPage<MangaCard> =
            MediaPage(AniLinks.Manga.makeMangaSearchLink(query)) { makeMangaCard(it) }

        fun getTrendingManga(): IMediaPage<MangaCard> =
            MediaPage(AniLinks.Manga.trending) { makeMangaCard(it) }

        fun getHentaiManga(): IMediaPage<MangaCard> =
            MediaPage(AniLinks.Manga.hentai) { makeMangaCard(it) }

        suspend fun getMangaDetail(mangaId: String): MangaFull {
            return withContext(Dispatchers.IO) {
                val json = fetchData(AniLinks.Manga.makeMangaDetailsLink(mangaId))
                extractMangaDetails(json.getJSONObject("data"))
            }
        }

        suspend fun getMangaChapters(mangaId: String): List<MangaChapter> {
            return withContext(Dispatchers.IO) {
                val json = fetchData(AniLinks.Manga.makeChaptersLink(mangaId))
                json.getJSONArray("data").map {

                }

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

        suspend fun getMangaImages(malId: Int): List<String> {
            return withContext(Dispatchers.IO) {
                delay(2000)
                mutableListOf<String>().apply {
                    for (i in 1..10) {
                        add("")
                    }
                }
            }
        }
    }
}