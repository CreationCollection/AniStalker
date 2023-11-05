package com.redline.anistalker.managements

import android.content.Context
import androidx.core.os.CancellationSignal
import com.redline.anistalker.managements.downloadSystem.DownloadTask
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.AnimeEpisode
import com.redline.anistalker.models.AnimeId
import com.redline.anistalker.models.AnimeTitle
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.AnimeType
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.models.VideoRange
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.models.WatchlistPrivacy
import com.redline.anistalker.utils.combineAsPath
import com.redline.anistalker.utils.getSafeFloat
import com.redline.anistalker.utils.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

object FileMaster {
    private lateinit var baseLocation: File

    private val animeCardLocation = "animeCards"
    private val watchlistLocation = "watchlist"

    private val downloads = "downloads"
    private val downloadEntry = "entries"
    private val downloadContent = "contents"
    private val downloadSources = "sources"
    private val downloadSegments = "segments"

    fun initialize(context: Context) {
        baseLocation = context.filesDir
    }


    fun readAllAnimeCards(): List<AnimeCard> {
        val file = File(baseLocation, animeCardLocation)
        val list = mutableListOf<AnimeCard>()

        file.listFiles()?.forEach {
            list.add(JSONObject(read(it)).toAnimeCard())
        }

        return list
    }

    fun readAllWatchlist(): List<Watchlist> {
        val file = File(baseLocation, watchlistLocation)
        val list = mutableListOf<Watchlist>()

        file.listFiles()?.forEach {
            list.add(JSONObject(read(it)).toWatchlist())
        }

        return list
    }

    fun readAllDownloadEntries(): List<AnimeDownload> {
        val file = File(baseLocation, downloads.combineAsPath(downloadEntry))
        val list = mutableListOf<AnimeDownload>()

        file.listFiles()?.forEach {
            list.add(JSONObject(read(it)).toAnimeDownload())
        }

        return list
    }

    fun readAllDownloadContent(): List<EpisodeDownload> {
        val file = File(baseLocation, downloads.combineAsPath(downloadContent))
        val list = mutableListOf<EpisodeDownload>()

        file.listFiles()?.forEach {
            list.add(JSONObject(read(it)).toEpisodeDownload())
        }

        return list
    }

    fun readAllDownloadSources(source: (String) -> Unit) {
        val file = File(baseLocation, downloads.combineAsPath(downloadSources))
        file.listFiles()?.forEach {
            source(read(it))
        }
    }

    // region Writers
    // ==========
    // Writers
    // ==========
    fun write(watchlist: Watchlist) {
        val file = File(baseLocation, watchlistLocation.combineAsPath(watchlist.id.toString()))
        val data = watchlist.toJSON().toString(4)
        write(file, data)
    }


    fun write(animeCard: AnimeCard) {
        val file = File(baseLocation, animeCardLocation.combineAsPath(animeCard.id.toString()))
        val data = animeCard.toJSON().toString(4)
        write(file, data)
    }

    fun write(animeDownload: AnimeDownload) {
        val file =
            File(baseLocation, downloads.combineAsPath(downloadEntry, animeDownload.animeId.zoroId.toString()))
        val data = animeDownload.toJSON().toString(4)
        write(file, data)
    }

    fun write(episodeDownload: EpisodeDownload) {
        val file = File(
            baseLocation,
            downloads.combineAsPath(downloadContent, episodeDownload.id.toString())
        )
        val data = episodeDownload.toJSON().toString()
        write(file, data)
    }

    fun write(downloadTask: DownloadTask) {
        val file = File(
            baseLocation,
            downloads.combineAsPath(downloadSources, downloadTask.episodeId.toString())
        )
        val data = downloadTask.toString()
        write(file, data)
    }

    fun writeDownloadSegment(
        uid: String,
        stream: InputStream,
        cancelSignal: CancellationSignal,
        callback: (length: Long) -> Unit,
    ) {
        val file = File(
            baseLocation,
            downloads.combineAsPath(downloadSegments, uid)
        )
        write(file, stream, cancelSignal, callback = callback)
    }
    // endregion


