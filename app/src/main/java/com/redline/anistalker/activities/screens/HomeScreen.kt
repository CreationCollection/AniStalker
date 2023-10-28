package com.redline.anistalker.activities.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeEpisode
import com.redline.anistalker.models.AnimeSpotlight
import com.redline.anistalker.ui.components.AnimeCardView
import com.redline.anistalker.ui.components.AsyncImage
import com.redline.anistalker.ui.components.BigEpisodeTail
import com.redline.anistalker.ui.components.CenteredBox
import com.redline.anistalker.ui.components.DownloadButton
import com.redline.anistalker.ui.components.ErrorSnackBar
import com.redline.anistalker.ui.components.StreamButton
import com.redline.anistalker.ui.components.rememberSimmerValue
import com.redline.anistalker.ui.components.simmerEnd
import com.redline.anistalker.ui.components.simmerStart
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.utils.wrap
import kotlinx.coroutines.delay


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    spotlights: List<AnimeSpotlight>,
    animeList: List<AnimeCard?>,
    animeCategories: List<AnimeCategory>,
    currentAnime: Anime? = null,
    lastCurrentAnimeEpisode: Int = 0,
    spotlightError: String? = null,
    loadingError: String? = null,
    onLoadSpotlight: (() -> Unit)? = null,
    onSpotlightClicked: ((spotlight: AnimeSpotlight) -> Unit)? = null,
    onStreamCurrentAnime: (() -> Unit)? = null,
    onDownloadCurrentAnime: (() -> Unit)? = null,
    onCurrentAnimeClicked: (() -> Unit)? = null,
    onCategoryChanged: ((category: AnimeCategory) -> Unit)? = null,
    onAnimeCardClicked: ((anime: AnimeCard) -> Unit)? = null,
    onLoadNextPage: () -> Unit,
) {
    var currentCategory by rememberSaveable {
        mutableIntStateOf(0)
    }
    var coverValue by rememberSaveable {
        mutableFloatStateOf(0f)
    }
    val simmerValue by rememberSimmerValue()
    val isInPreview = LocalInspectionMode.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                coverValue -= available.y
                return super.onPreScroll(available, source)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize()
    ) {
        stickyHeader {
            AnimatedVisibility(visible = coverValue > 40f) {
                Box(
                    modifier = Modifier
                        .background(aniStalkerColorScheme.background.copy(alpha = .8f))
                        .fillMaxWidth()
                        .statusBarsPadding()
                )
            }
        }

        item {
            if (spotlightError == null)
                AnimeSpotlightView(
                    spotlights = spotlights,
                    onSpotlightClicked = { onSpotlightClicked?.run { this(it) } }
                )
            else {
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .padding(20.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = .2f))
                            .clickable { onLoadSpotlight?.run { this() } }
                            .fillMaxSize()
                    ) {
                        Text(
                            text = spotlightError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        item {
            CurrentAnimeView(
                currentAnime = currentAnime,
                lastCurrentAnimeEpisode = lastCurrentAnimeEpisode,
                onStreamCurrentAnime = { onStreamCurrentAnime?.run { this() } },
                onDownloadCurrentAnime = { onDownloadCurrentAnime?.run { this() } },
                onCurrentAnimeClicked = { onCurrentAnimeClicked?.run { this() } }
            )
        }

        stickyHeader {
            AnimeCategoryTabView(
                animeCategory = animeCategories,
                selectedTab = currentCategory,
            ) {
                currentCategory = it
                onCategoryChanged?.run { this(animeCategories[it]) }
            }
        }

        if (animeList.isEmpty() && loadingError == null)
            onLoadNextPage()

        itemsIndexed(
            items = animeList,
            key = { index, it -> if (it == null || isInPreview) index + 3 else it.id + 10000 },
            contentType = { _, it -> if (it == null) null else AnimeCard::class.java }
        ) { index, it ->
            if (index > animeList.size - 5 && loadingError == null)
                onLoadNextPage()

            AnimeCardView(
                animeCard = it,
                showOwner = false,
                simmerValue = (simmerValue + (index * .1f)).wrap(0f, 1f)
            ) {
                onAnimeCardClicked?.run { this(it) }
            }
        }

        if (loadingError != null) item {
            Box(
                modifier = Modifier.padding(20.dp)
            ) {
                ErrorSnackBar(
                    loadingError = loadingError
                ) {
                    onLoadNextPage()
                }
            }
        }
    }
}

@Composable
private fun AnimeSpotlightSimmerView() {
    val corners = RoundedCornerShape(10.dp)
    val simmerValue by rememberSimmerValue()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(corners)
                    .background(lerp(simmerStart, simmerEnd, simmerValue.wrap(0f, 1f)))
                    .width(80.dp)
                    .height(14.dp)
            )

            Box(
                modifier = Modifier
                    .clip(corners)
                    .background(lerp(simmerStart, simmerEnd, (simmerValue + .1f).wrap(0f, 1f)))
                    .fillMaxWidth(.75f)
                    .height(18.dp)
            )

            Box(
                modifier = Modifier
                    .clip(corners)
                    .background(lerp(simmerStart, simmerEnd, (simmerValue + .2f).wrap(0f, 1f)))
                    .fillMaxWidth(.6f)
                    .height(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnimeSpotlightPopulatedView(
    spotlights: List<AnimeSpotlight>,
    onSpotlightClicked: (AnimeSpotlight) -> Unit
) {
    val spotlightPageCount = Int.MAX_VALUE
    val spotlightSize = spotlights.size.coerceAtLeast(1)

    val spotlightPagerState = rememberPagerState(
        initialPage = (spotlightPageCount / 2).let { half ->
            half - (half % spotlightSize)
        }
    ) {
        spotlightPageCount
    }

    val simmerValue by rememberSimmerValue()

    LaunchedEffect(key1 = spotlights) {
        while (true) {
            delay(6000)
            if (spotlightPagerState.currentPage >= spotlightPageCount) {
                spotlightPagerState.scrollToPage(0)
            } else if (spotlightPagerState.currentPage == 0) {
                spotlightPagerState.scrollToPage(spotlightPageCount - 1)
            } else {
                spotlightPagerState.animateScrollToPage(
                    spotlightPagerState.currentPage + 1
                )
            }
        }
    }

    val spotlightIndex = spotlightPagerState.currentPage % spotlightSize
    val spotlight = spotlights[spotlightIndex]
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Pager
        HorizontalPager(
            state = spotlightPagerState,
            beyondBoundsPageCount = 1,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.9f)
        ) {
            AsyncImage(
                url = spotlights[it % spotlightSize].image,
                loadColor = lerp(simmerStart, simmerEnd, simmerValue),
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    1f to aniStalkerColorScheme.background,
                                )
                            )
                        }
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxHeight(.8f)
                .align(Alignment.TopEnd)
                .padding(end = 30.dp)
        ) {
            repeat(spotlights.size) {
                val isActive = spotlightIndex == it
                val height =
                    if (isActive) 12.dp
                    else 6.dp
                val color =
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = .8f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = .2f)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                        .height(height)
                        .width(6.dp)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "#${spotlight.rank} Spotlight",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = spotlight.title.english,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BigEpisodeTail(spotlight.episodes)
                Divider(
                    modifier = Modifier.size(4.dp),
                    color = Color.White.copy(alpha = .4f)
                )
                Text(
                    text = spotlight.type.value,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )

//                Spacer(modifier = Modifier.weight(1f))

//                OutlinedButton(
//                    onClick = { onSpotlightClicked(spotlight) },
//                    modifier = Modifier
//                        .height(35.dp)
//                ) {
//                    val contentColor = LocalContentColor.current
//                    Image(
//                        painter = painterResource(R.drawable.play),
//                        contentDescription = null,
//                        colorFilter = ColorFilter.tint(contentColor),
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.size(8.dp))
//                    Text(
//                        text = "Check Out",
//                        color = contentColor,
//                        fontSize = 12.sp,
//                        textAlign = TextAlign.Center,
//                    )
//                }
            }
        }
    }

}

