package com.redline.anistalker.managements

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import com.redline.anistalker.models.AniErrorMessage
import com.redline.anistalker.models.AniResult
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.AnimeEpisode
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.models.WatchlistPrivacy
import com.redline.anistalker.utils.ExecutionFlow
import com.redline.anistalker.utils.KeyExecutionFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

val PREF_USER = "PREFERENCE_USER"
val PREF_USER_TOKEN = "PREFERENCE_USER_TOKEN"
val PREF_CURRENT_ANIME = "PREFERENCE_CURRENT_ANIME"

data class UserInfo(val username: String, val name: String) {
    override fun toString(): String {
        return JSONObject().apply {
            put("username", username)
            put("name", name)
        }.toString()
    }

    companion object {
        const val USERINFO = "USERINFO"

        fun toUserInfo(value: String): UserInfo {
            val json = JSONObject(value)
            return UserInfo(
                username = json.getString("username"),
                name = json.getString("name"),
            )
        }
    }
}

object UserData {
    private var initialized = false

    private var userInfo: UserInfo? = null
    private var userAuthToken: String? = null

    private val workScope = CoroutineScope(Dispatchers.IO)
    private val downloadHandleFlow = ExecutionFlow(1, workScope)
    private val operationFlow = KeyExecutionFlow<Int>(1, workScope)

    private val _currentAnime = MutableStateFlow<Anime?>(null)
    val currentAnime = _currentAnime.asStateFlow()

    private val _watchlist =
        MutableStateFlow<List<Watchlist>>(emptyList())
    val watchlist = _watchlist.asStateFlow()

    private val _animeList =
        MutableStateFlow<List<AnimeCard>>(emptyList())
    val animeList = _animeList.asStateFlow()

    private val _animeDownload =
        MutableStateFlow<List<AnimeDownload>>(emptyList())
    val animeDownload = _animeDownload.asStateFlow()

    private val downloadContent = mutableMapOf<Int, EpisodeDownload>()

    fun initialize(context: Context) {
        if (initialized) return

        initialized = true
        val pref = context.getSharedPreferences(PREF_USER, MODE_PRIVATE)
        val userInfoString = pref.getString(UserInfo.USERINFO, null)
        val currentAnimeString = pref.getString(PREF_CURRENT_ANIME, null)

//        userInfo = userInfoString ?.let { UserInfo.toUserInfo(it) }
        userAuthToken = pref.getString(PREF_USER_TOKEN, null)
        _currentAnime.value = currentAnimeString?.let { Anime.toAnime(it) }

        userInfo = UserInfo("Anmol011", "Anmol Kashyap")
        _animeList.value = FileMaster.readAllAnimeCards()
        _watchlist.value = FileMaster.readAllWatchlist()

        FileMaster.readAllDownloadContent().forEach {
            downloadContent[it.id] = it
        }
        _animeDownload.value = FileMaster.readAllDownloadEntries().map {
            var size = 0L
            var duration = 0f
            var totalEp = 0
            var subEp = 0
            var dubEp = 0

            it.content.forEach { epId ->
                downloadContent[epId]?.let { episode ->
                    size += episode.size
                    duration += episode.duration
                    totalEp++
                    if (episode.track == AnimeTrack.SUB) subEp++
                    else dubEp++
                }
            }

            it.copy(
                size = size,
                duration = duration,
                episodes = AnimeEpisode(total = totalEp, sub = subEp, dub = dubEp)
            )
        }

        CoroutineScope(Dispatchers.Default).launch {
            _currentAnime.collect {
                it?.run {
                    pref.edit {
                        putString(PREF_CURRENT_ANIME, it.toString())
                    }
                }
            }
        }
    }

    fun getCurrentUser(): UserInfo? = userInfo

    fun getDownloadContent(id: Int): EpisodeDownload? {
        return downloadContent[id]
    }

    fun setCurrentAnime(anime: Anime?) {
        _currentAnime.value = anime
    }

    fun addAnime(animeCard: AnimeCard) {
        FileMaster.write(animeCard)
        _animeList.value = _animeList.value.toMutableList().apply { add(animeCard) }
    }

    // Modifiers
    fun addAnimeToWatchlist(watchId: Int, animeId: Int): AniResult<Boolean> {
        val result = AniResult<Boolean>()
        result.then { pass ->
            if (pass) {
                val watch = _watchlist.value.find { it.id == watchId }?.let {
                    it.copy(
                        series = it.series.toMutableList().apply { add(animeId) }
                    )
                }
                watch?.also {
                    FileMaster.write(it)
                    _watchlist.run {
                        value = value.map { watchlist ->
                            if (watchlist.id == watchId) watch
                            else watchlist
                        }
                    }
                }
            }
        }
        operationFlow.execute(watchId) {
            // TODO(Notify Server and proceed with response)
            if (!_watchlist.value.any { it.series.contains(animeId) }) result.pass(true)
            else result.pass(false)
        }
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
        workScope.launch {
            userInfo?.let { user ->
                //TODO("Send this to server and proceed with response)
                val watchlist = Watchlist(
                    id = System.currentTimeMillis().toInt(),
                    title = title,
                    privacy = privacy,
                    owner = user.name
                )

                FileMaster.write(watchlist)
                result.pass(watchlist)
            } ?: result.reject(
                AniErrorMessage(AniErrorCode.INVALID_VALUE, "No User Logged In Yet")
            )
        }
        return result
    }

