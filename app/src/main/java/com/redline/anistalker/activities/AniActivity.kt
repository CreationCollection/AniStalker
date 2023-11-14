package com.redline.anistalker.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import com.redline.anistalker.managements.AniInitializer

open class AniActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AniInitializer.initializeApp(application)
        super.onCreate(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        AniInitializer.initializeApp(application)
        super.onCreate(savedInstanceState, persistentState)
    }
}