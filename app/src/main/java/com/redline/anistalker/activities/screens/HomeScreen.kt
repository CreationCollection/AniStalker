@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.redline.anistalker.activities.screens

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.managements.helper.Net
import com.redline.anistalker.models.AniError
import com.redline.anistalker.models.Anime
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeCategory
import com.redline.anistalker.models.AnimeScore
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.AnimeSort
import com.redline.anistalker.models.AnimeSpotlight
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.AnimeType
import com.redline.anistalker.ui.components.AnimeCardView
import com.redline.anistalker.ui.components.AsyncImage
import com.redline.anistalker.ui.components.BigEpisodeTail
import com.redline.anistalker.ui.components.CenteredBox
import com.redline.anistalker.ui.components.DropDownMenu
import com.redline.anistalker.ui.components.ExpandableBlock
import com.redline.anistalker.ui.components.rememberSimmerValue
import com.redline.anistalker.ui.components.simmerEnd
import com.redline.anistalker.ui.components.simmerStart
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.dark_background
import com.redline.anistalker.ui.theme.secondary_background
import com.redline.anistalker.utils.blurImage
import com.redline.anistalker.utils.toTitleCase
import com.redline.anistalker.utils.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

private val animeSortList = AnimeSort.values().map { it.value.toTitleCase() }
private val animeScoreList = AnimeScore.values().map { it.value.toTitleCase() }
private val animeTypeList = AnimeType.values().map { it.value.toTitleCase() }
private val animeTrackList = AnimeTrack.values().map { it.value }

private val filterSaver = Saver<MutableState<AnimeSearchFilter>, Bundle>(
    save = {
        Bundle().apply {
            putString("sort", it.value.sort.name)
            putString("score", it.value.score.name)
            putString("type", it.value.type.name)
            putString("track", it.value.track.name)
        }
    },
    restore = {
        mutableStateOf(
            AnimeSearchFilter(
                AnimeSort.valueOf(it.getString("sort", "DEFAULT")),
                AnimeScore.valueOf(it.getString("score", "ALL")),
                AnimeType.valueOf(it.getString("type", "ALL")),
                AnimeTrack.valueOf(it.getString("track", "ALL")),
            )
        )
    }
)

private const val simmerOffset = .2f

