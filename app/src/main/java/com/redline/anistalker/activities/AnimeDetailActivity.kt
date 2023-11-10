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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.redline.anistalker.R
import com.redline.anistalker.activities.screens.EpisodeDownloadSheet
import com.redline.anistalker.activities.screens.WatchlistOperationSheet
import com.redline.anistalker.managements.UserData
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeDate
import com.redline.anistalker.models.AnimeStatus
import com.redline.anistalker.models.Watchlist
import com.redline.anistalker.ui.components.AnimeStatus
import com.redline.anistalker.ui.components.AsyncImage
import com.redline.anistalker.ui.components.BigEpisodeTail
import com.redline.anistalker.ui.components.CenteredBox
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class AnimeDetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
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
                    val userWatchlist by UserData.watchlist.collectAsState()
                    val currentAnime by viewModel.currentAnime.collectAsState()
                    val episodeList by viewModel.episodeList.collectAsState()
                    val animeTrack by viewModel.animeTrack.collectAsState()

                    var showWatchlistSheet by rememberSaveable { mutableStateOf(false) }
                    var showCreationScreen by rememberSaveable { mutableStateOf(false) }

                    var showDownloadScreen by rememberSaveable { mutableStateOf(false) }

                    AnimeDetailScreen(
                        anime = anime,
                        images = images,
                        watchlist = watchlist,
                        currentAnime = currentAnime,
                        onStream = { /*TODO*/ },
                        onDownload = { showDownloadScreen = true },
                        onSetCurrentAnime = { viewModel.toggleCurrentAnime() },
                        onAddToWatchlist = { showWatchlistSheet = true },
                        onRemoveWatchlist = { viewModel.removeAnimeFromWatchlist(it) }
                    ) {
                        onBackPressedDispatcher.onBackPressed()
                    }

                    EpisodeDownloadSheet(
                        show = showDownloadScreen,
                        episodeList = episodeList,
                        episodeTrack = animeTrack,
                        onAnimeTrackChange = {
                             viewModel.setAnimeTrack(it)
                        },
                        onDownloadEpisode = {
                            viewModel.downloadEpisodes(it)
                        }
                    ) {
                        showDownloadScreen = false
                    }

                    anime?.let {
                        WatchlistOperationSheet(
                            show = showWatchlistSheet,
                            showCreationScreen = showCreationScreen,
                            showBackPressButton = true,
                            watchlist = userWatchlist,
                            anime = anime!!.let {
                                AnimeCard(
                                    id = it.id.zoroId,
                                    name = it.title,
                                    image = it.image,
                                    status = it.status,
                                    type = it.type,
                                    episodes = it.episodes,
                                    isAdult = it.isAdult,
                                )
                            },
                            onCreationScreenToggled = {
                                showCreationScreen = it
                            }
                        ) {
                            showWatchlistSheet = false
                            showCreationScreen = false
                        }
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
    currentAnime: Boolean,
    onStream: () -> Unit,
    onDownload: () -> Unit,
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

    val listState = rememberLazyListState()
    var topBarVisibility by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = anime) {
        withContext(Dispatchers.IO) {
            try {
                bgImage = anime?.image?.run {
                    val bitmap = BitmapFactory.decodeStream(Net.getStream(this))
                    context.blurImage(bitmap, 20f)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }.collect {
            topBarVisibility = listState.firstVisibleItemIndex > 0 || it > 40
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
                        contentScale = ContentScale.Crop,
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
//                            .offset(y = (-scrolledValue).dp)
                    )
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxSize()
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
                                    .aspectRatio(0.7f, true)
                            )
                        }
                    }
                }

                item {
                    AnimeDetailedView(
                        anime = anime,
                        AnimeDate(),
                        anime.end,
                        currentAnime,
                        onStream = onStream,
                        onDownload = onDownload,
                        onSetCurrentAnime = onSetCurrentAnime,
                        onSeasonClicked = { }
                    )
                }

                item {
                    Column(
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
                    if (watchlist.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.size(20.dp))
                        }
                        items(
                            items = it
                        ) { watch ->
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp)
                            ) {
                                WatchlistCard(
                                    watchlist = watch
                                ) { watchlist ->
                                    onRemoveWatchlist(watchlist.id)
                                }
                                Spacer(modifier = Modifier.size(10.dp))
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    } else item {
                        CenteredBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Text(
                                text = "Add this Anime in any watchlist.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = .6f)
                            )
                        }
                    }
                } ?: item {
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
        } ?: Box(
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
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .let {
                    if (topBarVisibility) it.shadow(4.dp)
                    else it
                }
        ) {
            AnimatedVisibility(
                visible = topBarVisibility,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .matchParentSize()
                    .fillMaxWidth()
            ) {
                bgImage?.let {
                    Image(
                        painter = BitmapPainter(it.asImageBitmap()),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp, BlurredEdgeTreatment.Rectangle)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        SolidColor(bg.copy(alpha = .92f))
                                    )
                                }
                            }
                    )
                } ?: Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .heightIn(min = 60.dp)
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onBackPressed() }
                        .clip(buttonShape)
                        .let {
                            if (anime != null) it
                                .border(.5.dp, MaterialTheme.colorScheme.primary, buttonShape)
                                .background(secondary_background)
                            else it
                        }
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimeDetailedView(
    anime: Anime,
    nextEpisodeDate: AnimeDate,
    endDate: AnimeDate,
    currentAnime: Boolean,
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

    val currentButtonColors = ButtonDefaults.buttonColors(
        containerColor =
        if (currentAnime) MaterialTheme.colorScheme.primaryContainer
        else secondary_background,
        contentColor =
        if (currentAnime) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.primary
    )

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
                text = "${anime.season.value} ${anime.year}",
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
            }
            if (endDate.isValid() && isAiring || LocalInspectionMode.current) Row(
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

            Button(
                onClick = { onSetCurrentAnime() },
                colors = currentButtonColors,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(.5.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val contentColor = LocalContentColor.current
                Text(
                    text =
                    if (currentAnime) "Remove as Current Anime"
                    else "Set As Current Anime",
                    fontSize = 12.sp,
                    color = contentColor
                )
            }
        }

        Column(
            modifier = Modifier
                .border(.5.dp, MaterialTheme.colorScheme.onPrimaryContainer, shape)
                .fillMaxWidth()
        ) {
            FlowRow(
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

            if (anime.otherNames.isNotEmpty()) {
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
        }

        ExpandableBlock(
            label = "Plot Summary",
            height = 50.dp,
            expand = plotSummaryExpanded,
            onClick = { plotSummaryExpanded = !plotSummaryExpanded }
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
            )
            {
                Text(
                    text = anime.description.trimStart(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        if (anime.relations.isNotEmpty()) ExpandableBlock(
            label = "Seasons",
            height = 50.dp,
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
                                            .5.dp,
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
                currentAnime = false,
                { }, { }, { }, { }, { }, { },
            )
        }
    }
}