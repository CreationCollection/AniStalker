@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.redline.anistalker.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.redline.anistalker.R
import com.redline.anistalker.managements.DownloadManager
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.AnimeTrack
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
import kotlin.math.abs

class DownloadDetailActivity : AniActivity() {
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
                    val failedContent by viewModel.failedDownloads.collectAsState()

                    DownloadDetailScreen(
                        animeDownload = animeDownload,
                        content = { viewModel.getContent(it) },
                        ongoingContent = ongoingContent,
                        failedContent = failedContent,
                        onPause = { DownloadManager.Anime.pause(this, it) },
                        onResume = { DownloadManager.Anime.resume(this, it) },
                        onCancel = { DownloadManager.Anime.cancel(this, it) },
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
    failedContent: List<Int>,
    onPause: (Int) -> Unit,
    onResume: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onRestart: (Int) -> Unit,
    onOpenFolder: (Int) -> Unit,
    onBackPress: () -> Unit,
) {
    var contentId by rememberSaveable {
        mutableIntStateOf(0)
    }

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

    val sortedOngoingContent by remember(ongoingContent) {
        mutableStateOf(ongoingContent.sortedByDescending { it.num })
    }
    val sortedContent by remember(animeDownload?.content) {
        val list = animeDownload?.content?.run {
            this.map { abs(it) }
                .toSet()
                .sortedByDescending { content(it)?.num }
        } ?: emptyList()
        mutableStateOf(list)
    }

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
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = { onBackPress() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp),
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

        if (
            animeDownload != null &&
            (ongoingContent.isNotEmpty() || animeDownload.content.isNotEmpty() || failedContent.isNotEmpty())
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (ongoingContent.isNotEmpty()) {
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

                    items(
                        items = sortedOngoingContent
                    ) { download ->
                        val episode = content(download.id)

                        if (episode != null) EpisodeDownloadView(
                            details = episode,
                            statusInfo = download,
                            hasSub = episode.track == AnimeTrack.SUB,
                            hasDub = episode.track == AnimeTrack.DUB,
                            onAction = {
                                if (download.status == DownloadStatus.RUNNING) onPause(episode.id)
                                else onResume(episode.id)
                            }
                        ) {
                            contentId = episode.episodeId
                        }
                    }
                }

                if (failedContent.isNotEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "Failed Downloads",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    items(
                        items = failedContent
                    ) { id ->
                        val episode = content(id)
                        if (episode != null) EpisodeDownloadView(
                            details = episode,
                            isFailed = true,
                            hasSub = episode.track == AnimeTrack.SUB,
                            hasDub = episode.track == AnimeTrack.DUB,
                            onAction = {
                                DownloadManager.Anime.retryDownload(context, id)
                            }
                        ) {
                            DownloadManager.Anime.retryDownload(context, id)
                        }
                    }
                }

                if (
                    (ongoingContent.isNotEmpty() || failedContent.isNotEmpty()) &&
                    animeDownload.content.isNotEmpty()
                ) item {
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

                items(sortedContent.size) { item ->
                    val id = abs(sortedContent[item])
                    val subEpisode = content(id)
                    val dubEpisode = content(-id)

                    val episode = subEpisode ?: dubEpisode

                    if (episode != null) EpisodeDownloadView(
                        details = episode,
                        hasSub = subEpisode != null,
                        hasDub = dubEpisode != null,
                        onAction = {

                        }
                    ) {
                        contentId = episode.episodeId
                    }
                }
            }
        } else {
            Box(
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
    }

    val contents by remember(contentId, animeDownload) {
        val sub = if (contentId != 0) content(contentId) else null
        val dub = if (contentId != 0) content(-contentId) else null
        mutableStateOf(Pair(sub, dub))
    }
    val ongoingContents by remember(contentId, ongoingContent, animeDownload) {
        val sub = if (contentId != 0) ongoingContent.find { it.id == contentId } else null
        val dub = if (contentId != 0) ongoingContent.find { it.id == -contentId } else null
        mutableStateOf(Pair(sub, dub))
    }
    DownloadEpisodeDetailSheet(
        contentSub = contents.first,
        contentDub = contents.second,
        ongoingSub = ongoingContents.first,
        ongoingDub = ongoingContents.second,
        onPause = { onPause(it) },
        onResume = { onResume(it) },
        onCancel = { onCancel(it) },
        onOpenFolder = { onOpenFolder(it) },
        onRestart = { onRestart(it) }
    ) {
        contentId = 0
    }
}

@Composable
private fun DownloadEpisodeDetailSheet(
    contentSub: EpisodeDownload? = null,
    contentDub: EpisodeDownload? = null,
    ongoingSub: OngoingEpisodeDownload? = null,
    ongoingDub: OngoingEpisodeDownload? = null,
    onPause: (Int) -> Unit,
    onResume: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onOpenFolder: (Int) -> Unit,
    onRestart: (Int) -> Unit,
    onHide: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(true)
    val scope = rememberCoroutineScope()

    val shape = RoundedCornerShape(6.dp)
    val dividerShape = RoundedCornerShape(4.dp)

    val content = contentSub ?: contentDub

    val dismiss = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onHide() }; Unit
    }

