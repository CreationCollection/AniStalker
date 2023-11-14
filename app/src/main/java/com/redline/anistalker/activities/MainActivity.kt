package com.redline.anistalker.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.redline.anistalker.R
import com.redline.anistalker.activities.screens.HomeScreen
import com.redline.anistalker.activities.screens.LibraryScreen
import com.redline.anistalker.activities.screens.MediaScreen
import com.redline.anistalker.activities.screens.SearchScreen
import com.redline.anistalker.ui.components.AniNavBar
import com.redline.anistalker.ui.components.NavItem
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.viewModels.DownloadViewModel
import com.redline.anistalker.viewModels.HomeViewModel
import com.redline.anistalker.viewModels.LibraryViewModel
import com.redline.anistalker.viewModels.SearchViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val screenHome = "SCREEN_HOME"
    private val screenSearch = "SCREEN_SEARCH"
    private val screenLibrary = "SCREEN_LIBRARY"
    private val screenDownload = "SCREEN_DOWNLOADS"

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val navItems = arrayOf(
            NavItem(R.drawable.home, R.drawable.home, "Home"),
            NavItem(R.drawable.search, R.drawable.search, "Search"),
            NavItem(R.drawable.library, R.drawable.library, "Library"),
            NavItem(R.drawable.downloads, R.drawable.downloads, "Media"),
        )

        setContent {
            val navController = rememberNavController()
            var selectedNavBar by rememberSaveable {
                mutableIntStateOf(0)
            }

            navController.enableOnBackPressed(false)
            BackHandler(true) {
                if (navController.currentDestination?.route == screenHome) finish()
                else {
                    navController.navigate(screenHome) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    }
                }
            }

            val onNavChange = remember {
                    NavController.OnDestinationChangedListener { _, destination, _ ->
                        selectedNavBar = when (destination.route) {
                            screenSearch -> 1
                            screenLibrary -> 2
                            screenDownload -> 3
                            else -> 0
                        }
                    }
                }
            DisposableEffect(navController) {
                navController.addOnDestinationChangedListener(onNavChange)
                onDispose {
                    navController.removeOnDestinationChangedListener(onNavChange)
                }
            }


            AniStalkerTheme {
                Surface {
                    Scaffold(
                        bottomBar = {
                            AniNavBar(
                                items = navItems,
                                selectedItem = selectedNavBar
                            ) {
                                if (selectedNavBar != it) {
                                    val route = when (it) {
                                        0 -> screenHome
                                        1 -> screenSearch
                                        2 -> screenLibrary
                                        3 -> screenDownload
                                        else -> screenHome
                                    }
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    }
                                }
                            }
                        },
                        contentWindowInsets = WindowInsets(0)
                    ) { pads ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(pads)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = screenHome
                            ) {
                                homeScreenComposable()
                                searchScreenComposable()
                                libraryScreenComposable()
                                mediaScreenComposable()
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.Transparent,
                                            1f to aniStalkerColorScheme.background
                                        )
                                    )
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openAnimeDetails(animeId: Int) {
        val intent = Intent(this@MainActivity, AnimeDetailActivity::class.java)
            .putExtra("animeId", animeId)
        startActivity(intent)
    }

    private fun openWatchlistDetails(watchId: Int) {
        startActivity(
            Intent(this, WatchlistDetailActivity::class.java).apply {
                putExtra("watchId", watchId)
            }
        )
    }

    private fun openDownloadDetails(animeId: Int) {
        startActivity(
            Intent(this, DownloadDetailActivity::class.java).apply {
                putExtra("animeId", animeId)
            }
        )
    }

    private fun NavGraphBuilder.homeScreenComposable() = composable(
        route = screenHome,
        enterTransition = { slideInVertically { it / 2 } + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val viewModel by viewModels<HomeViewModel>()

        val spotlights by viewModel.spotlightAnime.collectAsState()
        val currentAnime by viewModel.currentAnime.collectAsState()
        val animeList by viewModel.animeList.collectAsState()
        val spotlightError by viewModel.spotlightError.collectAsState()
        val error by viewModel.error.collectAsState()

        val animeCategories = viewModel.animeCategories

        HomeScreen(
            spotlights = spotlights,
            animeList = animeList,
            animeCategories = animeCategories,
            currentAnime = currentAnime,
            lastCurrentAnimeEpisode = 0,
            loadingError = error?.second,
            spotlightError = spotlightError?.second,

            onSpotlightClicked = {
                openAnimeDetails(it.id)
            },
            onLoadSpotlight = { viewModel.loadSpotlightAnime() },
            onStreamCurrentAnime = {

            },
            onDownloadCurrentAnime = {

            },
            onCurrentAnimeClicked = {

            },
            onCategoryChanged = {
                viewModel.changeCategory(it)
            },
            onAnimeCardClicked = {
                openAnimeDetails(it.id)
            }
        ) {
            viewModel.loadNextPage()
        }
    }

    private fun NavGraphBuilder.searchScreenComposable() = composable(
        route = screenSearch,
        enterTransition = { slideInVertically { it / 2 } + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val viewModel by viewModels<SearchViewModel>()

        val animeContent by viewModel.animeList.collectAsState()
        val loadingError by viewModel.error.collectAsState()

        SearchScreen(
            animeContent = animeContent,
            loadingError = loadingError?.second,
            onLoadNextPage = { viewModel.loadNextAnimePage() },
            onAnimeClicked = { openAnimeDetails(it) },
        ) { query, animeFilter ->
            if (query.isEmpty())
                viewModel.filter(animeFilter)
            else
                viewModel.search(query, animeFilter)
        }
    }

    private fun NavGraphBuilder.libraryScreenComposable() = composable(
        route = screenLibrary,
        enterTransition = { slideInVertically { it / 2 } + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val viewModel by viewModels<LibraryViewModel>()

        val watchlist by viewModel.watchlist.collectAsState()

        LibraryScreen(
            watchlist = watchlist,
            animeCount = 0,
            onAnimeCollectionClicked = { },
        ) {
            openWatchlistDetails(it.id)
        }
    }

    private fun NavGraphBuilder.mediaScreenComposable() = composable(
        route = screenDownload,
        enterTransition = { slideInVertically { it / 2 } + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val viewModel by viewModels<DownloadViewModel>()

        val animeDownloads by viewModel.animeDownloads.collectAsState()

        MediaScreen(
            animeDownloads = animeDownloads,
            ongoingDownloads = {
                viewModel.getAnimeOngoingDownload(it) ?: MutableStateFlow(emptyList())
            }
        ) {
            openDownloadDetails(it)
        }
    }
}
