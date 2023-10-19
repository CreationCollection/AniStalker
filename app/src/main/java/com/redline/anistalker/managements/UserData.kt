package com.redline.anistalker.managements

import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.Event
import com.redline.anistalker.models.HistoryEntry
import com.redline.anistalker.models.MangaCard
import com.redline.anistalker.models.MangaChapter
import com.redline.anistalker.models.MangaDownload
import com.redline.anistalker.models.MangaDownloadContent
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.models.WatchlistPrivacy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

object UserData {
    private val _watchlist =
        MutableStateFlow<List<Watchlist>>(mutableListOf<Watchlist>().apply {
            for (i in 0..30) {
                add(Watchlist())
            }
        })
    val watchlist = _watchlist.asStateFlow()

    private val _animeList =
        MutableStateFlow<List<AnimeCard>>(mutableListOf<AnimeCard>().apply {
            for (i in 0..100) {
                add(AnimeCard())
            }
        })
    val animeList = _animeList.asStateFlow()

    private val _mangaList =
        MutableStateFlow<List<MangaCard>>(mutableListOf<MangaCard>().apply {
            for (i in 0..16) {
                add(MangaCard())
            }
        })
    val mangaList = _mangaList.asStateFlow()

    private val _eventList =
        MutableStateFlow<List<Event>>(mutableListOf<Event>().apply {
            for (i in 0..100) {
                add(if (Random.nextBoolean()) Event.AnimeEvent() else Event.MangaEvent())
            }
        })
    val eventList = _eventList.asStateFlow()

    private val _animeDownload =
        MutableStateFlow<List<AnimeDownload>>(mutableListOf<AnimeDownload>().apply {
            for (i in 0..10) {
                add(AnimeDownload(
                    content = mutableMapOf<Int, List<EpisodeDownload>>().apply { 
                        for (x in 1..10) {
                            put(x, mutableListOf<EpisodeDownload>().apply { 
                                for (y in 1..Random.nextInt(50)) {
                                    add(EpisodeDownload(id = y))
                                }
                            })
                        }
                    },
                    ongoingContent = mutableListOf<EpisodeDownload>().apply {
                        for (x in 0..4) {
                            add(EpisodeDownload())
                        }
                    }
                ))
            }
        })
    val animeDownload = _animeDownload.asStateFlow()

    private val _mangaDownload =
        MutableStateFlow<List<MangaDownload>>(mutableListOf<MangaDownload>().apply {
            for (i in 0..6) {
                add(
                    MangaDownload(
                        chapters = mutableListOf<MangaChapter>().apply {
                            for (x in 0..10) {
                                add(MangaChapter())
                            }
                        }
                    )
                )
            }
        })
    val mangaDownload = _mangaDownload.asStateFlow()

    fun getCurrentWatchAnime(): StateFlow<Anime> {
        return MutableStateFlow(Anime())
    }

    fun getHistoryEntry(animeId: Int): HistoryEntry {
        return HistoryEntry()
    }

    fun getHistoryEntry(mangaId: String): HistoryEntry {
        return HistoryEntry()
    }

    // Modifiers
    fun addAnime(watchId: Int, animeId: Int): AniResult<Boolean> {
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
                    privacy = privacy
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

    fun addManga(mangaId: String): AniResult<MangaCard> {
        val result = AniResult<MangaCard>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(MangaCard())
        }.start()
        return result
    }

    fun removeManga(mangaId: String): AniResult<MangaCard> {
        val result = AniResult<MangaCard>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(MangaCard())
        }.start()
        return result
    }

    fun addChapterDownloads(
        mangaId: String,
        chapterId: List<String>
    ): AniResult<List<MangaDownloadContent>> {
        val result = AniResult<List<MangaDownloadContent>>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(mutableListOf<MangaDownloadContent>().apply {
                addAll(chapterId.map {
                    MangaDownloadContent(id = it)
                })
            })
        }.start()
        return result
    }

    fun removeChapterDownloads(
        mangaId: String,
        chapterId: List<String>
    ): AniResult<List<MangaDownloadContent>> {
        val result = AniResult<List<MangaDownloadContent>>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(mutableListOf<MangaDownloadContent>().apply {
                addAll(chapterId.map {
                    MangaDownloadContent(id = it)
                })
            })
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