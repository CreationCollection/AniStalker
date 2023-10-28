package com.redline.anistalker.managements.helper

import android.graphics.BitmapFactory
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import javax.net.ssl.SSLException

object Net {
    private val workPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val client = OkHttpClient()

    // HTTP GET request
    fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        return requestForString(request)
    }

    fun getStream(url: String): InputStream {
        val request = Request.Builder()
            .url(url)
            .build()

        return executeRequest(request) { it?.byteStream() ?: throw IOException("Empty Body") }
    }

    // HTTP POST request
    fun post(url: String, body: RequestBody): String {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return requestForString(request)
    }

    // HTTP PUT request
    fun put(url: String, body: RequestBody): String {
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        return requestForString(request)
    }

    // HTTP DELETE request
    fun delete(url: String): String {
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        return requestForString(request)
    }

    fun cacheImage(url: String): Boolean {
        if (!url.startsWith("http://") &&
            !url.startsWith("https://")) return false

        workPool.execute {
            try {
                val stream = getStream(url)
                val bitmap = BitmapFactory.decodeStream(stream)
            } catch(ex: IOException) {
                ex.printStackTrace()
            }
        }

        return true
    }

    private fun requestForString(request: Request): String {
        return executeRequest(request) {
            it?.string() ?: ""
        }
    }

    private fun<Result> executeRequest(request: Request, handle: (ResponseBody?) -> Result): Result {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                return handle(response.body)
            }
            else throw IOException(response.code.toString())
        }
        catch (e: SocketTimeoutException) {
            val errorCode = AniErrorCode.SLOW_NETWORK_ERROR
            throw AniError(errorCode)
        }
        catch (e: IOException) {
            e.printStackTrace()
            val errorCode = AniErrorCode.UNKNOWN
            throw AniError(errorCode, e.message ?: errorCode.message)
        }
    }
}