@Composable
fun HomeScreen(
    currentAnime: Anime?,
    lastCurrentAnimeEpisode: Int,

    suggestions: List<AnimeSpotlight?>,
    recentAnime: List<AnimeCard>,

    categories: List<AnimeCategory>,
    categoryItems: List<AnimeCard?>,
    onCategoryChange: (Int) -> Unit,
    onLoadNextPage: () -> Unit,

    onAnimeClick: (Int) -> Unit,

    searchResult: () -> StateFlow<List<AnimeCard?>>,
    onLoadSearchResult: () -> Unit,
    onSearch: (String, AnimeSearchFilter) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchFilter by rememberSaveable(saver = filterSaver) { mutableStateOf(AnimeSearchFilter()) }

    val scope = rememberCoroutineScope()
    val filterTabState = rememberLazyListState()
    val pagerState = rememberPagerState(initialPage = 2) { 3 }

    var selectedCategory by rememberSaveable { mutableIntStateOf(0) }

    val changePage = remember {
        { page: Int ->
            scope.launch { pagerState.animateScrollToPage(page) }
            scope.launch { filterTabState.animateScrollToItem(page) }
            Unit
        }
    }

    Column {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(4.dp)
                .background(aniStalkerColorScheme.background)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                SearchBarView(
                    query = searchQuery,
                    onQueryChanged = {
                        searchQuery = it
                    },
                    onSearch = {
                        if (pagerState.currentPage != 0 && it.isNotBlank()) changePage(0)
                        onSearch(it, searchFilter)
                    }
                )
            }
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                divider = { },
                indicator = {
                    TabRowDefaults.Indicator(
                        height = 1.dp,
                        modifier = Modifier.tabIndicatorOffset(it[pagerState.currentPage])
                    )
                },
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                modifier = Modifier
                    .wrapContentWidth()
            ) {
                repeat(3) { item ->
                    val selected = item == pagerState.currentPage
                    val color =
                        if (selected) MaterialTheme.colorScheme.primary
                        else Color.White

                    val title = when (item) {
                        0 -> "Search"
                        1 -> "Suggestions"
                        2 -> "Browse"
                        else -> "Tab"
                    }

                    Tab(
                        selected = item == pagerState.currentPage,
                        onClick = {
                            changePage(item)
                        },
                        modifier = Modifier
                            .height(50.dp)
                    ) {
                        Text(
                            text = title,
                            color = color,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        )
                    }
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
        ) { page ->
            when (page) {
                0 -> {
                    SearchTab(
                        searchFilter = searchFilter,
                        onAnimeClick = onAnimeClick,
                        onAnimeImageClick = { },
                        searchResult = searchResult,
                        onLoadSearchResult = onLoadSearchResult,
                    ) {
                        searchFilter = it
                        onSearch(searchQuery, it)
                    }
                }

                1 -> {
                    SuggestionTab(
                        suggestions = suggestions,
                        currentAnime = currentAnime,
                        lastCurrentAnimeEpisode = lastCurrentAnimeEpisode,
                        recentAnime = recentAnime,
                        onAnimeImageClick = { },
                        onAnimeClick = onAnimeClick,
                        scope = scope,
                    )
                }

                else -> {
                    BrowsingTab(
                        categories = categories,
                        selected = selectedCategory,
                        items = categoryItems,
                        onLoadNextPage = onLoadNextPage,
                        onAnimeClick = onAnimeClick
                    ) {
                        selectedCategory = it
                        onCategoryChange(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTab(
    searchFilter: AnimeSearchFilter,
    onAnimeClick: (Int) -> Unit,
    onAnimeImageClick: (String) -> Unit,
    searchResult: () -> StateFlow<List<AnimeCard?>>,
    onLoadSearchResult: () -> Unit,
    onFilterChange: (AnimeSearchFilter) -> Unit,
) {
    val simmerValue by rememberSimmerValue()
    val animeList by searchResult().collectAsState()
    val listState = rememberLazyListState()

    val loadMore by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull {
                it.index == listState.layoutInfo.totalItemsCount - 10
            } != null
        }
    }

    if (loadMore) {
        onLoadSearchResult()
    }

    LazyColumn(state = listState) {
        item {
            AnimeFilterView(
                filter = searchFilter,
                onFilterChanged = {
                    onFilterChange(it)
                }
            )
        }

        itemsIndexed(
            items = animeList
        ) { i, card ->
            AnimeCardView(
                animeCard = card,
                showOwner = false,
                simmerValue = (simmerValue * simmerOffset * i).wrap(0f, 1f),
                onImageClick = { onAnimeImageClick(it.image) }
            ) {
                onAnimeClick(it.id)
            }
        }
    }
}

@Composable
private fun SuggestionTab(
    suggestions: List<AnimeSpotlight?>,
    currentAnime: Anime?,
    lastCurrentAnimeEpisode: Int,
    recentAnime: List<AnimeCard>,

    scope: CoroutineScope,

    onAnimeImageClick: (String) -> Unit,
    onAnimeClick: (Int) -> Unit,
) {
    val simmerValue by rememberSimmerValue()

    LazyColumn {
        item {
            LazyRow(
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = suggestions
                ) { i, card ->
                    AnimeSuggestionView(
                        value = card,
                        simmer = (simmerValue * simmerOffset * i).wrap(0f, 1f),
                        scope = scope,
                    ) { id ->
                        onAnimeClick(id)
                    }
                }
            }
        }

        item {
            CurrentAnimeView(
                currentAnime = currentAnime,
                lastCurrentAnimeEpisode = lastCurrentAnimeEpisode,
                onImageClick = onAnimeImageClick,
                onClick = onAnimeClick
            )
        }

        if (recentAnime.isNotEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Continue Watching",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            itemsIndexed(
                items = recentAnime
            ) { i, card ->
                AnimeCardView(
                    animeCard = card,
                    simmerValue = (simmerValue * simmerOffset * i).wrap(0f, 1f),
                    showOwner = false,
                    onImageClick = { onAnimeImageClick(card.image) }
                ) {
                    onAnimeClick(it.id)
                }
            }
        } else {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Text(
                        text = "Recent Anime will be shown here.",
                        color = Color.White.copy(alpha = .4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowsingTab(
    categories: List<AnimeCategory>,
    selected: Int,
    items: List<AnimeCard?>,
    onLoadNextPage: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onCategoryChange: (Int) -> Unit,
) {
    val simmerValue by rememberSimmerValue()

    val categoryListState = rememberLazyListState()
    val listState = rememberLazyListState()
    val loadMore by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull {
                it.index == listState.layoutInfo.totalItemsCount - 10
            } != null
        }
    }

    if (loadMore) {
        onLoadNextPage()
    }

    LazyColumn(
        state = listState
    ) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth(),
                state = categoryListState,
            ) {
                itemsIndexed(
                    items = categories
                ) { index, category ->
                    FilterChip(
                        selected = selected == index,
                        onClick = {
                            if (selected != index) onCategoryChange(index)
                        },
                        label = {
                            Text(
                                text = category.label,
                                color = LocalContentColor.current,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = secondary_background
                        ),
                        modifier = Modifier
                            .height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }

            LaunchedEffect(selected) {
                categoryListState.animateScrollToItem(selected)
            }
        }

        itemsIndexed(
            items = items
        ) { i, anime ->
            AnimeCardView(
                animeCard = anime,
                showOwner = false,
                simmerValue = (simmerValue * simmerOffset * i).wrap(0f, 1f)
            ) {
                onAnimeClick(it.id)
            }
        }
    }
}

@Composable
private fun SearchBarView(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
) {
    LaunchedEffect(query) {
        delay(500)
        onSearch(query)
    }

    Row(
        modifier = Modifier
            .background(secondary_background, RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(60.dp)
                .height(50.dp)
        ) {
            val color = LocalContentColor.current
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color),
                modifier = Modifier
                    .size(20.dp)
            )
        }

        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ){
            if (query.isEmpty()) {
                Text(
                    text = "Search Anime here",
                    color = Color.White.copy(alpha = .5f),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                singleLine = true,
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                it()
            }
        }

        if (query.isNotEmpty()) Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable {
                    onQueryChanged("")
                }
                .width(60.dp)
                .height(50.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.close),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
            )
        }
    }
}

@Composable
private fun AnimeFilterView(
    filter: AnimeSearchFilter,
    onFilterChanged: (AnimeSearchFilter) -> Unit
) {
    var isFilterExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 20.dp)
    ) {
        ExpandableBlock(
            label = "Filters",
            expand = isFilterExpanded,
            height = 40.dp,
            onClick = { isFilterExpanded = !isFilterExpanded },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Score",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = .6f),
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                        )
                        DropDownMenu(
                            label = filter.score.value.toTitleCase(),
                            valueList = animeScoreList,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            onFilterChanged(filter.copy(score = AnimeScore.values()[it]))
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Sort",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = .6f),
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                        )
                        DropDownMenu(
                            label = filter.sort.value.toTitleCase(),
                            valueList = animeSortList,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            onFilterChanged(filter.copy(sort = AnimeSort.values()[it]))
                        }
                    }

                }
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Anime Type",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = .6f),
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                        )
                        DropDownMenu(
                            label = filter.type.value.toTitleCase(),
                            valueList = animeTypeList,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            onFilterChanged(filter.copy(type = AnimeType.values()[it]))
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Anime Track",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = .6f),
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                        )
                        DropDownMenu(
                            label = filter.track.value,
                            valueList = animeTrackList,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            onFilterChanged(filter.copy(track = AnimeTrack.values()[it]))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeSuggestionView(
    value: AnimeSpotlight?,
    simmer: Float = 0f,
    scope: CoroutineScope,
    onClick: (Int) -> Unit,
) {
    val color = lerp(secondary_background, dark_background, simmer)
    val shape = RoundedCornerShape(10.dp)
    val context = LocalContext.current

    var image by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(value) {
        scope.launch(Dispatchers.IO) {
            if (value != null) do try {
                BitmapFactory.decodeStream(Net.getStream(value.image)).run {
                    context.blurImage(this).let { image = it.asImageBitmap() }
                }
            } catch (err: AniError) {
                err.printStackTrace()
            } catch (err: IOException) {
                err.printStackTrace(); break
            }
            while (image == null)
        }
    }

    Row(
        modifier = Modifier
            .heightIn(max = 160.dp)
    ) {
        Crossfade(
            targetState = value,
            label = "Title",
            modifier = Modifier
        ) {
            if (it != null) {
                val rankString = String.format("%02d", it.rank)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f, true)
                    ) {
                        Text(
                            text = it.title.english,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .rotate(-90f)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = rankString,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(color, shape)
                        .fillMaxHeight()
                        .width(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        Crossfade(targetState = image, label = "Image") {
            if (it != null) {
                Image(
                    painter = BitmapPainter(it),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier
                        .clip(shape)
                        .clickable {
                            if (value != null) onClick(value.id)
                        }
                        .fillMaxHeight()
                        .aspectRatio(.7f, true)
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(color, shape)
                        .fillMaxHeight()
                        .aspectRatio(.7f, true)
                )
            }
        }
    }
}

@Composable
fun CurrentAnimeView(
    currentAnime: Anime?,
    lastCurrentAnimeEpisode: Int = 0,
    onImageClick: (String) -> Unit,
    onClick: (Int) -> Unit,
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
                        .fillMaxHeight()
                        .aspectRatio(.7f, true)
                        .clickable { onImageClick(currentAnime.image) }
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
                    OutlinedButton(
                        onClick = { onClick(currentAnime.id.zoroId) },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                    ) {
                        Text(
                            text = "Watch",
                            color = LocalContentColor.current
                        )
                    }
                }
            }
        }
    }

}

@Preview
@Composable
private fun P_HomeScreen() {
    AniStalkerTheme {
        Surface(
            color = aniStalkerColorScheme.background,
            contentColor = Color.White
        ) {

        }
    }
}