    fun deleteDownloadSegments(segments: List<String>) {
        segments.forEach {
            val file = File(
                baseLocation,
                downloads.combineAsPath(downloadSegments, it)
            )
            delete(file)
        }
    }

    fun delete(value: DownloadTask) {
        val file =
            File(
                baseLocation,
                downloads.combineAsPath(downloadSources, value.episodeId.toString())
            )
        delete(file)
    }


    fun segmentsIntoFile(links: List<String>, filename: String, callback: (length: Long) -> Unit) {
        val org = File("".combineAsPath(filename) + ".ts")
        val target = File("".combineAsPath(filename) + ".stalk.lock")

        val size = 4 * 1024
        val buffer = ByteArray(size)
        var len = 0

        var output: FileOutputStream? = null
        var channel: FileChannel? = null
        var lock: FileLock? = null

        try {
            output = target.outputStream()
            channel = output.channel
            lock = channel.lock()

            links.forEach {
                val stream = File(
                    baseLocation,
                    downloads.combineAsPath(downloadSegments, it)
                ).inputStream()

                while (stream.read(buffer, 0, size).also { l -> len = l } > 0) {
                    output.write(buffer, 0, len)
                    callback(len.toLong())
                }

                stream.close()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            if (lock != null && lock.isValid) lock.release()
            channel?.close()
            output?.close()
        }

        target.renameTo(org)
    }


    fun delete(file: File) {
        file.delete()
    }

    fun read(file: File): String {
        val reader = BufferedReader(InputStreamReader(file.inputStream()))
        val builder = StringBuilder()
        var line: String

        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
        }

        return builder.toString()
    }

    fun write(file: File, data: String) {
        if (file.exists()) {
            try {
                val temp = File(file.absolutePath + ".temp")
                val org = File(file.absolutePath + ".org")

                write(temp, data)

                file.renameTo(org)
                temp.renameTo(file)
                org.delete()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        } else {
            var stream: FileOutputStream? = null
            var channel: FileChannel? = null
            var lock: FileLock? = null

            try {
                stream = file.outputStream()
                channel = stream.channel
                lock = channel.lock()

                val writer = BufferedWriter(OutputStreamWriter(stream))
                writer.write(data)
            } catch (ex: IOException) {
                ex.printStackTrace()
            } finally {
                if (lock != null && lock.isValid) lock.release()
                channel?.close()
                stream?.close()
            }
        }
    }

    fun write(
        file: File,
        stream: InputStream,
        cancelSignal: CancellationSignal,
        callback: (length: Long) -> Unit,
    ) {
        val size = 4 * 1024
        val buffer = ByteArray(size)
        var len = 0

        var output: FileOutputStream? = null
        var channel: FileChannel? = null
        var lock: FileLock? = null

        try {
            output = file.outputStream()
            channel = output.channel
            lock = channel.lock()

            while (stream.read(buffer, 0, size).also { len = it } > 0) {
                if (cancelSignal.isCanceled) break
                output.write(buffer, 0, len)
                callback(len.toLong())
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            if (lock != null && lock.isValid) lock.release()
            channel?.close()
            output?.close()
        }
    }
}


private fun AnimeEpisode.toJSON(): JSONObject {
    return JSONObject().apply {
        put("total", total)
        put("sub", sub)
        put("dub", dub)
    }
}

private fun JSONObject.toEpisodes(): AnimeEpisode {
    return AnimeEpisode(getInt("sub"), getInt("dub"), getInt("total"))
}

private fun AnimeId.toJSON(): JSONObject {
    return JSONObject().apply {
        put("zoroId", zoroId)
        put("aniId", anilistId)
        put("malId", malId)
    }
}

private fun JSONObject.toAnimeId(): AnimeId {
    return AnimeId(
        zoroId = getInt("zoroId"),
        anilistId = getInt("aniId"),
        malId = getInt("malId")
    )
}

private fun VideoRange.toJSON(): JSONObject {
    return JSONObject().apply {
        put("start", start)
        put("end", end)
    }
}

private fun JSONObject.toVideoRange(): VideoRange {
    return VideoRange(
        start = getInt("start"),
        end = getInt("end")
    )
}

private fun EpisodeDownload.toJSON(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("animeId", animeId.toJSON())
        put("title", title)
        put("relation", relation)
        put("num", num)
        put("lang", language.name)
        put("quality", quality.name)
        put("intro", intro.toJSON())
        put("outro", outro.toJSON())
        put("duration", duration.toDouble())
        put("size", size)
    }
}

private fun JSONObject.toEpisodeDownload(): EpisodeDownload {
    return EpisodeDownload(
        id = getInt("id"),
        animeId = getJSONObject("animeId").toAnimeId(),
        title = getString("title"),
        relation = getString("relation"),
        num = getInt("num"),
        language = AnimeTrack.valueOf(getString("lang")),
        quality = VideoQuality.valueOf(getString("quality")),
        intro = getJSONObject("intro").toVideoRange(),
        outro = getJSONObject("outro").toVideoRange(),
        duration = getDouble("duration").toFloat(),
        size = getLong("size")
    )
}

private fun AnimeCard.toJSON(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("title", JSONObject().apply {
            put("english", name.english)
            put("userPreferred", name.userPreferred)
        })
        put("image", image)
        put("type", type.name)
        put("episodes", episodes.toJSON())
        put("isAdult", isAdult)
        put("owner", owner)
    }
}