    if (content != null) ModalBottomSheet(
        sheetState = sheetState,
        shape = RectangleShape,
        onDismissRequest = dismiss,
        containerColor = aniStalkerColorScheme.background,
        dragHandle = { },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
                    TrackProgressBar(
                        content = contentSub,
                        ongoingContent = ongoingSub,
                        track = AnimeTrack.SUB,
                        onPause = { if (contentSub != null) onPause(contentSub.id) },
                        onResume = { if (contentSub != null)  onResume(contentSub.id) },
                        onCancel = { if (contentSub != null) onCancel(contentSub.id) }
                    )

                    TrackProgressBar(
                        content = contentDub,
                        ongoingContent = ongoingDub,
                        track = AnimeTrack.DUB,
                        onPause = { if (contentDub != null) onPause(contentDub.id) },
                        onResume = { if (contentDub != null) onResume(contentDub.id) },
                        onCancel = { if (contentDub != null) onCancel(contentDub.id) }
                    )
                }
                Divider(modifier = Modifier.fillMaxWidth())

                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenFolder(content.id) },
                        shape = shape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Open Containing Folder"
                        )
                    }

                    OutlinedButton(
                        onClick = { onRestart(content.id) },
                        shape = shape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Re-Download"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackProgressBar(
    content: EpisodeDownload?,
    ongoingContent: OngoingEpisodeDownload?,
    track: AnimeTrack,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val dividerShape = CircleShape
    val dividerColor = Color.White.copy(.5f)

    val progress = ongoingContent?.let {
        if (it.status == DownloadStatus.WRITING) {
            if (it.size <= 0) 0f
            else it.downloadedSize / it.size.toFloat()
        } else {
            if (it.duration <= 0f) 0f
            else it.downloadedDuration / it.duration
        }
    } ?: 0f

    val statusString =
        ongoingContent?.let {
            String.format("%.2f%%", progress * 100)
        } ?: content?.size?.toSizeFormat()

    Box(
        modifier = Modifier
            .clip(shape)
            .background(secondary_background.copy(if (content == null) .25f else 1f))
            .height(50.dp)
    ) {
        ongoingContent?.let {
            LinearProgressIndicator(
                progress = progress,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxSize(),
                trackColor = Color.Transparent,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            when {
                ongoingContent != null && ongoingContent.status != DownloadStatus.PAUSED ->
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(8.dp)
                    )

                ongoingContent != null ->
                    Icon(
                        painter = painterResource(R.drawable.round_pause),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(5.dp)
                    )

                content == null ->
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(5.dp)
                    )

                else ->
                    IconButton(
                        onClick = { },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .size(30.dp)
                    ){
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
            }

            Text(
                text = track.value,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )

            if (statusString != null) {
                Divider(
                    color = dividerColor,
                    modifier = Modifier
                        .clip(dividerShape)
                        .size(4.dp)
                )
                Text(
                    text = statusString,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                )
            }

            if (ongoingContent == null && content != null) {
                Divider(
                    color = dividerColor,
                    modifier = Modifier
                        .clip(dividerShape)
                        .size(4.dp)
                )

                Text(
                    text = content.duration.toDurationFormat(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ongoingContent?.let {
                val shouldPause = it.status != DownloadStatus.PAUSED
                TextButton(
                    onClick = {
                        if (shouldPause) onPause() else onResume()
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
                    if (content != null) onCancel()
                    else { }
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = when {
                        content == null -> "Download"
                        ongoingContent == null -> "Delete"
                        else -> "Cancel"
                    }
                )
            }
        }
    }
}
