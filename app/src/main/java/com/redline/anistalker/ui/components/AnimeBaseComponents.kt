package com.redline.anistalker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.models.AnimeEpisode
import com.redline.anistalker.models.AnimeType
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.WatchlistPrivacy
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.dark_background
import com.redline.anistalker.ui.theme.dullRed
import com.redline.anistalker.ui.theme.secondary_background

@Composable
fun BigEpisodeTail(
    episodes: AnimeEpisode
) {
    val bg = dark_background
    val outline = secondary_background
    val totalBg = MaterialTheme.colorScheme.primaryContainer
    val totalFg = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .height(20.dp)
            .wrapContentWidth()
            .border(1.dp, outline, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
    ) {

        Row (
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
        ) {

            for (i in 0..1) {
                val it = i == 0
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(28.dp)
                        .height(15.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(
                            if (it) R.drawable.sub else R.drawable.dub
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(bg),
                        modifier = Modifier
                            .size(18.dp)
                    )
                }

                Text(
                    text = (if (it) episodes.sub else episodes.dub).toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            }
        }

        if (episodes.total > 0) {
            CenteredBox(
                modifier = Modifier
                    .background(totalBg)
                    .fillMaxHeight()
                    .defaultMinSize(minWidth = 35.dp)
                    .padding(horizontal = 5.dp)
            ) {
                Text(
                    text = episodes.total.toString(),
                    color = totalFg,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun SmallEpisodeTail(
    episodes: AnimeEpisode
) {
    val bg = dark_background
    val totalBg = MaterialTheme.colorScheme.primaryContainer
    val totalFg = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = Modifier
            .height(20.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
    ) {
        if (episodes.total > 0) {
            CenteredBox(
                modifier = Modifier
                    .background(totalBg)
                    .fillMaxHeight()
                    .defaultMinSize(minWidth = 30.dp)
                    .padding(horizontal = 5.dp)
            ) {
                Text(
                    text = episodes.total.toString(),
                    color = totalFg,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row (
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            for (i in 0..1) {
                val it = i == 0
                Row(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(18.dp)
                            .height(15.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Image(
                            painter = painterResource(
                                if (it) R.drawable.sub else R.drawable.dub
                            ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier
                                .size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = (if (it) episodes.sub else episodes.dub).toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistPrivacy(
    privacy: WatchlistPrivacy
) {
    val color = when (privacy) {
        WatchlistPrivacy.PUBLIC -> Color(0xFF8CBEDB)
        WatchlistPrivacy.SHARED -> Color(0xFFF85A5A)
        WatchlistPrivacy.PRIVATE -> Color(0xFFFFB871)
    }

    Text(
        text = privacy.value,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )
}

@Composable
fun AnimeStatus(
    isAiring: Boolean,
    type: AnimeType
) {
    val color =
        if (isAiring) Color(0xFF81F16F)
        else Color.White

    val modifier = Modifier
        .also {
            if (isAiring)
                it.border(1.dp, color, RoundedCornerShape(2.dp))
        }
        .clip(RoundedCornerShape(2.dp))
        .background(Color(0xFF1E1E1E))
        .wrapContentSize()
        .padding(horizontal = 6.dp, vertical = 2.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text(
            text = type.value,
            color = color,
            fontSize = 12.sp
        )
    }
}

@Composable
fun EpisodeRange(
    start: Int,
    end: Int,
    onClick: (() -> Unit)? = null
) {
    val color = MaterialTheme.colorScheme.onPrimaryContainer

    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .border(.4.dp, color, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .height(20.dp)
            .padding(horizontal = 10.dp)
            .clickable { onClick?.let { it() } }
    ) {
        Text(
            text = "EP",
            color = color,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = start.toString(),
            color = color,
            fontSize = 12.sp,
        )
        Text(
            text = " - ",
            color = color,
            fontSize = 12.sp,
        )
        Text(
            text = end.toString(),
            color = color,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun EpisodeProgress(
    num: Int,
    value: Float,
    status: DownloadStatus = DownloadStatus.RUNNING,
    onClick: (() -> Unit)? = null
) {
    val foreColor = MaterialTheme.colorScheme.onPrimaryContainer

    val color = when (status) {
        DownloadStatus.RUNNING -> MaterialTheme.colorScheme.tertiaryContainer
        DownloadStatus.PAUSED -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val bg = secondary_background

    Box (
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .border(.5.dp, foreColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .height(30.dp)
            .wrapContentWidth()
            .widthIn(max = 100.dp)
            .clickable { onClick?.let { it() } }
    ) {
        if (status != DownloadStatus.WAITING) {
            LinearProgressIndicator(
                progress = value,
                color = color,
                trackColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LinearProgressIndicator(
                color = color,
                trackColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = "EP",
                color = foreColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = num.toString(),
                color = foreColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DownloadButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(),
    onClick: () -> Unit
) {
    val color = dullRed

    val colors = ButtonDefaults.buttonColors(
        containerColor = color,
        contentColor = Color.White,
        disabledContainerColor = color.copy(alpha = .2f),
        disabledContentColor = Color.White
    )

    Button (
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(6.dp),
        contentPadding = contentPadding,
    ) {
        val contentColor = LocalContentColor.current
        Image(
            painter = painterResource(R.drawable.downloads),
            contentDescription = null,
            colorFilter = ColorFilter.tint(contentColor),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Download",
            color = contentColor,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun StreamButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(),
    onClick: () -> Unit
) {
    val color = dullRed

    val colors = ButtonDefaults.buttonColors(
        containerColor = Color.White,
        contentColor = color,
        disabledContainerColor = Color.White.copy(alpha = .5f),
        disabledContentColor = color
    )

    Button (
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        shape = RoundedCornerShape(6.dp),
    ) {
        val contentColor = LocalContentColor.current
        Image (
            painter = painterResource(R.drawable.play),
            contentDescription = null,
            colorFilter = ColorFilter.tint(contentColor),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Stream",
            color = contentColor,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun ReadButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(),
    onClick: () -> Unit
) {
    val color = dullRed

    val colors = ButtonDefaults.buttonColors(
        containerColor = Color.White,
        contentColor = color,
        disabledContainerColor = Color.White.copy(alpha = .5f),
        disabledContentColor = color
    )

    Button (
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        shape = RoundedCornerShape(6.dp),
    ) {
        val contentColor = LocalContentColor.current
        Image (
            painter = painterResource(R.drawable.read),
            contentDescription = null,
            colorFilter = ColorFilter.tint(contentColor),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Read",
            color = contentColor,
            fontSize = 12.sp,
        )
    }
}

// region Previews
@Preview(showBackground = false)
@Composable
private fun Preview_BigEpisodeTail() {
    AniStalkerTheme { BigEpisodeTail(AnimeEpisode(sub = 10, dub = 12, total = 24)) }
}

@Preview(showBackground = false)
@Composable
private fun Preview_SmallEpisodeTail() {
    AniStalkerTheme { SmallEpisodeTail(AnimeEpisode(sub = 10, dub = 12, total = 24)) }
}

@Preview(showBackground = false)
@Composable
private fun Preview_SmallEpisodeTail_ZeroTotal() {
    AniStalkerTheme { SmallEpisodeTail(AnimeEpisode(sub = 10, dub = 12, total = 0)) }
}

@Preview(showBackground = false)
@Composable
private fun Preview_WatchlistPrivacy() {
    AniStalkerTheme { WatchlistPrivacy(WatchlistPrivacy.PRIVATE) }
}

@Preview(showBackground = false, backgroundColor = 0xFFE91E63)
@Composable
private fun Preview_AnimeStatus() {
    AniStalkerTheme { AnimeStatus(false, AnimeType.SPECIAL) }
}

@Preview(showBackground = false, backgroundColor = 0xFFE91E63)
@Composable
private fun Preview_EpisodeRange() {
    AniStalkerTheme { EpisodeRange(1, 10) }
}

@Preview(showBackground = false, backgroundColor = 0xFFE91E63)
@Composable
private fun Preview_EpisodeProgress() {
    AniStalkerTheme { EpisodeProgress(1, .4f) }
}

@Preview(showBackground = false, backgroundColor = 0xFFE91E63)
@Composable
private fun Preview_DownloadButton() {
    DownloadButton {  }
}

@Preview(showBackground = false, backgroundColor = 0xFFE91E63)
@Composable
private fun Preview_StreamButton() {
    StreamButton {  }
}

@Preview(showBackground = false, backgroundColor = 0xFFE91E63)
@Composable
private fun Preview_ReadButton() {
    ReadButton {  }
}

// endregion