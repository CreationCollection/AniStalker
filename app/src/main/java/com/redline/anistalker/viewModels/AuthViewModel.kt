package com.redline.anistalker.viewModels

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.redline.anistalker.R
import com.redline.anistalker.models.AniErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class AuthViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val authUsername = "AUTH_USERNAME"
    private val authPassword = "AUTH_PASSWORD"
    private val authAutoSignIn = "AUTH_AUTO_SIGN_IN"
    private val stateImage = "STATE_IMAGE"

    private var successCallback: (() -> Unit)? = null
    private var errorCallback: ((errorCode: AniErrorCode, error: String) -> Unit)? = null
    private var errorOnce = mutableListOf<(errorCode: AniErrorCode, error: String) -> Unit>()

    private val autoSignIn = savedStateHandle.getStateFlow(authAutoSignIn, false)

    val username = savedStateHandle.getStateFlow(authUsername, "")
    val password = savedStateHandle.getStateFlow(authPassword, "")
    val imageResource = savedStateHandle.getStateFlow<Int?>(stateImage, null)

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val imageResourceList = listOf(
        R.drawable.p1,
        R.drawable.p2,
        R.drawable.p3,
        R.drawable.p4
    )

    init {
        viewModelScope.launch {
            while (true) {
                savedStateHandle[stateImage] =
                    imageResourceList[Random.nextInt(imageResourceList.size - 1)]
                delay(6000)
            }
        }
    }

    fun saveUsername(username: String) {
        savedStateHandle[authUsername] = username
    }

    fun savePassword(password: String) {
        savedStateHandle[authPassword] = password
    }

    fun onSuccess(callback: () -> Unit) {
        successCallback = callback
    }

    fun onError(callback: (errorCode: AniErrorCode, error: String) -> Unit) {
        errorCallback = callback
    }

    fun onErrorOnce(callback: (errorCode: AniErrorCode, error: String) -> Unit) =
        errorOnce.add(callback)

    fun shouldShowOneTap(): Boolean =
        autoSignIn.value.also { savedStateHandle[authAutoSignIn] = false }

    fun loginWithUsername(
        username: String,
        password: String
    ) {
        if (username.isBlank() || password.isBlank()) {
            reflectError(AniErrorCode.INVALID_VALUE, "Invalid Username or Password.")
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            delay(Random.nextLong(5000))
            succeed()
        }
    }

    fun signInWithUsername(
        username: String,
        password: String
    ) {
        if (username.isBlank() || password.isBlank()) {
            reflectError(AniErrorCode.INVALID_VALUE, "Invalid Username or Password.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            delay(Random.nextLong(5000))
            succeed()
        }
    }

    fun signInWithGoogle(
        googleToken: String?
    ) {
        if (googleToken == null) {
            reflectError(AniErrorCode.INVALID_TOKEN, "Invalid Google Token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            delay(Random.nextLong(5000))
            succeed()
        }
    }

    fun performOneTapSignIn(
        context: Context,
        authorizedOnly: Boolean,
        launchOperation: (intentSender: IntentSender, callback: (result: ActivityResult) -> Unit) -> Unit
    ) {
        _isProcessing.value = true

        val oneTapSignIn = Identity.getSignInClient(context)
        val request = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(authorizedOnly)
                    .setServerClientId(context.getString(R.string.serverClientId))
                    .build()
            )
            .build()

        oneTapSignIn.beginSignIn(request)
            .addOnSuccessListener {
                launchOperation(it.pendingIntent.intentSender) { result ->
                    handleOneTapSignInResult(oneTapSignIn, result)
                }
            }
            .addOnFailureListener {
                Log.e("One Tap Sign", it.stackTraceToString())
                reflectError(
                    AniErrorCode.ONE_TAP_ERROR,
                    "Unknown Error while trying to start OneTapSignIn."
                )
            }
    }

    fun performGoogleSignIn(
        context: Context,
        launchOperation: (intent: Intent, callback: (result: ActivityResult) -> Unit) -> Unit
    ) {
        _isProcessing.value = true

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.serverClientId))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        launchOperation(googleSignInClient.signInIntent) {
            handleGoogleSignInResult(it)
        }
    }

    private fun handleOneTapSignInResult(oneTapSignIn: SignInClient, result: ActivityResult) {
        try {
            val auth = oneTapSignIn.getSignInCredentialFromIntent(result.data)
            signInWithGoogle(auth.googleIdToken)
        } catch (ex: ApiException) {
            if (ex.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                reflectError(AniErrorCode.ONE_TAP_REJECTED, "Rejected!")
            } else {
                Log.e("One Tap Sign", ex.stackTraceToString())
                reflectError(
                    AniErrorCode.ONE_TAP_ERROR,
                    ex.message ?: "Unknown error during OneTapSignIn. statusCode: ${ex.statusCode}"
                )
            }
        } catch (ex: Exception) {
            Log.e("One Tap Sign", ex.stackTraceToString())
            reflectError(AniErrorCode.UNKNOWN, ex.message ?: "Unknown error during OneTapSignIn.")
        }
    }

    private fun handleGoogleSignInResult(result: ActivityResult) {
//        if (result.resultCode != Activity.RESULT_CANCELED) {
//            reflectError(AniErrorCode.GOOGLE_SIGNIN_REJECTED, "SignIn Rejected.")
//        }
//        else {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val acc = task.result
                signInWithGoogle(acc.idToken)
            } catch (e: Exception) {
                Log.e("Google SignIn", e.stackTraceToString())
                reflectError(
                    AniErrorCode.GOOGLE_SIGNIN_ERROR,
                    e.message ?: "Unknown Google SignIn Error!"
                )
            }
//        }
    }

    private fun succeed() {
        _isProcessing.value = false
        successCallback?.run { this() }
    }

    private fun reflectError(errorCode: AniErrorCode, error: String) {
        Log.e("Auth Screen", "error: $error, code: $errorCode")
        errorCallback?.run { this(errorCode, error) }
        errorOnce
            .onEach { c -> c(errorCode, error) }
            .also {
                it.clear()
            }
        _isProcessing.value = false
    }
}