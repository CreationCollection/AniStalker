package com.redline.anistalker.managements.helper

import com.redline.anistalker.models.Manga
import com.redline.anistalker.models.MangaCard
import com.redline.anistalker.models.MangaChapter
import com.redline.anistalker.utils.getIntOrNull
import com.redline.anistalker.utils.getSafeInt
import com.redline.anistalker.utils.getSafeString
import com.redline.anistalker.utils.getStringOrNull
import com.redline.anistalker.utils.map
import org.json.JSONObject

object MangaMediaExtractor {
    fun makeMangaCard(it: JSONObject): MangaCard {
        val id = it.getSafeString("id", "")
        val title = it.run {
            val altTitles = getJSONArray("altTitles")
            if (altTitles.length() > 0) altTitles.getString(0)
            else getSafeString("title")
        }
        val chapters = it.getSafeInt("lastChapter", 0)
        val coverId = it.getStringOrNull("coverId")

        return MangaCard(
            id = id,
            title = title,
            chapters = chapters,
            coverId = coverId,
        )
    }

    fun extractMangaDetails(it: JSONObject): Manga {
        val id = it.getSafeString("id", "")
        val (title, otherNames) = it.run {
            var title = ""
            val otherNames: MutableList<String> = mutableListOf()
            val altTitles = getJSONArray("altTitles")
            if (altTitles.length() > 0) {
                altTitles.map { getString(it) }.toCollection(otherNames)
                title = otherNames.removeFirst()
            }
            else getSafeString("title")
            Pair(title, otherNames)
        }
        val chapters = it.getIntOrNull("lastChapter")
        val coverId = it.getSafeString("coverId")
        val desc = it.getSafeString("desc")
        val (aniId, malId) = it.getJSONObject("mapping").run {
            Pair(
                getSafeInt("anilist"),
                getSafeInt("malId")
            )
        }
        val status = it.getSafeString("status", "completed")
        val type = it.getSafeString("type", "unknown")
        val year = it.getSafeInt("year")
        val genres = it.getJSONArray("genres").map {
            getString(it)
        }

        return Manga(
            id = id,
            aniListId = aniId,
            malId = malId,
            title = title,
            chapters = chapters,
            coverId = coverId,
            status = status,
            type = type,
            year = year,
            genres = genres,
            otherNames = otherNames,
            description = desc
        )
    }

    fun makeMangaChapter(it: JSONObject, mangaId: String): MangaChapter {
        val id = it.getSafeString("id")
        val title = it.getSafeString("title")

        return MangaChapter(
            id = id,
            mangaId = mangaId,
            mangaTitle = "",
            title = "",
            num = 0,
        )
    }
}