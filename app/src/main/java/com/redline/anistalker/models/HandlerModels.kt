package com.redline.anistalker.models

enum class AniErrorCode(val index: Int) {
    UNKNOWN(0),
    NOT_FOUND(-1),
    CONNECTION_ERROR(-2),
}

class AniError : Exception {
    var errorCode: AniErrorCode = AniErrorCode.UNKNOWN
        private set

    constructor(errorCode: AniErrorCode): super(generateMessage(errorCode)) {
        this.errorCode = errorCode
    }
    constructor(errorCode: AniErrorCode, message: String): super(message) {
        this.errorCode = errorCode
    }

    companion object {
        fun generateMessage(errorCode: AniErrorCode): String {
            return when (errorCode) {
                AniErrorCode.CONNECTION_ERROR -> "Connection Error"
                AniErrorCode.NOT_FOUND -> "Source Not Found!"
                else -> "Unknown Error!"
            }
        }
    }
}

class AniResult<T> {
    private var onResult: ((T) -> Unit)? = null
    private var onError: ((AniError) -> Unit)? = null

    var result: T? = null
        private set
    var error: AniError? = null
        private set

    fun then(callBack: (T) -> Unit) {
        onResult = callBack
    }

    fun catch(callBack: (AniError) -> Unit) {
        onError = callBack
    }

    fun pass(value: T) {
        result = value
        onResult?.let { it(value) }
    }

    fun reject(value: AniError) {
        error = value
        onError?.let { it(value) }
    }
}