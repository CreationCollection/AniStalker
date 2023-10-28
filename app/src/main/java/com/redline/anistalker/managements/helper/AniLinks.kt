package com.redline.anistalker.managements.helper

import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.AnimeTrack
import java.net.URLEncoder

object AniLinks {
    val baseUrl = "https://stalk-anime.up.railway.app"

    object Anime {
        val spotlight = "$baseUrl/anime/spotlight"
        private val category = "$baseUrl/anime/category/"
        private val genre = "$baseUrl/anime/genre/:genre"
        private val search = "$baseUrl/anime/search/:keyword?page=:page"
        private val filter = "$baseUrl/anime/filter?page=:page"
        private val details = "$baseUrl/anime/:id"
        private val episodes = "$baseUrl/anime/:id/episodes"
        private val episodeServers = "$baseUrl/anime/episode/:id/servers"
        private val episodeVideos = "$baseUrl/anime/episode/:id/video?track=:track&sf=:files"
        private val images = "$baseUrl/anime/:id/images"

        fun makeSearchLink(keyword: String, filter: AnimeSearchFilter): String {
            return search
                .replace(":keyword", keyword) + makeFilterParams(filter)
        }

        fun makeFilterLink(filter: AnimeSearchFilter) =
            this.filter + makeFilterParams(filter)

        fun makeAnimeDetailLink(animeId: Int) =
            details.replace(":id", animeId.toString())

        fun makeEpisodesLink(animeId: Int) =
            episodes.replace(":id", animeId.toString())

        fun makeEpisodeServersLink(epId: Int) =
            episodeServers.replace(":id", epId.toString())

        fun makeEpisodeVideoLink(epId: Int, track: AnimeTrack, files: Boolean) =
            episodeVideos
                .replace(":epId", epId.toString())
                .replace(":track", track.value.lowercase())
                .replace(":files", files.toString())

        fun makeCategoryLink(category: AnimeCategory) =
            this.category + category.value

        fun makeGenreLink(genre: String) =
            this.genre.replace(":genre", genre)

        fun makeImagesLink(malId: Int) =
            images.replace(":id", malId.toString())

        private fun makeFilterParams(filter: AnimeSearchFilter): String {
            return "".run {
                this + "&sort=${filter.sort.index}"
                this + "&score=${filter.score.index}"
                this + "&type=${ filter.type.index }"
                this + "&track=${ filter.track.index }"
            }
        }
    }

    object Manga {
        private val search = "$baseUrl/manga/search/:keyword"
        val trending = "$baseUrl/manga/trending?page=:page"
        val hentai = "$baseUrl/manga/hentai?page=:page"
        private val details = "$baseUrl/manga/:id"
        private val chapters = "$baseUrl/manga/:id/chapters"
        private val pages = "$baseUrl/manga/pages/:id"
        private val cover = "$baseUrl/manga/cover/:id"

        fun makeMangaSearchLink(query: String) =
            search.replace(":keyword", URLEncoder.encode(query, "UTF-8"))

        fun makeMangaDetailsLink(mangaId: String) =
            details.replace(":id", mangaId)

        fun makeChaptersLink(mangaId: String) =
            chapters.replace(":id", mangaId)

        fun makeChapterPagesLink(chapterId: String) =
            pages.replace(":id", chapterId)

        fun makeCoverLink(coverId: String) =
            cover.replace(":id", coverId)
    }
}