    fun removeAnime(watchId: Int, animeId: Int): AniResult<Boolean> {
        val result = AniResult<Boolean>()
        operationFlow.execute(watchId) {
            //TODO("Notify database and proceed with response")
            val watch = _watchlist.value.find { it.id == watchId }?.let {
                it.copy(series = it.series.filterNot { aId -> aId == animeId })
            }
            watch?.also {
                FileMaster.write(watch)
                _watchlist.apply {
                    value = value.map {
                        if (it.id == watchId) watch
                        else it
                    }
                }
                result.pass(true)
            } ?: result.reject(AniErrorMessage(AniErrorCode.NOT_FOUND, "Watchlist Not Found!"))
        }
        return result
    }

    fun removeWatchlist(watchId: Int): AniResult<Watchlist> {
        val result = AniResult<Watchlist>()
        operationFlow.execute(watchId) {
            // TODO("Archive this watchlist and proceed with response")
            val watch = _watchlist.value.find { it.id == watchId }

            watch?.also {
                FileMaster.delete(watch)
                _watchlist.apply {
                    value = value.filterNot { i -> i.id == watch.id }
                }
                result.pass(it)
            } ?: result.reject(AniErrorMessage(AniErrorCode.INVALID_VALUE, "No Watchlist Found!"))
        }
        return result
    }

    fun updateWatchlist(
        watchId: Int,
        title: String,
        privacy: WatchlistPrivacy
    ): AniResult<Watchlist> {
        val result = AniResult<Watchlist>()
        operationFlow.execute(watchId) {
            // TODO("Notify Server and proceed with response")
            // Demo
            val watch = _watchlist.value.find { it.id == watchId }?.copy(
                title = title,
                privacy = privacy
            )
            // Demo
            watch?.let {
                FileMaster.write(watch)
                _watchlist.apply {
                    value = value.map {
                        if (it.id == watchId) watch
                        else it
                    }
                }
                result.pass(Watchlist())
            } ?: result.reject(AniErrorMessage(AniErrorCode.INVALID_VALUE, "No Watchlist Found!"))
        }
        return result
    }

    fun canDownload(episodeId: Int, lang: AnimeTrack): Boolean {
        val episode = downloadContent[trackSign(lang) * episodeId]
        return episode == null ||
                episode.let {
                    !FileMaster.isDownloadExist(it.file)
                }
    }

    fun addAnimeDownload(
        anime: Anime,
        episode: AnimeEpisodeDetail,
        lang: AnimeTrack,
    ): EpisodeDownload {
        val downloadId = trackSign(lang) * episode.id
        val folder = "${anime.title.english}${File.separator}"

        val episodeDownload = EpisodeDownload(
            id = downloadId,
            episodeId = episode.id,
            animeId = anime.id.zoroId,
            title = episode.title,
            num = episode.episode,
            track = lang,
            file = folder + "EP${episode.episode}-${lang.value}-UHD_${anime.title.english}"
        )
        downloadContent[downloadId] = episodeDownload

        val download = (_animeDownload.value.find {
            it.animeId.zoroId == anime.id.zoroId
        } ?: AnimeDownload(
            animeId = anime.id,
            title = anime.title.english,
            image = anime.image,
            type = anime.type,
        )).let {
            it.copy(
                content = it.content - downloadId,
                ongoingContent =
                if (it.ongoingContent.contains(downloadId)) it.ongoingContent
                else it.ongoingContent + downloadId
            )
        }

        downloadHandleFlow.execute {
            _animeDownload.apply {
                value =
                    if (value.none { it.animeId.zoroId == download.animeId.zoroId }) value + download
                    else value
            }

            FileMaster.write(episodeDownload)
            FileMaster.write(download)
        }
        return episodeDownload
    }

    fun completeDownload(animeId: Int, downloadId: Int, duration: Float, size: Long) {
        downloadHandleFlow.execute {
            val downloadEp = downloadContent[downloadId]
            downloadEp?.let {
                downloadContent[downloadId] =
                    it.copy(duration = duration, size = size).also(FileMaster::write)
            }
            _animeDownload.apply {
                value = value.map {

                    if (it.animeId.zoroId == animeId) {
                        val episode = it.episodes.run {
                            AnimeEpisode(
                                total = total + 1,
                                sub = sub + if (downloadEp?.track == AnimeTrack.SUB) 1 else 0,
                                dub = dub + if (downloadEp?.track == AnimeTrack.DUB) 1 else 0,
                            )
                        }
                        it.copy(
                            content = if (downloadId !in it.content && downloadEp != null)
                                it.content + downloadId else it.content,
                            ongoingContent = if (downloadEp != null)
                                it.ongoingContent - downloadId else it.ongoingContent,
                            duration = it.duration + duration,
                            size = it.size + size,
                            episodes = episode,
                        ).also(FileMaster::write)
                    } else it
                }
            }
        }
    }

    fun removeAnimeDownload(animeId: Int, downloadId: Int) {
        downloadHandleFlow.execute {
            downloadContent.remove(downloadId)
            _animeDownload.value = _animeDownload.value.map {
                if (it.animeId.zoroId == animeId) {
                    it.copy(
                        content = if (downloadId !in it.content) it.content + downloadId else it.content,
                        ongoingContent = it.ongoingContent - downloadId
                    )
                } else it
            }
        }
    }

    fun removeAnimeDownload(animeId: Int) {
        downloadHandleFlow.execute {
            _animeDownload.apply {
                value.forEach {
                    it.content.forEach { epId -> downloadContent.remove(epId) }
                    it.ongoingContent.forEach { epId -> downloadContent.remove(epId) }
                }
                value = value.filterNot { it.animeId.zoroId == animeId }
            }
        }
    }

    fun updateLastEpisode(animeId: Int, episode: Int) {
        workScope.launch {
            try {
                //TODO("Send this to server")
            } catch (err: AniError) {
                err.printStackTrace()
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
    }

    private fun trackSign(track: AnimeTrack): Int = if (track == AnimeTrack.SUB) 1 else -1
}