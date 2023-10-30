package com.redline.anistalker.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.redline.anistalker.R
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeDate
import com.redline.anistalker.models.AnimeStatus
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.ui.components.AnimeStatus
import com.redline.anistalker.ui.components.AsyncImage
import com.redline.anistalker.ui.components.BigEpisodeTail
import com.redline.anistalker.ui.components.DownloadButton
import com.redline.anistalker.ui.components.ExpandableBlock
import com.redline.anistalker.ui.components.StreamButton
import com.redline.anistalker.ui.components.WatchlistCard
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.dark_background
import com.redline.anistalker.ui.theme.secondary_background
import com.redline.anistalker.utils.blurImage
import com.redline.anistalker.viewModels.pages.AnimePageViewModel
import kotlin.math.ceil

class AnimeDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val animeId = intent.getIntExtra("animeId", 0)

        setContent {
            AniStalkerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = aniStalkerColorScheme.background
                ) {
                    val viewModel by viewModels<AnimePageViewModel>()
                    viewModel.initialize(animeId)

                    val anime by viewModel.anime.collectAsState()
                    val images by viewModel.images.collectAsState()
                    val watchlist by viewModel.watchlist.collectAsState()

                    AnimeDetailScreen(
                        anime = anime,
                        images = images,
                        watchlist = watchlist,
                        onStream = { },
                        onDownload = { },
                        onEpisodeEvent = { },
                        onCompletionEvent = { },
                        onSetCurrentAnime = { },
                        onAddToWatchlist = { },
                        onRemoveWatchlist = { }
                    ) {

                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeDetailScreen(
    anime: Anime?,
    images: List<String>,
    watchlist: List<Watchlist>?,
    onStream: () -> Unit,
    onDownload: () -> Unit,
    onEpisodeEvent: () -> Unit,
    onCompletionEvent: () -> Unit,
    onSetCurrentAnime: () -> Unit,
    onAddToWatchlist: () -> Unit,
    onRemoveWatchlist: (Int) -> Unit,
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current

    val bg = aniStalkerColorScheme.background
    val buttonShape = RoundedCornerShape(6.dp)
    val imageShape = RoundedCornerShape(6.dp)
    var bgImage by remember {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(key1 = anime) {
        try {
            bgImage = anime?.image?.run {
                val bitmap = BitmapFactory.decodeStream(Net.getStream(this))
                context.blurImage(bitmap, 20f)
            }
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        anime?.let {
            AnimatedVisibility(
                visible = bgImage != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                bgImage?.let {
                    Image(
                        painter = BitmapPainter(it.asImageBitmap()),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.27f)
                            .blur(20.dp, BlurredEdgeTreatment.Rectangle)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        Brush.verticalGradient(
                                            0f to bg.copy(alpha = .68f),
                                            1f to bg
                                        )
                                    )
                                }
                            }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxSize()
                    .padding(bottom = 60.dp)
            ) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth()
                    ) {
                        items(
                            items = images
                        ) {
                            AsyncImage(
                                url = it,
                                loadColor = secondary_background,
                                modifier = Modifier
                                    .clip(imageShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, imageShape)
                                    .fillMaxHeight()
                                    .aspectRatio(1.42f, true)
                            )
                        }
                    }
                }

                item {
                    AnimeDetailedView(
                        anime = anime,
                        AnimeDate(),
                        anime.end,
                        onEpisodeEvent,
                        onCompletionEvent,
                        onSetCurrentAnime,
                        onStream,
                        onDownload,
                        onSeasonClicked = { }
                    )
                }

                item {
                    Column (
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Watchlist",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clickable { onAddToWatchlist() }
                                .clip(buttonShape)
                                .border(.5.dp, MaterialTheme.colorScheme.outline, buttonShape)
                                .background(secondary_background)
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Add to Watchlist".uppercase(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                watchlist?.let {
                    items(
                        items = it
                    ) { watch ->
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            WatchlistCard(
                                watchlist = watch
                            ) {

                            }
                        }
                    }
                } ?:
                item {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp))
                    }
                }
            }
        } ?:

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp)
            )

