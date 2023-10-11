package com.redline.anistalker.managements

import com.redline.anistalker.models.AniResult

data class UserAuthToken(
    val token: String = "",
    val refreshToken: String = "",
    val expiresIn: Int = 0,
    val username: String = "",
)

object UserAuth {
    fun login(username: String, password: String): AniResult<Boolean> {
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

    fun loginByGoogle(token: String): AniResult<Boolean> {
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

    fun signUp(username: String, password: String): AniResult<Boolean> {
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

    fun signUpByGoogle(username: String, token: String): AniResult<Boolean> {
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

    fun refreshToken(refreshToken: String): AniResult<UserAuthToken> {
        val result = AniResult<UserAuthToken>()
        Thread {
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {
            }
            result.pass(UserAuthToken())
        }.start()
        return result
    }
}