@Composable
fun AnimeSpotlightView(
    spotlights: List<AnimeSpotlight>,
    onSpotlightClicked: (spotlight: AnimeSpotlight) -> Unit
) {
    Box {
        AnimatedVisibility(
            visible = spotlights.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            AnimeSpotlightSimmerView()
        }
        AnimatedVisibility(
            visible = spotlights.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (spotlights.isNotEmpty()) AnimeSpotlightPopulatedView(
                spotlights = spotlights,
                onSpotlightClicked = { onSpotlightClicked(it) }
            )
        }
    }
}

@Composable
fun CurrentAnimeView(
    currentAnime: Anime?,
    lastCurrentAnimeEpisode: Int = 0,
    onStreamCurrentAnime: (() -> Unit)? = null,
    onDownloadCurrentAnime: (() -> Unit)? = null,
    onCurrentAnimeClicked: (() -> Unit)? = null,
) {
    currentAnime?.let { anime ->
        val simmerValue by rememberSimmerValue()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            CenteredBox(
                modifier = Modifier
                    .height(50.dp)
            ) {
                Text(
                    text = "Currently Watching",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                AsyncImage(
                    url = anime.image,
                    loadColor = lerp(simmerStart, simmerEnd, simmerValue),
                    modifier = Modifier
                        .border(
                            .5f.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .width(95.dp)
                        .fillMaxHeight()
                        .clickable { onCurrentAnimeClicked?.run { this() } }
                )

                Spacer(modifier = Modifier.size(10.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = anime.title.english,
                        fontWeight = FontWeight.Bold,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = anime.type.value,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Divider(
                                modifier = Modifier.size(4.dp),
                                color = Color.White.copy(alpha = .4f)
                            )
                            Text(
                                text = anime.status.value,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min)
                        ) {
                            BigEpisodeTail(episodes = anime.episodes)
                            Spacer(modifier = Modifier.size(10.dp))
                            CenteredBox(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .fillMaxHeight()
                                    .padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = lastCurrentAnimeEpisode.toString(),
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StreamButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(35.dp)
                        ) {
                            onStreamCurrentAnime?.run { this() }
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        DownloadButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(35.dp)
                        ) {
                            onDownloadCurrentAnime?.run { this() }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun AnimeCategoryTabView(
    animeCategory: List<AnimeCategory>,
    selectedTab: Int = 0,
    onCategorySelected: (item: Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = aniStalkerColorScheme.background,
        edgePadding = 20.dp,
        indicator = {
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(it[selectedTab])
            )
        },
        modifier = Modifier
            .background(aniStalkerColorScheme.background)
            .statusBarsPadding()
    ) {
        repeat(animeCategory.size) { index ->
            Tab(
                selected = selectedTab == index,
                onClick = { onCategorySelected(index) },
                modifier = Modifier
                    .height(60.dp)
            ) {
                Text(
                    text = animeCategory[index].label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

// Previewies
/*
@Preview
@Composable
private fun P_AnimeSpotlightView() {
    AniStalkerTheme {
        Surface(
            color = aniStalkerColorScheme.background
        ) {
            AnimeSpotlightView(
                spotlights = listOf(
                    AnimeSpotlight(episodes = AnimeEpisode(total = 12)),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                    AnimeSpotlight(),
                )
            ) {

            }
        }
    }
}

@Preview
@Composable
private fun P_CurrentAnimeView() {
    AniStalkerTheme {
        Surface(
            color = aniStalkerColorScheme.background
        ) {
            CurrentAnimeView(currentAnime = Anime()) {
            }
        }
    }
}

@Preview(device = "id:pixel_3a", wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
private fun P_AnimeCategoryView() {
    AniStalkerTheme(dynamicColor = true) {
        Surface(
            color = aniStalkerColorScheme.background
        ) {
            AnimeCategoryView(
                animeCategory = listOf(
                    AnimeCategory.RECENTLY_UPDATED to 20,
                    AnimeCategory.TOP_AIRING to 30,
                    AnimeCategory.COMPLETED to 40,
                    AnimeCategory.MOST_FAVORITE to 20,
                    AnimeCategory.MOST_POPULAR to 10
                ),
                onLoadNextCategoryPage = { },
                onAnimeCardClicked = { }
            ) { category, _ ->
                if (category == AnimeCategory.TOP_AIRING) null
                else AnimeCard()
            }
        }
    }
}
*/
@Preview(wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
@Composable
private fun P_HomeScreen() {
    AniStalkerTheme(dynamicColor = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = aniStalkerColorScheme.background,
            contentColor = Color.White
        ) {
            HomeScreen(
                listOf(
                    AnimeSpotlight(image = "", episodes = AnimeEpisode(total = 12)),
                    AnimeSpotlight(image = ""),
                    AnimeSpotlight(image = ""),
                    AnimeSpotlight(image = ""),
                    AnimeSpotlight(image = ""),
                ),
                animeList = listOf(
                    AnimeCard(),
                ),
                animeCategories = listOf(
                    AnimeCategory.RECENTLY_UPDATED,
                    AnimeCategory.TOP_AIRING,
                    AnimeCategory.COMPLETED,
                    AnimeCategory.MOST_FAVORITE,
                    AnimeCategory.MOST_POPULAR
                ),
                currentAnime = Anime(episodes = AnimeEpisode(total = 1023)),
            ) {
            }
        }
    }
}