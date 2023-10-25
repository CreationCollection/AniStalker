package com.redline.anistalker.managements.helper

import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDate
import com.redline.anistalker.models.AnimeEpisode
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeId
import com.redline.anistalker.models.AnimeRelation
import com.redline.anistalker.models.AnimeSeason
import com.redline.anistalker.models.AnimeSpotlight
import com.redline.anistalker.models.AnimeStatus
import com.redline.anistalker.models.AnimeTitle
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.AnimeType
import com.redline.anistalker.models.Subtitle
import com.redline.anistalker.models.Video
import com.redline.anistalker.models.VideoFile
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.models.VideoRange
import com.redline.anistalker.models.VideoSegment
import com.redline.anistalker.utils.getSafeBoolean
import com.redline.anistalker.utils.getSafeFloat
import com.redline.anistalker.utils.getSafeInt
import com.redline.anistalker.utils.getSafeString
import com.redline.anistalker.utils.map
import org.json.JSONArray
import org.json.JSONObject

object AnimeMediaExtractor {
    fun extractAnimeId(it: JSONObject): AnimeId {
        return AnimeId(
            it.getSafeInt("zoroId"),
            it.getSafeInt("aniId"),
            it.getSafeInt("malId")
        )
    }
    fun extractAnimeTitle(it: JSONObject): AnimeTitle =
        AnimeTitle(it.getSafeString("english"), it.getSafeString("userPreferred"))
    fun extractEpisode(it: JSONObject): AnimeEpisode {
        return AnimeEpisode(
            it.getSafeInt("sub", 0),
            it.getSafeInt("dub", 0),
            it.getSafeInt("total", 0)
        )
    }
    fun extractDate(it: JSONObject): AnimeDate {
        return AnimeDate(
            date = it.getSafeInt("date"),
            month = it.getSafeInt("month"),
            year = it.getSafeInt("year"),
        )
    }

    fun extractRelation(it: JSONObject): AnimeRelation {
        return AnimeRelation(
            zoroId = it.getSafeInt("zoroId"),
            image = it.getSafeString("image"),
            title = it.getSafeString("title")
        )
    }

    fun extractEpisodeDetails(it: JSONObject): AnimeEpisodeDetail =
        AnimeEpisodeDetail(
            id = it.getSafeInt("id"),
            episode = it.getSafeInt("episode"),
            title = it.getSafeString("title"),
            isFiller = it.getSafeBoolean("isFiller", false)
        )

    fun extractVideoRange(it: JSONObject): VideoRange =
        VideoRange(
            start = it.getSafeInt("start", 0),
            end = it.getSafeInt("end", 0),
        )

    fun extractSubtitle(it: JSONObject): Subtitle =
        Subtitle(
            lang = "en",
            url = it.getSafeString("url")
        )


    fun extractVideoLinks(it: JSONObject, quality: VideoQuality): VideoFile {
        val videoQuality = if (quality == VideoQuality.HD) "hd" else "uhd"
        val isJSON = it.get(videoQuality)

        var url = ""
        val files = mutableListOf<VideoSegment>()

        when (isJSON) {
            true -> it.getJSONObject(videoQuality).run {
                url = getSafeString("url")
                getJSONArray("files").map {
                    val obj = getJSONObject(it)
                    files.add(
                        VideoSegment(
                            length = obj.getSafeFloat("length"),
                            at = obj.getSafeFloat("at"),
                            url = obj.getSafeString("file"),
                            file = "",
                        )
                    )
                }
            }
            false -> url = it.getSafeString(videoQuality)
        }

        return VideoFile(
            master = url,
            files = files,
        )
    }

    fun extractStreamingVideo(it: JSONObject) =
        Video(
            intro = extractVideoRange(it.getJSONObject("intro")),
            outro = extractVideoRange(it.getJSONObject("outro")),
            track = it.getSafeString("track", "sub").run { AnimeTrack.valueOf(uppercase()) },
            subtitle = it.getJSONArray("subtitles").map { extractSubtitle(getJSONObject(it)) },
            hd = extractVideoLinks(it.getJSONObject("video"), VideoQuality.HD),
            uhd = extractVideoLinks(it.getJSONObject("video"), VideoQuality.UHD),
        )

    fun makeAnimeCard(value: JSONObject): AnimeCard {
        val id = value.getJSONObject("id").run {
            getInt("zoroId")
        }
        val title = value.getJSONObject("title").run {
            AnimeTitle(getSafeString("english"), getSafeString("userPreferred"))
        }
        val type = value.getSafeString("type", "ALL").let { AnimeType.valueOf(it) }
        val image = value.getSafeString("image", "")
        val episodes = value.getJSONObject("episodes").run {
            AnimeEpisode(
                getSafeInt("sub", 0),
                getSafeInt("dub", 0),
                getSafeInt("total", 0)
            )
        }
        val isAdult = value.getSafeBoolean("isAdult", false)
        val duration = value.getSafeInt("duration", 0)

        return AnimeCard(
            id = id,
            name = title,
            type = type,
            image = image,
            episodes = episodes,
            isAdult = isAdult,
            year = duration,
        )
    }
    fun makeAnimeSpotlight(value: JSONObject): AnimeSpotlight {
        val id = value.getJSONObject("id").run {
            getInt("zoroId")
        }
        val title = value.getJSONObject("title").run {
            AnimeTitle(getSafeString("english"), getSafeString("userPreferred"))
        }
        val type = value.getSafeString("type", "ALL").let { AnimeType.valueOf(it) }
        val image = value.getSafeString("image", "")
        val episodes = value.getJSONObject("episodes").run {
            AnimeEpisode(
                getSafeInt("sub", 0),
                getSafeInt("dub", 0),
                getSafeInt("total", 0)
            )
        }
        val rank = value.getSafeInt("rank", 0)

        return AnimeSpotlight(
            id = id,
            title = title,
            image = image,
            episodes = episodes,
            type = type,
            rank = rank,
        )
    }

    fun makeAnime(value: JSONObject): Anime {
        val id = extractAnimeId(value.getJSONObject("id"))
        val title = extractAnimeTitle(value.getJSONObject("title"))
        val type = value.getSafeString("type", "ALL").let { AnimeType.valueOf(it) }
        val image = value.getSafeString("image", "")
        val episodes = extractEpisode(value.getJSONObject("episodes"))
        val isAdult = value.getSafeBoolean("isAdult", false)
        val year = extractDate(value.getJSONObject("start"))
        val end = extractDate(value.getJSONObject("end"))
        val season = value.getSafeString("season", "ALL")
            .let { AnimeSeason.valueOf(it.uppercase()) }
        val status = value.getSafeString("status", "ALL")
            .let { AnimeStatus.values().find { v -> it.contains(v.value) } ?: AnimeStatus.ALL }
        val description = value.getSafeString("description")
        val otherNames = value.getJSONArray("otherNames").map { getString(it) }

        val relations = value.getJSONArray("relations").map {
            extractRelation(getJSONObject(it))
        }
        val genres = value.getJSONArray("genres").map { getString(it) }

        return Anime(
            id = id,
            title = title,
            type = type,
            year = year.year,
            end = end,
            season = season,
            status = status,
            image = image,
            description = description,
            otherNames = otherNames,
            episodes = episodes,
            relations = relations,
            isAdult = isAdult,
            genres = genres,
        )
    }
}