package com.redline.anistalker.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDownload
import com.redline.anistalker.models.AnimeEpisodeDetail
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.EpisodeDownload
import com.redline.anistalker.models.EpisodeRange
import com.redline.anistalker.models.Event
import com.redline.anistalker.models.EventType
import com.redline.anistalker.models.OngoingEpisodeDownload
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.dark_background
import com.redline.anistalker.ui.theme.secondary_background
import com.redline.anistalker.utils.toDurationFormat
import com.redline.anistalker.utils.toSizeFormat
import kotlin.math.ceil
import com.redline.anistalker.models.AnimeCard as AnimeHalf


@Composable
private fun AnimeCardPopulatedDetails(
    animeCard: AnimeCard,
    showOwner: Boolean,
    highlightOwner: Boolean,
    highlightColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = animeCard.name.english,
                maxLines = 1,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.size(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            SmallEpisodeTail(episodes = animeCard.episodes)
            Divider(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = animeCard.type.value,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            if (showOwner) {
                Divider(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                ) {
                    Text(
                        text = "by",
                        color = Color.White.copy(alpha = .5f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = animeCard.owner,
                        color = if (highlightOwner) highlightColor else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeCardSimmerDetails(
    simmerColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(simmerColor)
        ) { }
        Spacer(modifier = Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(.7f)
                .height(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(simmerColor)
        ) { }
    }
}

@Composable
fun AnimeCardView(
    animeCard: AnimeHalf? = null,
    simmerValue: Float = 0f,
    showOwner: Boolean = true,
    highlightOwner: Boolean = true,
    onImageClick: ((anime: AnimeCard) -> Unit)? = null,
    onClick: (anime: AnimeCard) -> Unit
) {
    val simmerColor = (secondary_background).copy(alpha = lerp(.4f, 1f, simmerValue))
    val outline = MaterialTheme.colorScheme.primary

    val imageShape = RoundedCornerShape(8.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .let { if (animeCard != null) it.clickable { onClick(animeCard) } else it }
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {

        AsyncImage(
            url = animeCard?.image,
            loadColor = simmerColor,
            modifier = Modifier
                .clickable {
                    onImageClick?.let {
                        if (animeCard != null) it(animeCard)
                    }
                }
                .clip(imageShape)
                .let { if (animeCard != null) it.border(1.dp, outline, imageShape) else it }
                .width(75.dp)
                .height(60.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            this@Row.AnimatedVisibility(
                visible = animeCard == null,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "Anime Details simmer",
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                AnimeCardSimmerDetails(simmerColor)
            }
            this@Row.AnimatedVisibility(
                visible = animeCard != null,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "Anime Details simmer",
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (animeCard != null) {
                    AnimeCardPopulatedDetails(
                        animeCard = animeCard,
                        showOwner = showOwner,
                        highlightOwner = highlightOwner,
                        highlightColor = outline
                    )
                }
            }
        }
    }
}


@Composable
private fun WatchlistCardSimmerDetail(
    simmerColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(.8f)
                .height(22.5.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(simmerColor)
        )
        Spacer(modifier = Modifier.size(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(.6f)
                .height(22.5.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(simmerColor)
        )
    }
}

@Composable
private fun WatchlistCardPopulatedDetails(
    watchlist: Watchlist,
    highlightOwner: Boolean,
    highlightColor: Color,
) {
    val bg = dark_background
    val tertiary = MaterialTheme.colorScheme.tertiary

    val imagePainter = awarePainterResource(resId = R.drawable.folowing_icon)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Text(
            text = watchlist.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.size(5.dp))
        Row(
            modifier = Modifier.height(25.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .fillMaxHeight()
            ) {
                CenteredBox(
                    modifier = Modifier
                        .background(bg)
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp)
                ) {
                    WatchlistPrivacy(privacy = watchlist.privacy)
                }
                Spacer(modifier = Modifier.size(1.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(bg)
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = "${watchlist.series.size} Series",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Divider(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White.copy(alpha = .4f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Text(
                            text = "by",
                            color = Color.White.copy(alpha = .5f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = watchlist.owner,
                            color = if (highlightOwner) highlightColor else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (watchlist.privacy != com.redline.anistalker.models.WatchlistPrivacy.PRIVATE || LocalInspectionMode.current) {
                Spacer(modifier = Modifier.size(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp)
                ) {
                    Image(
                        painter = imagePainter,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        colorFilter = ColorFilter.tint(tertiary)
                    )
                    Text(
                        text = watchlist.following.toString(),
                        color = tertiary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistCard(
    watchlist: Watchlist? = null,
    simmerValue: Float = 0f,
    onClick: (anime: Watchlist) -> Unit
) {
    val simmerColor = dark_background.copy(alpha = lerp(.4f, 1f, simmerValue))
    val primary = MaterialTheme.colorScheme.primary
    val bg = secondary_background

    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .border(.5.dp, Color.White.copy(alpha = .2f), shape)
            .clip(shape)
            .background(bg)
            .fillMaxWidth()
            .clickable { watchlist?.let { onClick(it) } }
    ) {
        AsyncImage(
            url = watchlist?.image,
            loadColor = bg,
            overlayBrush = Brush.horizontalGradient(
                0f to bg, 1f to bg.copy(alpha = .5f)
            ),
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp)
                .align(Alignment.CenterEnd)
        )

        AnimatedVisibility(
            visible = watchlist == null,
            enter = fadeIn(),
            exit = fadeOut(),
            label = "Watchlist Details Simmer",
            modifier = Modifier.fillMaxWidth()
        ) {
            WatchlistCardSimmerDetail(simmerColor)
        }
        AnimatedVisibility(
            visible = watchlist != null,
            enter = fadeIn(),
            exit = fadeOut(),
            label = "Watchlist Details Simmer",
            modifier = Modifier.fillMaxWidth()
        ) {
            if (watchlist != null) {
                WatchlistCardPopulatedDetails(
                    watchlist = watchlist,
                    highlightOwner = true,
                    highlightColor = primary,
                )
            }
        }

    }

}


@Composable
private fun AnimeDownloadCard_Details(
    details: AnimeDownload,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val dim = Color.White.copy(alpha = .4f)
    val grey = Color.White.copy(alpha = .75f)

    val size = details.size.toSizeFormat().split(" ")
    val duration = details.duration.toDurationFormat().split(" ")

    Box(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        AsyncImage(
            url = details.image,
            loadColor = dark_background,
            overlayBrush = SolidColor(Color.Black.copy(alpha = .9f)),
            modifier = Modifier.fillMaxSize()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = details.title,
                    color = primary,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigEpisodeTail(details.episodes)
                    Divider(modifier = Modifier.size(4.dp), color = dim)
                    Text(
                        text = details.type.value,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = duration[0],
                            color = primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = duration[1],
                            color = grey,
                            fontSize = 12.sp,
                        )
                    }
                    Divider(modifier = Modifier.size(4.dp), color = dim)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = size[0],
                            color = primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = size[1],
                            color = grey,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeDownloadCard_Progress(
    downloads: List<OngoingEpisodeDownload>,
    onPause: () -> Unit,
    onCancel: () -> Unit,
    onEpisodePause: (episode: OngoingEpisodeDownload) -> Unit,
) {
    val primaryFix = MaterialTheme.colorScheme.onPrimaryContainer

    var isPaused = false
    var paused = 0
    var running = 0
    var waiting = 0

    LaunchedEffect(downloads.hashCode()) {
        downloads.forEach {
            when (it.status) {
                DownloadStatus.PAUSED -> paused++
                DownloadStatus.RUNNING -> running++
                DownloadStatus.WAITING, DownloadStatus.NETWORK_WAITING -> waiting++
                else -> {}
            }
        }
        if (running == 0 && waiting == 0) isPaused = true
    }

    val speed = downloads.let {
        var value: Long = 0
        it.forEach { d ->
            value += d.downloadSpeed
        }
        value.toSizeFormat() + "/s"
    }

    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = primaryFix,
            disabledContentColor = primaryFix.copy(alpha = .5f)
        )


    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 15.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Image(
                    painter = awarePainterResource(R.drawable.downloading),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(primaryFix),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = running.toString(),
                    color = primaryFix,
                    fontSize = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Image(
                    painter = awarePainterResource(R.drawable.pause),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(primaryFix),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = paused.toString(),
                    color = primaryFix,
                    fontSize = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Image(
                    painter = awarePainterResource(R.drawable.download_queue),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(primaryFix),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = waiting.toString(),
                    color = primaryFix,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = speed,
                color = Color.White,
                fontSize = 12.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = onPause,
                colors = buttonColors,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                val contentColor = LocalContentColor.current
                Text(
                    text = if (isPaused) "Resume All" else "Pause All",
                    color = contentColor
                )
            }
            OutlinedButton(
                onClick = onCancel,
                colors = buttonColors,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                val contentColor = LocalContentColor.current
                Text(
                    text = "Cancel All",
                    color = contentColor
                )
            }
        }

        val count = 3
        val rows = ceil(downloads.size / count.toDouble()).toInt()
        Column (
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(rows) { x ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    repeat(count) { y ->
                        val index = (x * count) + y
                        if (index < downloads.size) {
                            val it = downloads[index]

                            val value =
                                if (it.duration <= 0) 0f
                                else it.downloadedDuration / it.duration

                            Box (
                                modifier = Modifier.weight(1f)
                            ){
                                EpisodeProgress(
                                    num = it.num,
                                    value = value,
                                    status = it.status,
                                ) {
                                    onEpisodePause(it)
                                }
                            }
                        }
                        else {
                            Box(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeDownloadCard_Content(
    content: Map<String, List<EpisodeRange>>,
    onClick: (episodeStart: Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        for (i in content.entries) {
            Column(
            ) {
                CenteredBox(
                    modifier = Modifier.height(25.dp)
                ) {
                    Text(
                        text = i.key,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(i.value) {
                        com.redline.anistalker.ui.components.EpisodeRange(
                            start = it.start,
                            end = it.end,
                        ) {
                            onClick(it.start)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeDownloadCard(
    animeInfo: AnimeDownload,
    ongoingDownloads: List<OngoingEpisodeDownload>,
    onPauseAll: (() -> Unit)? = null,
    onCancelAll: (() -> Unit)? = null,
    onDownloadClick: ((download: OngoingEpisodeDownload) -> Unit)? = null,
    onClick: (animeId: Int) -> Unit
) {
    val isPreview = LocalInspectionMode.current
    val outline = Color.White.copy(alpha = .2f)
    val bg = remember {
        if (isPreview) Color(0xFF1E1E1E)
        else secondary_background
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(.5.dp, outline, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
    ) {
        AnimeDownloadCard_Details(
            details = animeInfo,
        ) { onClick(animeInfo.animeId.zoroId) }

        if (ongoingDownloads.isNotEmpty()) {
            Divider(color = outline)
            AnimeDownloadCard_Progress(
                downloads = ongoingDownloads,
                onPause = { onPauseAll?.let { it() } },
                onCancel = { onCancelAll?.let { it() } },
                onEpisodePause = { d -> onDownloadClick?.let { it(d) } }
            )
        }
    }
}

@Composable
fun EpisodeDownloadContentView(
    details: AnimeEpisodeDetail,
    onClick: ((quality: VideoQuality) -> Unit)? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    val labelBg = MaterialTheme.colorScheme.primaryContainer

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 20.dp)
    ) {
        CenteredBox(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(labelBg)
        ) {
            Text(
                text = "EP" + details.episode.toString(),
                color = primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = details.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.size(4.dp))
            if (details.isFiller) {
                CenteredBox(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 15.dp)
                        .height(20.dp)
                ) {
                    Text(
                        text = "Filler",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(secondary_background)
                .height(38.dp)
                .padding(4.dp)
        ) {
            CenteredBox(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .clickable { onClick?.let { it(VideoQuality.HD) } }
            ) {
                Image(
                    painter = awarePainterResource(R.drawable.hd),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                )
            }
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline
            )
            CenteredBox(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .clickable { onClick?.let { it(VideoQuality.UHD) } }
            ) {
                Image(
                    painter = awarePainterResource(R.drawable.uhd),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                )
            }
        }
    }
}

@Composable
fun EpisodeDownloadView(
    details: EpisodeDownload,
    statusInfo: OngoingEpisodeDownload? = null,
    onPause: (() -> Unit)? = null,
    onClick: ((episodeId: Int) -> Unit)? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    val labelBg = MaterialTheme.colorScheme.primaryContainer
    val color = MaterialTheme.colorScheme.primary
    val dim = Color.White.copy(alpha = .4f)

    val progress =
        if (LocalInspectionMode.current) .4f
        else if (statusInfo == null || statusInfo.duration <= 0f) 0f
        else (statusInfo.downloadedDuration / statusInfo.duration)

    val progressValue = progress.toString().format("%.1f")

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick?.let { it(details.id) } }
    ) {
        CenteredBox(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(labelBg)
                .clickable { onPause?.let { it() } }
        ) {
            Text(
                text = "EP" + details.num.toString(),
                color = primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = details.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.size(5.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    text = VideoQuality.UHD.value,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = statusInfo?.let { it.downloadSpeed.toSizeFormat() + "/s" }
                        ?: details.size.toSizeFormat(),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Divider(modifier = Modifier.size(4.dp), color = dim)
                Text(
                    text = if (statusInfo != null) progressValue else details.duration.toDurationFormat(),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.size(5.dp))

            if (statusInfo != null) {
                if (statusInfo.status == DownloadStatus.WAITING) {
                    LinearProgressIndicator(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        trackColor = Color.White.copy(alpha = .2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                } else {
                    LinearProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        trackColor = Color.White.copy(alpha = .2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaEventView(
    event: Event,
    image: Bitmap? = null,
    onStream: ((eventId: Int) -> Unit)? = null,
    onDownload: ((eventId: Int) -> Unit)? = null,
    onClick: ((eventId: Int) -> Unit)? = null,
) {
    val isManga = /*event is Event.MangaEvent*/ false
    val bg = secondary_background

    val heading = event.heading
    val title = event.title
    var contentNum = 0
    val content = when (event) {
        is Event.AnimeEvent -> {
            contentNum = event.episodeNum
            when (event.type) {
                EventType.NEW_EPISODE -> "Episode"
                EventType.ANIME_COMPLETE -> "Total Episodes"
                else -> "Unknown"
            }
        }

//        is Event.MangaEvent -> {
//            contentNum = event.chapterNum
//            when (event.type) {
//                EventType.NEW_CHAPTER -> "Episode"
//                EventType.MANGA_COMPLETE -> "Total Episodes"
//                else -> "Unknown"
//            }
//        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick?.let { it(event.id) } }
        ) {

            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 100.dp)
            ) {
                this@Row.AnimatedVisibility(
                    visible = image == null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .fillMaxSize()
                    ) {}
                }

                this@Row.AnimatedVisibility(
                    visible = image != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    if (image != null) {
                        Image(
                            painter = BitmapPainter(image.asImageBitmap()),
                            contentDescription = null,
                            modifier = Modifier
                                .border(
                                    .5.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = heading,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row {
                    Text(
                        text = content,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = contentNum.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(10.dp))

        Row(
        ) {
            if (isManga) {
                ReadButton(modifier = Modifier.weight(1f)) {
                    onStream?.let { it(event.id) }
                }
            } else {
                StreamButton(modifier = Modifier.weight(1f)) {
                    onStream?.let { it(event.id) }
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            DownloadButton(modifier = Modifier.weight(1f)) {
                onDownload?.let { it(event.id) }
            }
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun P_AnimeCard() {
    AniStalkerTheme {
        AnimeCardView(animeCard = AnimeHalf()) {

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_AnimeCardSimmer() {
    AniStalkerTheme {
        AnimeCardView() {

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_Watchlist() {
    AniStalkerTheme {
        WatchlistCard(Watchlist()) {
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_WatchlistSimmer() {
    AniStalkerTheme {
        WatchlistCard {
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_AnimeDownloadCard_ProgressOnly() {
    AniStalkerTheme {
        AnimeDownloadCard(
            animeInfo = AnimeDownload(),
            ongoingDownloads = listOf(
                OngoingEpisodeDownload(),
                OngoingEpisodeDownload(),
                OngoingEpisodeDownload(),
                OngoingEpisodeDownload(),
                OngoingEpisodeDownload(),
            ),
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_AnimeDownloadCard_OnlyContent() {
    AniStalkerTheme {
        AnimeDownloadCard(
            animeInfo = AnimeDownload(),
            ongoingDownloads = listOf(
            ),
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_AnimeDownloadCard_AllIn() {
    AniStalkerTheme {
        AnimeDownloadCard(
            animeInfo = AnimeDownload(),
            ongoingDownloads = listOf(
                OngoingEpisodeDownload(duration = 1f, downloadedDuration = .5f),
                OngoingEpisodeDownload(
                    duration = 1f,
                    downloadedDuration = .6f,
                    status = DownloadStatus.PAUSED
                ),
                OngoingEpisodeDownload(duration = 1f, downloadedDuration = 1f),
            ),
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_EpisodeDownloadContentView() {
    AniStalkerTheme {
        EpisodeDownloadContentView(
            details = AnimeEpisodeDetail()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun P_EpisodeDownloadContentView_Filler() {
    AniStalkerTheme {
        EpisodeDownloadContentView(
            details = AnimeEpisodeDetail(isFiller = true)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun P_EpisodeDownloadView() {
    AniStalkerTheme {
        EpisodeDownloadView(
            details = EpisodeDownload(),
            statusInfo = OngoingEpisodeDownload()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun P_EpisodeDownloadView_Filler() {
    AniStalkerTheme {
        EpisodeDownloadView(
            details = EpisodeDownload()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun P_EventView_Anime() {
    AniStalkerTheme {
        MediaEventView(
            event = Event.AnimeEvent().apply {
                type = EventType.NEW_EPISODE
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun P_EventView_Manga() {
    AniStalkerTheme {
//        MediaEventView(
//            event = Event.MangaEvent().apply {
//                type = EventType.MANGA_COMPLETE
//            }
//        )
    }
}


// endregion