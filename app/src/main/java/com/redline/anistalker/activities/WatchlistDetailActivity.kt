package com.redline.anistalker.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeEpisode
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.ui.components.AnimeCardView
import com.redline.anistalker.ui.components.WatchlistPrivacy
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.dark_background
import com.redline.anistalker.viewModels.pages.WatchlistPageViewModel

class WatchlistDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val watchId = intent.getIntExtra("watchId", 0)
        setContent {
            AniStalkerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = aniStalkerColorScheme.background
                ) {
                    val viewModel by viewModels<WatchlistPageViewModel>()

                    viewModel.initialize(watchId)
                    val watchlist by viewModel.watchlist.collectAsState()
                    val animeList by viewModel.animeList.collectAsState()

                    WatchlistDetailScreen(
                        watchlist = watchlist,
                        animeList = animeList
                    ) {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistDetailScreen(
    watchlist: Watchlist?,
    animeList: List<AnimeCard>,
    onBackPressed: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)

    Column (
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .background(aniStalkerColorScheme.background)
                .statusBarsPadding()
                .shadow(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(60.dp)
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = RoundedCornerShape(6.dp),
                    onClick = { onBackPressed() },
                    modifier = Modifier
                        .height(40.dp)
                ) {
                    val contentColor = LocalContentColor.current
                    Image(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        colorFilter = ColorFilter.tint(contentColor)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Back",
                        fontSize = 12.sp,
                        color = contentColor,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // Should be an edit button here...
            }
        }

        watchlist?.also {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 20.dp),
            ) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = watchlist.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(shape)
                                .border(
                                    BorderStroke(.5.dp, MaterialTheme.colorScheme.outline),
                                    shape
                                )
                                .background(dark_background)
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            WatchlistPrivacy(privacy = watchlist.privacy)
                            Divider(
                                modifier = Modifier
                                    .clip(shape)
                                    .size(4.dp)
                            )
                            Text(
                                text = watchlist.owner,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Divider(
                                modifier = Modifier
                                    .clip(shape)
                                    .size(4.dp)
                            )
                            Text(
                                text = "${watchlist.series.size} Anime",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        AssistChip(
                            onClick = { /*TODO*/ },
                            label = { Text(text = "Share") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }

                item {

                }

//            item {
//                Box(
//                    modifier = Modifier.padding(vertical = 10.dp)
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .clip(RoundedCornerShape(20.dp))
//                            .background(secondary_background)
//                            .padding(horizontal = 10.dp)
//                            .height(40.dp)
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.search),
//                            contentDescription = null,
//                            modifier = Modifier.size(40.dp)
//                        )
//                        Spacer(modifier = Modifier.size(10.dp))
//                        TextField(
//                            value = ,
//                            onValueChange =
//                        )
//                    }
//                }
//            }

                if (animeList.isNotEmpty()) {
                    items(
                        items = animeList
                    ) { anime ->
                        AnimeCardView(
                            animeCard = anime,
                            onImageClick = { }
                        ) {

                        }
                    }
                }
                else {
                    item {
                        Box (
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            Text(
                                text = "No Anime yet added in this watchlist!",
                                color = Color.White.copy(alpha = .4f)
                            )
                        }
                    }
                }
            }
        } ?:
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = "Watchlist does not exists!",
            )
        }
    }
}

@Preview(wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
private fun P_WatchlistDetailScreen() {
    AniStalkerTheme(dynamicColor = true) {
        Surface(
            color = aniStalkerColorScheme.background,
            contentColor = Color.White,
        ) {
            WatchlistDetailScreen(
                watchlist = Watchlist(),
                animeList = listOf(
                    AnimeCard(episodes = AnimeEpisode(total = 30)),
                    AnimeCard(),
                    AnimeCard(),
                    AnimeCard(episodes = AnimeEpisode(total = 30)),
                    AnimeCard(episodes = AnimeEpisode(total = 30)),
                    AnimeCard(episodes = AnimeEpisode(total = 30)),
                    AnimeCard(),
                    AnimeCard(),
                )
            ) {

            }
        }
    }
}