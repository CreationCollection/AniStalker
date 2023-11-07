package com.redline.anistalker.managements

import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.Event
import com.redline.anistalker.models.HistoryEntry
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.models.WatchlistPrivacy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserInfo(val username: String, val name: String)

object UserData {
    private var userInfo: UserInfo? = null

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

    fun getHistoryEntry(animeId: Int): HistoryEntry {
        return HistoryEntry()
    }

    fun getHistoryEntry(mangaId: String): HistoryEntry {
        return HistoryEntry()
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

    fun addAnimeDownload(
        animeId: Int,
        epId: Int,
        quality: VideoQuality,
        track: AnimeTrack
    ): AniResult<EpisodeDownload> {
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

    fun completeDownload(animeId: Int, epId: Int): AniResult<EpisodeDownload> {
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