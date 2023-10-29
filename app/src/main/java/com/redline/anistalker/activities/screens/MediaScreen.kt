package com.redline.anistalker.activities.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.redline.anistalker.managements.UserData.watchlist
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.EpisodeRange
import com.redline.anistalker.models.OngoingEpisodeDownload
import com.redline.anistalker.ui.components.AnimeDownloadCard
import com.redline.anistalker.ui.components.CenteredBox
import com.redline.anistalker.ui.components.WatchlistCard
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.secondary_background
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MediaScreen(
    animeDownloads: List<AnimeDownload>,
    ongoingDownloads: (Int) -> StateFlow<List<OngoingEpisodeDownload>>
) {
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
                    text = "Downloads",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = animeDownloads
            ) { downloads ->
                val ongoing by ongoingDownloads(downloads.dId).collectAsState()

                AnimeDownloadCard(
                    animeInfo = downloads,
                    ongoingDownloads = ongoing,
                    showContent = false,
                    onExpandContent = { }
                ) {

                }
            }
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
            MediaScreen(
                animeDownloads = listOf(
                    AnimeDownload(),
                    AnimeDownload(),
                    AnimeDownload(),
                ),
                ongoingDownloads = {
                    MutableStateFlow(
                        listOf(
                            OngoingEpisodeDownload(),
                            OngoingEpisodeDownload(),
                            OngoingEpisodeDownload(),
                        )
                    )
                }
            )
        }
    }
}