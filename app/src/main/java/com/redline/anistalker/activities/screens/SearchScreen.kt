@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)

package com.redline.anistalker.activities.screens

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redline.anistalker.R
import com.redline.anistalker.models.AnimeCard
import com.redline.anistalker.models.AnimeScore
import com.redline.anistalker.models.AnimeSearchFilter
import com.redline.anistalker.models.AnimeSort
import com.redline.anistalker.models.AnimeTrack
import com.redline.anistalker.models.AnimeType
import com.redline.anistalker.ui.components.AnimeCardView
import com.redline.anistalker.ui.components.DropDownMenu
import com.redline.anistalker.ui.components.ErrorSnackBar
import com.redline.anistalker.ui.components.ExpandableBlock
import com.redline.anistalker.ui.components.rememberSimmerValue
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.secondary_background
import com.redline.anistalker.utils.toTitleCase
import com.redline.anistalker.utils.wrap

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    animeContent: List<AnimeCard?>,
    loadingError: String? = null,
    onLoadNextPage: () -> Unit,
    onAnimeClicked: (Int) -> Unit,
    onSearch: (query: String, animeFilter: AnimeSearchFilter) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable(saver = filterSaver) { mutableStateOf(AnimeSearchFilter()) }
    val simmerValue by rememberSimmerValue()

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
//        item {
//            Text(
//                text = "Search",
//                fontWeight = FontWeight.Bold,
//                fontSize = 18.sp,
//                color = Color.White,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp, vertical = 20.dp)
//            )
//        }

        stickyHeader {
            SearchBarView(
                query = searchQuery,
                onQueryChanged = { searchQuery = it }
            ) {
                onSearch(searchQuery, filter)
            }
        }

        item {
            AnimeFilterView(filter = filter) {
                filter = it
                onSearch(searchQuery, filter)
            }
        }

        if (animeContent.isEmpty() && loadingError == null) onLoadNextPage()

        itemsIndexed(
            items = animeContent,
//            key = { i, item ->  },
        ) { index, anime ->
            if (index > animeContent.size - 5 && loadingError == null)
                onLoadNextPage()

            AnimeCardView(
                animeCard = anime,
                showOwner = false,
                simmerValue = (simmerValue + .2f * index).wrap(0f, 1f)
            ) {
                anime?.run { onAnimeClicked(id) }
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
private fun SearchBarView(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
) {
    val bg = aniStalkerColorScheme.background
    val colors = SearchBarDefaults.colors(
        containerColor = secondary_background
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(aniStalkerColorScheme.background)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChanged,
            onSearch = {
                onSearch(query)
            },
            placeholder = {
                Text(text = "Search Anime or Manga")
            },
            leadingIcon = {
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
            },
            trailingIcon = {
                if (query.isNotEmpty()) Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable { onQueryChanged("") }
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
            },
            active = false,
            onActiveChange = { },
            colors = colors,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.height(50.dp)
        ) {
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
            .padding(horizontal = 30.dp)
            .padding(bottom = 20.dp)
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
                            text = "Score",
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
                            text = "Score",
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
                            text = "Score",
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
private fun AnimeContentPage(
    animeContent: List<AnimeCard?>,
    animeFilter: AnimeSearchFilter,
    onFilterChanged: (AnimeSearchFilter) -> Unit,
    onLoadNextPage: () -> Unit,
    onAnimeClicked: (Int) -> Unit,
) {
    val simmerValue by rememberSimmerValue()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (animeContent.isEmpty())
            onLoadNextPage()

        item(key = 0) {
            AnimeFilterView(
                filter = animeFilter,
                onFilterChanged = onFilterChanged
            )
        }

        items(
            count = animeContent.size,
//            key = { animeContent[it]?.id ?: -(it + 1) },
            contentType = { AnimeCard::class.java }
        ) {
            if (it >= animeContent.size - 4)
                onLoadNextPage()

            val anime = animeContent[it]
            AnimeCardView(
                animeCard = anime,
                showOwner = false,
                simmerValue = (simmerValue + .2f * it).wrap(0f, 1f)
            ) {
                anime?.run { onAnimeClicked(id) }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun P_SearchScreen() {
    AniStalkerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = aniStalkerColorScheme.background,
            contentColor = Color.White,
        ) {
            SearchScreen(
                animeContent = listOf(
                    AnimeCard(),
                    AnimeCard(),
                    AnimeCard(),
                ),
                onLoadNextPage = { },
                onAnimeClicked = { },
                loadingError = "null",
            ) { _, _ ->
            }
        }
    }
}