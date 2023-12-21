package com.redline.anistalker.managements

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AniInitializer {
    private var initializing = false
    private var initialized = false
    private var completedEvent: (() -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun initializeApp(application: Application) {
        if (initializing || initialized) return
        scope.launch {
            initializing = true

            FileMaster.initialize(application)
            UserData.initialize(application)
            DownloadManager.initialize(application)

            initialized = true
            initializing = false
            if (completedEvent != null) {
                completedEvent?.invoke()
                completedEvent = null
            }
        }
    }

    fun onInitialized(callback: () -> Unit) {
        if (!initialized) completedEvent= callback
        else callback()
    }
}