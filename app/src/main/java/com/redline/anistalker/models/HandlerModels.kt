package com.redline.anistalker.models

enum class AniErrorCode(val index: Int, val message: String) {
    ONE_TAP_REJECTED(3, "OneTap signIn Rejected"),
    GOOGLE_SIGNIN_REJECTED(2, "Google SignIn Rejected"),
    REJECTED(1, "Request Rejected"),
    UNKNOWN(0, "Unknown Error"),
    NOT_FOUND(-1, "Not Found Error"),
    CONNECTION_ERROR(-2, "Unable to establish connection"),
    SLOW_NETWORK_ERROR(-3, "Slow Network Speed Error"),
    SERVER_ERROR(-4, "Unexpected Error from Server!"),
    INVALID_VALUE(-30, "Provided Value is Invalid"),
    INVALID_TOKEN(-40, "Provided Token is Invalid"),
    ONE_TAP_ERROR(-50, "OneTap error"),
    GOOGLE_SIGNIN_ERROR(-60, "Google SignIn Error")
}

data class AniErrorMessage(val code: AniErrorCode, val message: String = code.message)

class AniError : Exception {
    var errorCode: AniErrorCode = AniErrorCode.UNKNOWN
        private set

    constructor(errorCode: AniErrorCode): super(errorCode.message) {
        this.errorCode = errorCode
    }
    constructor(errorCode: AniErrorCode, message: String): super(message) {
        this.errorCode = errorCode
    }
}

class AniResult<T> {
    private val onResult: MutableList<(T) -> Unit> = mutableListOf()
    private val onError: MutableList<(AniErrorMessage) -> Unit> = mutableListOf()
    private val onFinal: MutableList<AniResult<T>.() -> Unit> = mutableListOf()

    var result: T? = null
        private set
    var error: AniErrorMessage? = null
        private set

    fun then(callBack: (T) -> Unit): AniResult<T> {
        if (result != null) callBack(result!!)
        else onResult.add(callBack)
        return this
    }

    fun catch(callBack: (AniErrorMessage) -> Unit): AniResult<T> {
        if (error != null) callBack(error!!)
        else onError.add(callBack)
        return this
    }

    fun finally(callBack: AniResult<T>.() -> Unit): AniResult<T> {
        if (result != null || error != null) callBack()
        else onFinal.add(callBack)
        return this
    }

    fun pass(value: T) {
        result = value
        onResult.forEach { it(value) }
    }

    fun reject(value: AniErrorMessage) {
        error = value
        onError.forEach { it(value) }
    }
}