package com.redline.anistalker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.redline.anistalker.R
import com.redline.anistalker.models.AniErrorCode
import com.redline.anistalker.ui.components.CenteredBox
import com.redline.anistalker.ui.components.awarePainterResource
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.viewModels.AuthViewModel
import kotlinx.coroutines.launch

class AuthScreen : ComponentActivity() {
    private var onIntentResult: ((ActivityResult) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val routeLogin = "ROUTE_LOGIN"
        val routeSignIn = "ROUTE_SIGN_IN"

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // FOR DEBUG PURPOSES ONLY
        proceed()

        val viewModel: AuthViewModel by viewModels()

        viewModel.onSuccess {
            proceed()
        }

        val googleCheckout: () -> Unit = {
            viewModel.onErrorOnce { code, _ ->
                if (code == AniErrorCode.ONE_TAP_ERROR) {
                    viewModel.performGoogleSignIn(this) { intent, resultCallback ->
                        onIntentResult = { result -> resultCallback(result) }
                        intentLauncher.launch(intent)
                    }
                }
            }
            viewModel.performOneTapSignIn(this, false) { intentSender, resultCallback ->
                onIntentResult = { result -> resultCallback(result) }
                intentSenderLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
        }

        setContent {
            AniStalkerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = aniStalkerColorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()

                    val username by viewModel.username.collectAsState()
                    val password by viewModel.password.collectAsState()
                    val isProcessing by viewModel.isProcessing.collectAsState()
                    val backgroundImage by viewModel.imageResource.collectAsState()

                    val snackBarState = remember { SnackbarHostState() }

                    navController.enableOnBackPressed(false)

                    viewModel.onError { code, error ->
                        scope.launch {
                            if (code == AniErrorCode.ONE_TAP_REJECTED ||
                                code == AniErrorCode.GOOGLE_SIGNIN_REJECTED ||
                                code == AniErrorCode.ONE_TAP_ERROR ||
                                code == AniErrorCode.GOOGLE_SIGNIN_ERROR)
                                return@launch

                            snackBarState.showSnackbar(
                                message = error,
                                duration = SnackbarDuration.Long,
                                withDismissAction = true,
                            )
                        }
                    }

                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackBarState)
                        },
                        contentWindowInsets = WindowInsets(0)
                    ) { pad ->
                        NavHost(
                            navController = navController,
                            startDestination = routeLogin,
                            modifier = Modifier.padding(pad)
                        ) {
                            composable(
                                route = routeLogin,
                                enterTransition = { slideInHorizontally { -it } },
                                exitTransition = { slideOutHorizontally { -it } }
                            ) {
                                LoginScreen(
                                    username = username,
                                    password = password,
                                    onUsernameChanged = { viewModel.saveUsername(it) },
                                    onPasswordChanged = { viewModel.savePassword(it) },
                                    onLogin = { username, password ->
                                        viewModel.loginWithUsername(username, password)
                                    },
                                    onGoogleLogin = googleCheckout,
                                    onRegister = {
                                        viewModel.savePassword("")
                                        navController.navigate(routeSignIn)
                                    },
                                    backgroundImage = backgroundImage,
                                    enabled = !isProcessing
                                )
                            }

                            composable(
                                route = routeSignIn,
                                enterTransition = { slideInHorizontally { it } },
                                exitTransition = { slideOutHorizontally { it } }
                            ) {
                                SignInScreen(
                                    username = username,
                                    password = password,
                                    onUsernameChanged = { viewModel.saveUsername(it) },
                                    onPasswordChanged = { viewModel.savePassword(it) },
                                    onSignIn = { username, password ->
                                        viewModel.signInWithUsername(username, password)
                                    },
                                    onGoogleSignIn = googleCheckout,
                                    onLogin = {
                                        viewModel.savePassword("")
                                        navController.navigate(routeLogin)
                                    },
                                    enabled = !isProcessing
                                )
                            }
                        }
                    }
                }
            }
        }

        if (viewModel.shouldShowOneTap()) {
            viewModel.performOneTapSignIn(this, false) { intentSender, resultCallback ->
                onIntentResult = { result -> resultCallback(result) }
                intentSenderLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
        }
    }

    private fun proceed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private val intentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onIntentResult?.run { this(it) }
            onIntentResult = null
        }

    private val intentSenderLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            onIntentResult?.run { this(it) }
            onIntentResult = null
        }
}

