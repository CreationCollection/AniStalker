package com.redline.anistalker.utils

import kotlin.math.roundToInt


private val sizeLabels = arrayOf( "B", "KB", "MB", "GB" )
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
    else if (this < 60*60) "${ (this / 60).roundToInt() } Minutes"
    else "${ (this / (60*60)).roundToInt() } Hours"
}