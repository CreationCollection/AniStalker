package com.redline.anistalker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.models.DownloadStatus
import com.redline.anistalker.models.MangaCard
import com.redline.anistalker.models.MangaDownload
import com.redline.anistalker.models.MangaDownloadContent
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.dark_background
import com.redline.anistalker.ui.theme.secondary_background

@Composable
fun MangaCardView(
    manga: MangaCard,
    imageUrl: String? = null,
    onClick: (mangaId: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(secondary_background)
            .width(120.dp)
            .padding(6.dp)
            .clickable { onClick(manga.id) }
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(dark_background)
                .fillMaxWidth()
                .height(140.dp)
        ) {

            AsyncImage(
                url = imageUrl,
                loadColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Divider()
            CenteredBox(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "${manga.chapters} Chapters",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = manga.title,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun MangaDownloadView(
    info: MangaDownload,
    downloadInfo: MangaDownloadContent,
    imageUrl: String? = null,
    onPause: ((mangaId: String) -> Unit)? = null,
    onCancel: ((mangaId: String) -> Unit)? = null,
    onClick: ((mangaId: String) -> Unit)? = null,
) {
    val primaryButtonText =
        if (downloadInfo.downloaded) "Read"
        else if (downloadInfo.status == DownloadStatus.PAUSED) "Resume"
        else "Pause"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clickable { onClick?.let { it(info.id) } }
    ) {
        AsyncImage(
            url = imageUrl,
            loadColor = secondary_background,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .size(width = 100.dp, height = 140.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = info.title,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row {
                        Text(
                            text = "CHAPTERS",
                            color = Color.White.copy(alpha = .5f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "${info.downloadableChapters.size} | ${info.downloadedChapters.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Divider(modifier = Modifier.size(4.dp))
                    if (!downloadInfo.downloaded) {
                        Row {
                            Text(
                                text = "PAGES",
                                color = Color.White.copy(alpha = .5f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "${downloadInfo.totalPages} | ${downloadInfo.downloadedPages}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(5.dp))
                if (downloadInfo.status == DownloadStatus.WAITING) {
                    LinearProgressIndicator()
                } else {
                    val value: Float =
                        if (LocalInspectionMode.current) .4f
                        else if (info.downloadableChapters.isEmpty()) 0f
                        else (info.downloadedChapters.size / info.downloadableChapters.size).toFloat()
                    LinearProgressIndicator(progress = value)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    shape = RoundedCornerShape(10.dp),
                    onClick = { onPause?.let { it(info.id) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = primaryButtonText
                    )
                }
                Spacer(modifier = Modifier.size(5.dp))
                OutlinedButton(
                    shape = RoundedCornerShape(10.dp),
                    onClick = { onCancel?.let { it(info.id) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (downloadInfo.downloaded) "Manage" else "Cancel"
                    )
                }
            }
        }
    }
}

// region Preview

@Preview
@Composable
private fun P_MangaCardView() {
    AniStalkerTheme {
        MangaCardView(manga = MangaCard()) {
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun P_MangaDownloadView() {
    AniStalkerTheme {
        MangaDownloadView(
            info = MangaDownload(),
            downloadInfo = MangaDownloadContent()
        )
    }
}

// endregion