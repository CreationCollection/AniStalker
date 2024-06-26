package com.redline.anistalker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
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

@SuppressLint("CustomSplashScreen")
class AniSplashScreen : ComponentActivity() {
    private var nfPermit: ActivityResultLauncher<String>? = null
    private var sPermit: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val handler = Handler(mainLooper)
        val startTime = System.currentTimeMillis()

        nfPermit = registerForActivityResult(ActivityResultContracts.RequestPermission()) { recreate() }
        sPermit = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { recreate() }

        AniInitializer.onInitialized {
            handler.postDelayed({
                startActivity(
                    Intent(this, MainActivity::class.java)
                )
                finish()
            }, 1000 - (System.currentTimeMillis() - startTime).coerceAtMost(1000))
        }

        setContent {
            if (hasNotificationPermission() && hasStoragePermission())
                AniInitializer.initializeApp(application)

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

                            if (!hasNotificationPermission()) {
                                Button(
                                    onClick = {
                                        requestNotificationPermission()
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Allow Notification Permission"
                                    )
                                }
                                Spacer(modifier = Modifier.size(10.dp))
                            }

                            if (!hasStoragePermission()) Button(
                                onClick = {
                                    requestStoragePermission()
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Allow Storage Permission"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (!hasNotificationPermission()) {
                nfPermit?.launch(permission)
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sPermit?.launch(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
    }
}