@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.redline.anistalker.activities.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.ui.components.EpisodeDownloadContentView
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import kotlinx.coroutines.launch

@Composable
fun EpisodeDownloadSheet(
    show: Boolean,
    dubCount: Int = 0,
    episodeList: List<AnimeEpisodeDetail>?,
    onDownloadEpisode: (AnimeEpisodeDetail, AnimeTrack) -> Unit,
    onHide: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    val amount =
        if (LocalInspectionMode.current) 5
        else 20

    if (show) ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            scope.launch { sheetState.hide() }
                .invokeOnCompletion { onHide() }
        },
        dragHandle = { },
        shape = RoundedCornerShape(20.dp),
        containerColor = aniStalkerColorScheme.background,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Download Episodes",
                        fontWeight = FontWeight.Bold,
                    )
                }
                Divider(modifier = Modifier.fillMaxWidth())
            }

            if (!episodeList.isNullOrEmpty()) {
                val chipListState = rememberLazyListState()
                val chipScope = rememberCoroutineScope()

                val chipCount by rememberSaveable {
                    val count = kotlin.math.ceil(episodeList.size / (amount * 1.0)).toInt()
                    mutableIntStateOf(count)
                }

                var selectedChip by rememberSaveable {
                    mutableIntStateOf(0)
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    state = chipListState
                ) {
                    items(chipCount) {
                        val start = it * amount
                        val end = (start + amount).coerceAtMost(episodeList.size)
                        FilterChip(
                            selected = selectedChip == it,
                            onClick = {
                                selectedChip = it
                                chipScope.launch { chipListState.animateScrollToItem(it) }
                            },
                            label = {
                                Text(
                                    text = "EP ${start + 1}- $end",
                                )
                            }
                        )
                    }
                }

                LazyColumn {
                    val startRange = selectedChip * amount
                    val endRange = (startRange + amount).coerceAtMost(episodeList.size)
                    val range = endRange - startRange

                    items(range) {
                        val episode = episodeList[it + startRange]
                        EpisodeDownloadContentView(
                            details = episode,
                            hasDub = episode.episode <= dubCount,
                        ) { track ->
                            onDownloadEpisode(episode, track)
                        }
                    }
                }
            }
            else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    Text(
                        text = "No episode available for this anime!",
                        color = Color.White.copy(alpha = .6f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun P_Screen() {
    AniStalkerTheme {
        Surface(
            color = aniStalkerColorScheme.background,
            contentColor = Color.White,
        ) {
            EpisodeDownloadSheet(
                show = true,
                episodeList = listOf(
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                    AnimeEpisodeDetail(),
                ),
                onDownloadEpisode = { _, _ -> }
            ) {
            }
        }
    }
}