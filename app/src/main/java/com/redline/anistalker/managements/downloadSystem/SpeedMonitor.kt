package com.redline.anistalker.managements.downloadSystem

import com.redline.anistalker.utils.toSizeFormat

class SpeedMonitor(private val smoothness: Int = 8) {
    private var speedList = mutableListOf<Long>()

    fun consumeBytes(bytes: Long) {
        speedList.add(bytes)
        if (speedList.size > smoothness) speedList.removeFirst()
    }

    fun reset() {
        speedList.clear()
    }

    fun get(): Long {
        return speedList.average().toLong()
    }

    override fun toString(): String {
        return get().toSizeFormat()
    }
}