package com.redline.anistalker.managements

import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.Event
import com.redline.anistalker.models.HistoryEntry
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.models.WatchlistPrivacy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

data class UserInfo(val username: String, val name: String)

object UserData {
    private var userInfo: UserInfo? = null
    private var userAuthToken: String? = null

    private val _currentAnime = MutableStateFlow<Anime?>(null)
    val currentAnime = _currentAnime.asStateFlow()

    private val _watchlist =
        MutableStateFlow<List<Watchlist>>(emptyList())
    val watchlist = _watchlist.asStateFlow()

    private val _animeList =
        MutableStateFlow<List<AnimeCard>>(emptyList())
    val animeList = _animeList.asStateFlow()

    private val _eventList =
        MutableStateFlow<List<Event>>(emptyList())
    val eventList = _eventList.asStateFlow()

    private val _animeDownload =
        MutableStateFlow<List<AnimeDownload>>(emptyList())
    val animeDownload = _animeDownload.asStateFlow()

    private val downloadContent = mutableMapOf<Int, EpisodeDownload>()

    init {
        userInfo = UserInfo("Anmol011", "Anmol Kashyap")
    }

    fun getCurrentUser(): UserInfo? = userInfo

    fun getDownloadContent(episodeId: Int): EpisodeDownload? {
        return downloadContent[episodeId]
    }

    fun setCurrentAnime(anime: Anime?) {
        _currentAnime.value = anime
    }

    fun addAnime(animeCard: AnimeCard) {
        _animeList.value = _animeList.value.toMutableList().apply { add(animeCard) }
    }

    // Modifiers
    fun addAnimeToWatchlist(watchId: Int, animeId: Int): AniResult<Boolean> {
        val result = AniResult<Boolean>()
        result.then {
            if (it) _watchlist.run {
                value = value.map {  watchlist ->
                    if (watchlist.id == watchId)
                        watchlist.copy(series = watchlist.series.toMutableList().apply { add(animeId) })
                    else watchlist
                }
            }
        }
        Thread {
            if (_watchlist.value.any { it.id == watchId }) {
                try {
                    Thread.sleep(1000)
                } catch (_: Exception) {
                }
                result.pass(true)
            } else {
                result.pass(false)
            }
        }.start()
        return result
    }

    fun addWatchlist(watchId: Int): AniResult<Boolean> {
        val result = AniResult<Boolean>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(true)
        }.start()
        return result
    }

    fun createWatchlist(title: String, privacy: WatchlistPrivacy): AniResult<Watchlist> {
        val result = AniResult<Watchlist>()
        result.then {
            _watchlist.run {
                value = value.toMutableList() + it
            }
        }
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            val uuid = UUID.randomUUID()
            val mostSigBits = uuid.mostSignificantBits
            val leastSigBits = uuid.leastSignificantBits
            val id = (mostSigBits xor leastSigBits).toInt()
            result.pass(
                Watchlist(
                    id = id,
                    title = title,
                    privacy = privacy,
                    owner = getCurrentUser()!!.username
                )
            )
        }.start()
        return result
    }

    fun removeAnime(watchId: Int, animeId: Int): AniResult<Boolean> {
        val result = AniResult<Boolean>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(true)
        }.start()
        return result
    }

    fun removeWatchlist(watchId: Int): AniResult<Watchlist> {
        val result = AniResult<Watchlist>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(Watchlist())
        }.start()
        return result
    }

    fun updateWatchlist(watchId: Int, watchlist: Watchlist): AniResult<Watchlist> {
        val result = AniResult<Watchlist>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(watchlist)
        }.start()
        return result
    }

    fun canDownload(episodeId: Int, lang: AnimeTrack): Boolean {
        val episode = downloadContent[episodeId]
        return episode == null ||
                episode.let {
                    val file =
                        if (lang == AnimeTrack.SUB) it.subFile
                        else it.dubFile
                    !FileMaster.isDownloadExist(file)
                }
    }

    fun addAnimeDownload(
        anime: Anime,
        episode: AnimeEpisodeDetail,
    ): EpisodeDownload {
        val folder = "${ anime.title.english }${ File.separator }"

        val episodeDownload = EpisodeDownload(
            id = episode.id,
            animeId = anime.id.zoroId,
            title = episode.title,
            num = episode.episode,
            subFile = folder + "EP${ episode.episode }-SUB-UHD_${ anime.title.english }",
            dubFile = folder + "EP${ episode.episode }-DUB-UHD_${ anime.title.english }"
        )
        downloadContent[episodeDownload.id] = episodeDownload

        val download = (_animeDownload.value.find {
            it.animeId.zoroId == anime.id.zoroId
        } ?: AnimeDownload(
            animeId = anime.id,
            title = anime.title.english,
            image = anime.image,
            type = anime.type,
        )).let {
            it.copy(
                content = it.content.filter { epId -> epId != episode.id},
                ongoingContent =
                    if (it.ongoingContent.contains(episode.id)) it.ongoingContent
                    else it.ongoingContent.toMutableList().apply { add(episode.id) }
            )
        }

        _animeDownload.apply {
            value = value.toMutableList().apply {
                if (!any { it.animeId.zoroId == download.animeId.zoroId }) add(download)
            }
        }

        return episodeDownload
    }

    fun completeDownload(animeId: Int, epId: Int) {
        _animeDownload.apply {
            value = value.map {
                if (it.animeId.zoroId == animeId)
                    it.copy(
                        content = it.content.toMutableList().apply {
                            if (!any { id -> id == epId }) add(epId)
                        },
                        ongoingContent = it.ongoingContent.filter { id -> id != epId }
                    )
                else it
            }
        }
    }

    fun removeAnimeDownload(animeId: Int, epId: Int): AniResult<EpisodeDownload> {
        val result = AniResult<EpisodeDownload>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(EpisodeDownload())
        }.start()
        return result
    }

    fun removeAnimeDownload(animeId: Int): AniResult<AnimeDownload> {
        val result = AniResult<AnimeDownload>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(AnimeDownload())
        }.start()
        return result
    }

    fun updateHistory(animeId: Int, history: HistoryEntry): AniResult<HistoryEntry> {
        val result = AniResult<HistoryEntry>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(HistoryEntry())
        }.start()
        return result
    }

    fun updateHistory(mangaId: String, history: HistoryEntry): AniResult<HistoryEntry> {
        val result = AniResult<HistoryEntry>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(HistoryEntry())
        }.start()
        return result
    }
}