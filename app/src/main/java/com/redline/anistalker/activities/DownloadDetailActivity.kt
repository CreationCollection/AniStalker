@file:OptIn(ExperimentalMaterial3Api::class)

package com.redline.anistalker.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.redline.anistalker.R
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.OngoingEpisodeDownload
import com.redline.anistalker.ui.components.EpisodeDownloadView
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.secondary_background
import com.redline.anistalker.utils.blurImage
import com.redline.anistalker.utils.toDurationFormat
import com.redline.anistalker.utils.toSizeFormat
import com.redline.anistalker.viewModels.DownloadDetailViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class DownloadDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val animeId = intent.getIntExtra("animeId", 0)

        setContent {
            AniStalkerTheme {
                Surface(
                    color = aniStalkerColorScheme.background,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel by viewModels<DownloadDetailViewModel>()
                    viewModel.initializeFor(animeId)

                    val animeDownload by viewModel.animeDownload.collectAsState()
                    val ongoingContent by viewModel.ongoingDownloads.collectAsState()

                    DownloadDetailScreen(
                        animeDownload = animeDownload,
                        content = { viewModel.getContent(it) },
                        ongoingContent = ongoingContent,
                        onPause = { DownloadManager.Anime.pause(this, it) },
                        onResume = { DownloadManager.Anime.resume(this, it) },
                        onCancel = { DownloadManager.Anime.cancel(this, it) },
                        onDelete = { },
                        onRestart = { },
                        onOpenFolder = { },
                    ) {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadDetailScreen(
    animeDownload: AnimeDownload?,
    content: (Int) -> EpisodeDownload?,
    ongoingContent: List<OngoingEpisodeDownload>,
    onPause: (Int) -> Unit,
    onResume: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onRestart: (Int) -> Unit,
    onOpenFolder: (Int) -> Unit,
    onBackPress: () -> Unit,
) {
    val tobBarColor = aniStalkerColorScheme.background.copy(alpha = .9f)

    var bitmapPainter by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    val context = LocalContext.current
    LaunchedEffect(animeDownload?.image) {
        if (animeDownload != null) do {
            try {
                withContext(Dispatchers.IO) {
                    bitmapPainter =
                        BitmapFactory.decodeStream(Net.getStream(animeDownload.image))?.let {
                            context.blurImage(it).asImageBitmap()
                        }
                }
            } catch (err: CancellationException) {
                break
            } catch (err: AniError) {
                err.printStackTrace()
            } catch (err: IOException) {
                err.printStackTrace()
                break
            }
        } while (bitmapPainter == null)
    }

    var selectedEpisode: EpisodeDownload? by remember { mutableStateOf(null) }
    var ongoingEpisode: OngoingEpisodeDownload? = null

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .shadow(4.dp)
                .fillMaxWidth()
                .drawWithCache {
                    onDrawWithContent {
                        drawRect(SolidColor(aniStalkerColorScheme.background))
                        bitmapPainter?.let {
                            val height = (size.width / it.width) * it.height
                            drawImage(
                                it,
                                srcSize = IntSize(it.width, it.height),
                                dstSize = IntSize(size.width.toInt(), height.toInt())
                            )
                        }
                        drawRect(SolidColor(tobBarColor))
                        drawContent()
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .statusBarsPadding()
            ) {
                Button(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = { onBackPress() },
                    modifier = Modifier.size(40.dp)
                ) {
                    val contentColor = LocalContentColor.current
                    Image(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp),
                        colorFilter = ColorFilter.tint(contentColor)
                    )
                }

                if (animeDownload != null) Text(
                    text = animeDownload.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }

        animeDownload?.let {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (ongoingContent.isNotEmpty() && animeDownload.ongoingContent.isNotEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "Downloading",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    items(animeDownload.ongoingContent.size) { content ->
                        val episode = content(animeDownload.ongoingContent[content])
                        val download = ongoingContent[content]

                        if (episode != null) EpisodeDownloadView(
                            details = episode,
                            statusInfo = download,
                            onPause = {
                                onPause(episode.id)
                            }
                        ) {
                            selectedEpisode = episode
                            ongoingEpisode = download
                        }
                    }
                }

                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Episodes",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                items(animeDownload.content.size) { content ->
                    val episode = content(animeDownload.content[content])
                    if (episode != null) EpisodeDownloadView(
                        details = episode,
                        onPause = {
                            onPause(episode.id)
                        }
                    ) {
                        selectedEpisode = episode
                        ongoingEpisode = ongoingContent.find { it.id == episode.id }
                    }
                }
            }
        } ?: Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Text(
                text = "Downloaded Episodes will show here!",
                color = Color.White.copy(alpha = .5f)
            )
        }
    }

    DownloadEpisodeDetailSheet(
        content = selectedEpisode,
        ongoingContent = ongoingEpisode,
        onPause = { onPause(it.id) },
        onResume = { onResume(it.id) },
        onCancel = { onCancel(it.id) },
        onDelete = { onDelete(it.id) },
        onOpenFolder = { onOpenFolder(it.id) },
        onRestart = { onRestart(it.id) }
    ) {
        selectedEpisode = null
        ongoingEpisode = null
    }
}

@Composable
private fun DownloadEpisodeDetailSheet(
    content: EpisodeDownload?,
    ongoingContent: OngoingEpisodeDownload?,
    onPause: (EpisodeDownload) -> Unit,
    onResume: (EpisodeDownload) -> Unit,
    onCancel: (EpisodeDownload) -> Unit,
    onDelete: (EpisodeDownload) -> Unit,
    onOpenFolder: (EpisodeDownload) -> Unit,
    onRestart: (EpisodeDownload) -> Unit,
    onHide: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val shape = RoundedCornerShape(6.dp)
    val dividerShape = RoundedCornerShape(4.dp)

    val progress =
        remember(content, ongoingContent) {
            ongoingContent?.let {
                if (it.status == DownloadStatus.WRITING) {
                    if (it.size <= 0) 0f
                    else it.downloadedSize / it.size.toFloat()
                } else {
                    if (it.duration <= 0f) 0f
                    else it.downloadedDuration / it.duration
                }
            } ?: 0f
        }


    val dismiss = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onHide() }; Unit
    }

    if (content != null) ModalBottomSheet(
        sheetState = sheetState,
        shape = RectangleShape,
        onDismissRequest = dismiss
    ) {
        val statusString =
            remember(content, ongoingContent) {
                ongoingContent?.let {
                    String.format("%.2f%%", progress)
                } ?: content.size.toSizeFormat()
            }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Text(
                    text = content.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Divider(modifier = Modifier.fillMaxWidth())
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "EPISODE ${content.num}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Divider(
                        modifier = Modifier
                            .clip(dividerShape)
                            .size(4.dp)
                    )
                    Text(
                        text = "Ultra HD",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .background(secondary_background)
                            .height(50.dp)
                    ) {
                        ongoingContent?.let {
                            LinearProgressIndicator(
                                progress = progress,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(5.dp)
                            )
                            Text(
                                text = content.track.value,
                                fontWeight = FontWeight.Bold,
                            )

                            Divider(
                                modifier = Modifier
                                    .clip(dividerShape)
                                    .size(4.dp)
                            )

                            Text(
                                text = statusString,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (ongoingContent == null) {
                                Divider(
                                    modifier = Modifier
                                        .clip(dividerShape)
                                        .size(4.dp)
                                )

                                Text(
                                    text = content.duration.toDurationFormat(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            ongoingContent?.let {
                                val shouldPause = it.status == DownloadStatus.PAUSED
                                TextButton(
                                    onClick = {
                                        if (shouldPause) onPause(content) else onResume(content)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (shouldPause) "Pause" else "Resume"
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    if (ongoingContent == null) onDelete(content) else onCancel(content)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (ongoingContent == null) "Delete" else "Cancel"
                                )
                            }
                        }
                    }
                }
                Divider(modifier = Modifier.fillMaxWidth())
                
                OutlinedButton(
                    onClick = { onOpenFolder(content) },
                    shape = shape,
                ) {
                    Text(
                        text = "Open Containing Folder"
                    )
                }
                
                OutlinedButton(onClick = { onRestart(content) }) {
                    Text(
                        text = "Re-Download"
                    )
                }

                OutlinedButton(onClick = { onDelete(content) }) {
                    Text(
                        text = "Delete"
                    )
                }
            }
        }
    }
}

@Preview(wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE)
@Composable
private fun P_Screen() {
    AniStalkerTheme(dynamicColor = true) {
        Surface(
            color = aniStalkerColorScheme.background,
            contentColor = Color.White
        ) {
            DownloadDetailScreen(
                animeDownload = AnimeDownload(
                    content = listOf(
                        0, 1, 2, 3, 4, 5
                    ),
                    ongoingContent = listOf(0, 1, 2, 3, 4, 5)
                ),
                content = { EpisodeDownload() },
                ongoingContent = listOf(
                    OngoingEpisodeDownload(),
                    OngoingEpisodeDownload(),
                    OngoingEpisodeDownload(),
                    OngoingEpisodeDownload(),
                    OngoingEpisodeDownload(),
                ),
                onPause = { },
                onResume = { },
                onCancel = { },
                onDelete = { },
                onRestart = { },
                { }
            ) {

            }
        }
    }
}