@file:OptIn(ExperimentalMaterial3Api::class)

package com.redline.anistalker.activities.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.ui.components.CenteredBox
import com.redline.anistalker.ui.components.WatchlistCard
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.secondary_background
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    watchlist: List<Watchlist>,
    animeCount: Int,
    onAnimeCollectionClicked: () -> Unit,
    onWatchlistClicked: (Watchlist) -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    var showCreationScreen by rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 30.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = "Library",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            CenteredBox(
                modifier = Modifier
                    .clickable { }
                    .clip(RoundedCornerShape(6.dp))
                    .size(40.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.sync),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(25.dp)
                )
            }
            CenteredBox(
                modifier = Modifier
                    .clickable {
                        showCreationScreen = true
                    }
                    .clip(RoundedCornerShape(6.dp))
                    .size(40.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.add),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clickable { onAnimeCollectionClicked() }
                            .clip(shape)
                            .border(.5.dp, Color.White.copy(alpha = .2f), shape)
                            .background(secondary_background)
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "All Anime ($animeCount)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Box (
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.outline)
                            .fillMaxWidth()
                            .height(.5.dp)
                    )
                }
            }

            items(
                items = watchlist
            ) {
                WatchlistCard(
                    watchlist = it
                ) { watch ->
                    onWatchlistClicked(watch)
                }
            }
        }
    }

    val dismissSheet = {
        scope.launch { sheetState.hide() }
            .invokeOnCompletion { showCreationScreen = false }
        Unit
    }
    if (showCreationScreen) ModalBottomSheet(
        onDismissRequest = dismissSheet
    ){
        WatchlistCreationSheet(
            onBackPress = {
            },
        ) {
            dismissSheet()
        }
    }
}


@Preview
@Composable
private fun PreviewScreen() {
    AniStalkerTheme {
        Surface(
            color = aniStalkerColorScheme.background
        ) {
            LibraryScreen(
                watchlist = listOf(
                    Watchlist(),
                    Watchlist(),
                    Watchlist(),
                    Watchlist(),
                    Watchlist(),
                    Watchlist(),
                ),
                animeCount = 10,
                onAnimeCollectionClicked = { }
            ) {

            }
        }
    }
}