private fun JSONObject.toAnimeCard(): AnimeCard {
    return AnimeCard(
        id = getInt("id"),
        name = getJSONObject("title").run {
            AnimeTitle(getString("english"), getString("userPreferred"))
        },
        image = getString("image"),
        type = AnimeType.valueOf(getString("type")),
        episodes = getJSONObject("episodes").toEpisodes(),
        isAdult = getBoolean("isAdult"),
        owner = getString("owner")
    )
}

private fun Watchlist.toJSON(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("title", title)
        put("image", image)
        put("privacy", privacy.name)
        put("owner", owner)
        put("provider", provider)
        put("series", JSONArray().apply {
            series.forEach { put(it) }
        })
        put("following", following)
    }
}

private fun JSONObject.toWatchlist(): Watchlist {
    return Watchlist(
        id = getInt("id"),
        title = getString("title"),
        image = getString("image"),
        privacy = WatchlistPrivacy.valueOf(getString("privacy")),
        owner = getString("owner"),
        provider = getString("provider"),
        series = getJSONArray("series").map { getInt(it) },
        following = getInt("following")
    )
}

private fun AnimeDownload.toJSON(): JSONObject {
    return JSONObject().apply {
        put("animeId", animeId.toJSON())
        put("title", title)
        put("image", image)
        put("episodes", JSONObject().apply {
            put("total", episodes.total)
            put("sub", episodes.sub)
            put("dub", episodes.dub)
        })
        put("duration", duration)
        put("size", size)
        put("content", JSONArray().apply {
            content.onEach {
                put(it)
            }
        })
        put("ongoingContent", JSONArray().apply {
            ongoingContent.forEach {
                put(it)
            }
        })
    }
}

private fun JSONObject.toAnimeDownload(): AnimeDownload {
    return AnimeDownload(
        animeId = getJSONObject("animeId").toAnimeId(),
        title = getString("title"),
        image = getString("image"),
        episodes = getJSONObject("episodes").toEpisodes(),
        duration = getSafeFloat("duration"),
        size = getLong("size"),
        content = getJSONArray("content").map { getInt(it) },
        ongoingContent = getJSONArray("ongoingContent").map { getInt(it) }
    )
}