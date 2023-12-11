package com.redline.anistalker.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.managements.AniInitializer
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme

@SuppressLint("CustomSplashScreen")
class AniSplashScreen: AniActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val handler = Handler(mainLooper)
        val startTime = System.currentTimeMillis()

        setContent {
            AniInitializer.onInitialized {
                handler.postDelayed({
                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )
                    finish()
                }, 1000 - (System.currentTimeMillis() - startTime).coerceAtMost(1000))
            }

            AniStalkerTheme {
                Surface(
                    color = aniStalkerColorScheme.background,
                    contentColor = Color.White,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column (
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                            )
                            Spacer(modifier = Modifier.size(20.dp))
                            Text(
                                text = getString(R.string.app_name),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}