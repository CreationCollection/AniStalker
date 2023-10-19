package com.redline.anistalker.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AniErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val authUsername = "AUTH_USERNAME"
    private val authPassword = "AUTH_PASSWORD"
    private val authGoogleToken = "AUTH_GOOGLE_TOKEN"

    val username = savedStateHandle.getStateFlow(authUsername, "")
    val password = savedStateHandle.getStateFlow(authPassword, "")

    val googleToken = savedStateHandle.getStateFlow<String?>(authGoogleToken, null)

    fun saveUsername(username: String) {
        savedStateHandle[authUsername] = username
    }

    fun savePassword(password: String) {
        savedStateHandle[authPassword] = password
    }

    fun saveGoogleToken(token: String) {
        savedStateHandle[authGoogleToken] = token
    }

    suspend fun loginWithUsername(
        username: String = this.username.value,
        password: String = this.password.value
    ): Boolean {
        if (username.isBlank() || password.isBlank())
            throw AniError(AniErrorCode.INVALID_VALUE, "Provided Username or Password is invalid!")
        return withContext(Dispatchers.IO) {
            true
        }
    }

    suspend fun loginWithGoogle(
        googleToken: String? = this.googleToken.value
    ): Boolean {
        if (googleToken == null)
            throw AniError(AniErrorCode.INVALID_TOKEN)
        return withContext(Dispatchers.IO) {
            true
        }
    }

    suspend fun signInWithUsername(
        username: String = this.username.value,
        password: String = this.password.value
    ): Boolean {
        if (username.isBlank() || password.isBlank())
            throw AniError(AniErrorCode.INVALID_VALUE, "Provided Username or Password is invalid!")
        return withContext(Dispatchers.IO) {
            true
        }
    }

    suspend fun signInWithGoogle(
        username: String = this.username.value,
        googleToken: String? = this.googleToken.value
    ): Boolean {
        if (googleToken == null || username.isBlank())
            throw AniError(AniErrorCode.INVALID_VALUE, "Provided Username is invalid!")
        return withContext(Dispatchers.IO) {
            true
        }
    }
}