@Composable
private fun LoginScreen(
    username: String,
    onUsernameChanged: ((username: String) -> Unit)? = null,
    password: String,
    onPasswordChanged: ((password: String) -> Unit)? = null,
    backgroundImage: Int? = R.drawable.p1,
    enabled: Boolean = true,
    onLogin: ((username: String, password: String) -> Unit)? = null,
    onGoogleLogin: (() -> Unit)? = null,
    onRegister: (() -> Unit)? = null,
) {
    val bg = aniStalkerColorScheme.background

    val shape = RoundedCornerShape(6.dp)
    val googleColors = ButtonDefaults.buttonColors(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primaryContainer,
        disabledContainerColor = Color.White.copy(alpha = .5f)
    )

    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.4f)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                0f to bg,
                                .1f to bg.copy(alpha = .7f),
                                1f to bg
                            )
                        )
                    }
                }
        ) {
            Image(
                painter =
                if (backgroundImage == null) ColorPainter(bg)
                else painterResource(backgroundImage),
                contentDescription = null,
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Bottom)
                .padding(horizontal = 30.dp)
                .align(Alignment.BottomCenter)
        ) {
            CenteredBox(
                modifier = Modifier.height(60.dp)
            ) {
                Text(
                    text = "Login In AniStalker",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { onUsernameChanged?.run { this(it) } },
                singleLine = true,
                label = { Text(text = "Username") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = password,
                onValueChange = { onPasswordChanged?.run { this(it) } },
                singleLine = true,
                label = { Text(text = "Password") },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onLogin?.run { this(username, password) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    enabled = enabled
                ) {
                    Text(
                        text = "LogIn",
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { onGoogleLogin?.run { this() } },
                    colors = googleColors,
                    shape = shape,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = awarePainterResource(R.drawable.google_colored),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "LogIn with Google",
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Divider(modifier = Modifier.weight(1f))
                }
                OutlinedButton(
                    onClick = { onRegister?.run { this() } },
                    shape = shape,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Join AniStalker",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun SignInScreen(
    username: String,
    onUsernameChanged: ((username: String) -> Unit)? = null,
    password: String,
    onPasswordChanged: ((password: String) -> Unit)? = null,
    enabled: Boolean = true,
    onSignIn: ((username: String, password: String) -> Unit)? = null,
    onGoogleSignIn: (() -> Unit)? = null,
    onLogin: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(6.dp)
    val googleColors = ButtonDefaults.buttonColors(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primaryContainer,
        disabledContainerColor = Color.White.copy(alpha = .5f)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 30.dp, vertical = 20.dp)
        ) {
            CenteredBox(
                modifier = Modifier.height(60.dp)
            ) {
                Text(
                    text = "Join AniStalker",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { onUsernameChanged?.run { this(it) } },
                singleLine = true,
                label = { Text(text = "Username") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = password,
                onValueChange = { onPasswordChanged?.run { this(it) } },
                singleLine = true,
                label = { Text(text = "Password") },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onSignIn?.run { this(username, password) }
                    },
                    shape = shape,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "JOIN",
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { onGoogleSignIn?.run { this() } },
                    colors = googleColors,
                    shape = shape,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = awarePainterResource(R.drawable.google_colored),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Join with Google",
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Divider(modifier = Modifier.weight(1f))
                }
                OutlinedButton(
                    onClick = { onLogin?.run { this() } },
                    shape = shape,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Login Instead",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}


@Preview(
    showSystemUi = false, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
    device = "id:pixel_xl"
)
@Preview(
    showSystemUi = false, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    device = "spec:width=1440px,height=3120px,dpi=560,isRound=true"
)
@Composable
private fun P_LoginScreen() {
    AniStalkerTheme(dynamicColor = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = aniStalkerColorScheme.background
        ) {
            LoginScreen(
                username = "Username",
                password = "Password",
            )
        }
    }
}

@Preview(showSystemUi = false, wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE)
@Composable
private fun P_SignInScreen() {
    AniStalkerTheme(dynamicColor = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = aniStalkerColorScheme.background
        ) {
            SignInScreen(
                username = "Username",
                password = "Password"
            )
        }
    }
}
