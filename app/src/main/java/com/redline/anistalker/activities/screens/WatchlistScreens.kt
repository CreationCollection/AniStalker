@file:OptIn(ExperimentalMaterial3Api::class)

package com.redline.anistalker.activities.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.models.WatchlistPrivacy
import com.redline.anistalker.ui.components.AnimeCardView
import com.redline.anistalker.ui.components.CenteredBox
import com.redline.anistalker.ui.components.WatchlistCard
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.secondary_background
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistOperationSheet(
    show: Boolean,
    showCreationScreen: Boolean = false,
    showBackPressButton: Boolean = false,
    watchlist: List<Watchlist>?,
    anime: AnimeCard?,
    onWatchlistCreated: ((Watchlist) -> Unit)? = null,
    onWatchlistSelected: ((Int) -> Unit)? = null,
    onCreationScreenToggled: (Boolean) -> Unit,
    onHide: () -> Unit,
) {
    var title by rememberSaveable {
        mutableStateOf("")
    }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (show) ModalBottomSheet(
        sheetState = sheetState,
        containerColor = aniStalkerColorScheme.background,
        contentColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        dragHandle = { },
        onDismissRequest = {
            scope.launch { sheetState.hide() }
                .invokeOnCompletion { onHide() }
            title = ""
        },
        modifier = Modifier
            .imePadding()
    ) {
        if (showCreationScreen) {
            WatchlistCreationSheet(
                title = title,
                onTitleChanged = { title = it },
                showBackPressButton = showBackPressButton,
                onBackPress = {
                    onCreationScreenToggled(false)
                    scope.launch { sheetState.partialExpand() }
                }
            ) {
                UserData.createWatchlist(it.title, it.privacy)
                    .then { watchlist ->
                        onCreationScreenToggled(false)
                        scope.launch { sheetState.partialExpand() }
                            .invokeOnCompletion {
                                onWatchlistCreated?.let { it1 -> it1(watchlist) }
                            }
                    }
                    .catch {
                    }
            }
        } else {
            watchlist?.let {
                WatchlistSelectionSheet(
                    watchlist = watchlist,
                    anime = anime,
                    onCreateWatchlist = {
                        onCreationScreenToggled(true)
                        scope.launch { sheetState.expand() }
                    }
                ) { id ->
                    if (anime != null) UserData.addAnimeToWatchlist(id, anime.id)
                        .then {
                            UserData.addAnime(anime)
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    onWatchlistSelected?.run { this(id) }
                                    onHide()
                                }
                        }
                        .catch {
                        }
                }
            }
        }
    }
}

@Composable
fun WatchlistSelectionSheet(
    watchlist: List<Watchlist>,
    anime: AnimeCard? = null,
    onCreateWatchlist: () -> Unit,
    onWatchlistSelected: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        CenteredBox(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Select Watchlist",
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Divider(modifier = Modifier.fillMaxWidth())
        anime?.also {
            Box(modifier = Modifier.padding(vertical = 20.dp)) {
                AnimeCardView(
                    animeCard = it,
                    showOwner = false
                ) { }
            }
            Divider(modifier = Modifier.fillMaxWidth())
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            item {
                Button(
                    onClick = {
                        onCreateWatchlist()
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = secondary_background
                    ),
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val contentColor = LocalContentColor.current
                    Text(
                        text = "Create New Watchlist",
                        color = contentColor
                    )
                }
            }

            if (watchlist.isNotEmpty()) {
                items(
                    items = watchlist
                ) { watch ->
                    val disabled = watch.series.contains(anime?.id)
                    Box(
                        modifier = Modifier
                            .alpha(if (disabled) .6f else 1f)
                    ) {
                        WatchlistCard(
                            watchlist = watch
                        ) {
                            if (!disabled) onWatchlistSelected(watch.id)
                        }
                    }
                }
            } else {
                item {
                    CenteredBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Text(
                            text = "No Existing Watchlist, Create new one.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = .5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistCreationSheet(
    title: String,
    onTitleChanged: (String) -> Unit,
    showBackPressButton: Boolean = false,
    onBackPress: () -> Unit,
    onCreated: (Watchlist) -> Unit,
) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(true)
    ) {
        Row {
            if (showBackPressButton) Box (
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onBackPress()
                        }
                        .size(40.dp)
                        .padding(10.dp)
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = "Create New Watchlist",
                    fontWeight = FontWeight.Bold,
                )
            }
            if (showBackPressButton) Spacer(modifier = Modifier.size(60.dp))
        }
        Divider(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = .5f))
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = title,
                label = {
                    Text(text = "Title")
                },
                onValueChange = {
                    if (it.length < 120) onTitleChanged(it)
                },
                modifier = Modifier.fillMaxWidth()
            )
            TabRow(
                selectedTabIndex = selectedTab,
                divider = { },
                containerColor = Color.Transparent,
                indicator = {
                    TabRowDefaults.Indicator(
                        height = 4.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .tabIndicatorOffset(it[selectedTab])
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(secondary_background)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.height(45.dp)
                ) {
                    Text(
                        text = "Public",
                    )
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.height(45.dp)
                ) {
                    Text(
                        text = "Shared",
                    )
                }
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.height(45.dp)
                ) {
                    Text(
                        text = "Private",
                    )
                }
            }

        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onCreated(
                    Watchlist(
                        title = title,
                        privacy = when (selectedTab) {
                            0 -> WatchlistPrivacy.PUBLIC
                            1 -> WatchlistPrivacy.SHARED
                            else -> WatchlistPrivacy.PRIVATE
                        },
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = secondary_background,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            val contentColor = LocalContentColor.current
            Text(
                text = "Create New Watchlist",
                color = contentColor
            )
        }
    }
}

@Preview
@Composable
private fun P_WatchlistSelectionView() {
    AniStalkerTheme {
        Surface(
            color = aniStalkerColorScheme.background,
            contentColor = Color.White,
        ) {
            WatchlistSelectionSheet(
                watchlist = listOf(
                    Watchlist(),
                    Watchlist(),
                    Watchlist(),
                ),
                anime = AnimeCard(),
                onCreateWatchlist = { }
            ) {

            }
        }
    }
}

@Preview
@Composable
private fun P_WatchlistCreationView() {
    AniStalkerTheme {
        Surface(
            color = aniStalkerColorScheme.background,
            contentColor = Color.White,
        ) {
            WatchlistCreationSheet(title = "", onTitleChanged = { }, onBackPress = { }) { }
        }
    }
}