//            Text(
//                text = "Unable to fetch information!",
//            )
        }

        Box(
            modifier = Modifier
                .background(Color.Transparent)
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row (
                verticalAlignment  = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onBackPressed() }
                    .clip(buttonShape)
                    .border(.5.dp, MaterialTheme.colorScheme.primary, buttonShape)
                    .background(secondary_background)
                    .shadow(10.dp, ambientColor = Color.Black.copy(alpha = .75f))
                    .height(35.dp)
                    .padding(horizontal = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Back",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimeDetailedView(
    anime: Anime,
    nextEpisodeDate: AnimeDate,
    endDate: AnimeDate,
    onEpisodeEvent: () -> Unit,
    onCompletionEvent: () -> Unit,
    onSetCurrentAnime: () -> Unit,
    onStream: () -> Unit,
    onDownload: () -> Unit,
    onSeasonClicked: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val notifyColor = MaterialTheme.colorScheme.tertiary

    var plotSummaryExpanded by remember {
        mutableStateOf(false)
    }
    var seasonExpanded by remember {
        mutableStateOf(false)
    }

    val isAiring = anime.status == AnimeStatus.AIRING

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = anime.title.english,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BigEpisodeTail(
                episodes = anime.episodes
            )
            Divider(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(4.dp),
            )
            AnimeStatus(
                isAiring = anime.status == AnimeStatus.AIRING,
                type = anime.type
            )
            Divider(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(4.dp),
            )
            Text(
                text = "${ anime.season.value } ${ anime.year }",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            if (nextEpisodeDate.isValid() && isAiring || LocalInspectionMode.current) Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, notifyColor, shape)
                        .height(40.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = "Next Episode On",
                        color = notifyColor,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = nextEpisodeDate.toDateString(),
                        fontWeight = FontWeight.Bold,
                        color = notifyColor,
                        fontSize = 12.sp,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable { onEpisodeEvent() }
                        .clip(shape)
                        .border(1.dp, notifyColor, shape)
                        .size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.notification_outline),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(notifyColor),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (endDate.isValid() && isAiring || LocalInspectionMode.current) Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, notifyColor, shape)
                        .height(40.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = "Completes On",
                        color = notifyColor,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = endDate.toDateString(),
                        fontWeight = FontWeight.Bold,
                        color = notifyColor,
                        fontSize = 12.sp
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable { onCompletionEvent() }
                        .clip(shape)
                        .border(1.dp, notifyColor, shape)
                        .size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.notification_outline),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(notifyColor),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
            ) {
                StreamButton(
                    modifier = Modifier.weight(1f)
                ) {
                    onStream()
                }
                Spacer(modifier = Modifier.size(10.dp))
                DownloadButton(
                    modifier = Modifier.weight(1f)
                ) {
                    onDownload()
                }
            }
        }

        Column(
            modifier = Modifier
                .border(.5.dp, MaterialTheme.colorScheme.onPrimaryContainer, shape)
                .fillMaxWidth()
        ) {
            FlowRow (
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 15.dp)
            ) {
                repeat(anime.genres.size) {
                    if (it > 0) {
                        Divider(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .size(4.dp)
                                .align(Alignment.CenterVertically),
                        )
                    }

                    Text(
                        text = anime.genres[it].uppercase(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer)

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 15.dp)
            ) {
                repeat(anime.otherNames.size) {
                    Text(
                        text = anime.otherNames[it],
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        ExpandableBlock(
            label = "Plot Summary",
            height = 40.dp,
            expand = plotSummaryExpanded,
            onClick = { plotSummaryExpanded = !plotSummaryExpanded }
        ) {
            Text(
                text = anime.description.trimStart(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(20.dp)
            )
        }
        ExpandableBlock(
            label = "Seasons",
            height = 40.dp,
            expand = seasonExpanded,
            onClick = { seasonExpanded = !seasonExpanded }
        ) {
            val arrangement = Arrangement.spacedBy(10.dp)
            Column(
                verticalArrangement = arrangement,
                modifier = Modifier
                    .padding(20.dp)
            ) {
                val rows = ceil(anime.relations.size / 2.0).toInt()
                repeat(rows) { i ->
                    Row(
                        horizontalArrangement = arrangement,
                    ) {
                        repeat(2) { j ->
                            val index = (i * 2) + j
                            if (index < anime.relations.size) {
                                val relation = anime.relations[index]
                
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clickable { onSeasonClicked(relation.zoroId) }
                                        .clip(shape)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onPrimaryContainer,
                                            shape
                                        )
                                        .weight(1f)
                                        .height(40.dp)
                                ) {
                                    Text(
                                        text = relation.title,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    AsyncImage(url = relation.image, loadColor = dark_background)
                                }
                                
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun previewScreen() {
    AniStalkerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = aniStalkerColorScheme.background
        ) {
            AnimeDetailScreen(
                anime = Anime(),
                images = listOf(),
                watchlist = null,
                { }, { }, { }, { }, { }, { }, { }, { }
            )
        }
    }
}