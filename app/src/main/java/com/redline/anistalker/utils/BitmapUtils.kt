@file:Suppress("DEPRECATION")

package com.redline.anistalker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

fun Context.blurImage(image: Bitmap, radius: Float = 20f): Bitmap {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        val bitmap = image.copy(image.config, true)
        val rs = RenderScript.create(this)
        val bitmapAlloc = Allocation.createFromBitmap(rs, image)
        val output = Allocation.createTyped(rs, bitmapAlloc.type)
        ScriptIntrinsicBlur.create(rs, bitmapAlloc.element).apply {
            setRadius(radius)
            setInput(bitmapAlloc)
            forEach(output)
        }
        output.copyTo(bitmap)
        rs.destroy()
        bitmap
    } else {
        image
    }
}

fun Context.blurImageResource(image: Int, radius: Float = 25f): Bitmap {
    return blurImage(BitmapFactory.decodeResource(resources, image), radius)
}