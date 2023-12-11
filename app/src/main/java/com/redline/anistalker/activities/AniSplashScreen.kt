package com.redline.anistalker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.redline.anistalker.R
import com.redline.anistalker.managements.AniInitializer
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("CustomSplashScreen")
class AniSplashScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val handler = Handler(mainLooper)
        val startTime = System.currentTimeMillis()

        val permissionGranted = MutableStateFlow(hasPermissions())
        if (permissionGranted.value) AniInitializer.initializeApp(application)

        val permissionRequester =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
                permissionGranted.value = p.all { it.value }
            }

        setContent {
            var callbackAdded by rememberSaveable {
                mutableStateOf(false)
            }
            if (!callbackAdded) {
                AniInitializer.onInitialized {
                    handler.postDelayed({
                        startActivity(
                            Intent(this, MainActivity::class.java)
                        )
                        finish()
                    }, 1000 - (System.currentTimeMillis() - startTime).coerceAtMost(1000))
                }
                callbackAdded = true
            }

            val permissionStatus by permissionGranted.collectAsState()

            AniStalkerTheme {
                Surface(
                    color = aniStalkerColorScheme.background,
                    contentColor = Color.White,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                            )
                            Text(
                                text = getString(R.string.app_name),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.size(20.dp))
                            if (!permissionStatus) Button(
                                onClick = {
                                    requestPermissions(
                                        permissionRequester,
                                        permissionGranted
                                    )
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Allow Permissions"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissions(
        request: ActivityResultLauncher<Array<String>>,
        state: MutableStateFlow<Boolean>
    ) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasStoragePermission()) {
            permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.isEmpty()) {
            AniInitializer.initializeApp(application)
            state.value = true
        } else request.launch(permissions.toTypedArray())
    }

    private fun hasPermissions() = hasStoragePermission() && hasNotificationPermission()

    private fun hasStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }
}