package com.redline.anistalker.utils

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt


private val sizeLabels = arrayOf("B", "KB", "MB", "GB")
fun Long.toSizeFormat(): String {
    var value = this.toFloat()
    var label = "B"

    for (i in sizeLabels) {
        label = i
        if (value < 1024.0) break

        value /= 1024.0f
    }

    val formattedValue = if (value % 1 == 0.0f) {
        value.toInt().toString()
    } else {
        String.format("%.1f", value)
    }

    return "$formattedValue $label"
}

fun Float.toDurationFormat(): String {
    return if (this < 60) "$this Seconds"
    else if (this < 60 * 60) "${(this / 60).roundToInt()} Minutes"
    else "${(this / (60 * 60)).roundToInt()} Hours"
}

fun Float.wrap(min: Float, max: Float): Float {
    val range = max - min
    var wrappedValue = (this - min) % range

    if (wrappedValue < 0.0f) {
        wrappedValue += range
    }

    return wrappedValue + min
}

fun Float.clampedNormalize(min: Float, max: Float): Float {
    val clampedValue = when {
        this < min -> min
        this > max -> max
        else -> this
    }

    return (clampedValue - min) / (max - min)
}

fun String.toTitleCase(): String {
    val strings = lowercase().split("_").map {
        it.replaceFirstChar { c -> c.uppercaseChar() }
    }
    return strings.joinToString(" ")
}

fun <T> List<T>.fill(count: Int, items: (Int) -> T): List<T> {
    return toMutableList().apply {
        repeat(count) {
            add(items(it))
        }
    }
}

fun <T> fillList(count: Int, items: (Int) -> T): List<T> {
    return mutableListOf<T>().fill(count, items)
}


// JSON Extensions
fun JSONObject.getSafeString(key: String, default: String = "null"): String {
    return if (isNull(key)) default else getString(key)
}

fun JSONObject.getStringOrNull(key: String): String? = if (isNull(key)) null else getString(key)

fun JSONObject.getSafeInt(key: String, default: Int = 0): Int {
    return if (isNull(key)) default else getInt(key)
}

fun JSONObject.getIntOrNull(key: String): Int? {
    return if (isNull(key)) null else getInt(key)
}

fun JSONObject.getSafeBoolean(key: String, default: Boolean = false) =
    if (isNull(key)) default else getBoolean(key)

fun JSONObject.getSafeFloat(key: String, default: Float = 0f) =
    if (isNull(key)) default else getDouble(key).toFloat()

fun<T> JSONArray.map(mapping: JSONArray.(index: Int) -> T): List<T> {
    val list = mutableListOf<T>()
    for (i in 0 until length()) {
        list.add(this.mapping(i))
    }
    return list
}