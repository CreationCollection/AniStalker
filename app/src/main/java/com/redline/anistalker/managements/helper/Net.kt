package com.redline.anistalker.managements.helper

import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import okhttp3.*
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

object Net {
    private val client = OkHttpClient()

    // HTTP GET request
    fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        return executeRequest(request)
    }

    // HTTP POST request
    fun post(url: String, body: RequestBody): String {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return executeRequest(request)
    }

    // HTTP PUT request
    fun put(url: String, body: RequestBody): String {
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        return executeRequest(request)
    }

    // HTTP DELETE request
    fun delete(url: String): String {
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        return executeRequest(request)
    }

    private fun executeRequest(request: Request): String {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body ?: throw IOException("Empty Body Received")
                return body.string()
            }
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
        return ""
    }
}
