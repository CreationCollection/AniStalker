package com.redline.anistalker.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.redline.anistalker.R
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.aniStalkerColorScheme
import com.redline.anistalker.ui.theme.md_theme_dark_background
import com.redline.anistalker.ui.theme.md_theme_dark_outline
import com.redline.anistalker.ui.theme.md_theme_dark_primary
import com.redline.anistalker.ui.theme.secondary_background
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

data class NavItem(val unselectedIcon: Int, val selectedIcon: Int, val label: String)

@Composable
fun AniNavBar(
    items: Array<NavItem>,
    selectedItem: Int = 0,
    onSelected: (selected: Int) -> Unit
) {
    val bg = secondary_background
    val chipColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
    val chipFg = MaterialTheme.colorScheme.primary

    val shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)

    Box(
        modifier = Modifier
            .border(.5.dp, Color.White.copy(alpha = .5f), shape)
            .clip(shape)
            .shadow(2.dp, shape = shape, ambientColor = aniStalkerColorScheme.background)
            .fillMaxWidth()
            .background(bg)
            .navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 20.dp)
        ) {
            repeat(items.size) { i ->
                val item = items[i]
                val active = selectedItem == i

                val fraction by animateFloatAsState(
                    targetValue = if (active) 1f else 0f,
                    label = "fraction"
                )

                val bgColor = lerp(Color.Transparent, chipColor, fraction)
                val fgColor = lerp(Color.White, chipFg, fraction)
                val imgSize = lerp(25.dp, 20.dp, fraction)

                val icon = awarePainterResource(
                    resId = if (active) item.selectedIcon else item.unselectedIcon
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(40.dp))
                        .background(bgColor)
                        .clickable {
                            onSelected(i)
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Image(
                        painter = icon,
                        contentDescription = item.label,
                        colorFilter = ColorFilter.tint(fgColor),
                        modifier = Modifier
                            .size(imgSize)
                    )
                    AnimatedVisibility(visible = active) {
                        Text(
                            text = item.label,
                            color = fgColor,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(start = 15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DropDownMenu(
    label: String,
    valueList: List<String>,
    modifier: Modifier = Modifier,
    title: String = label,
    onSelected: (selected: Int) -> Unit,
) {
    var showMenu by rememberSaveable {
        mutableStateOf(false)
    }

    val bg = secondary_background
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { showMenu = true }
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 15.dp)
            .height(40.dp)
            .then(modifier)
    ) {
        Text(
            text = label,
            color = Color.White
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    if (showMenu) {
        val dbg =
            if (LocalInspectionMode.current) Color(0xFF151515)
            else md_theme_dark_background
        Dialog(
            onDismissRequest = { showMenu = false },
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(dbg)
            ) {
                CenteredBox(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .height(60.dp)
                ) {
                    Text(
                        text = title,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider()
                LazyColumn(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(dbg)
                        .padding(vertical = 10.dp)
                ) {
                    items(valueList.size) {
                        CenteredBox(
                            modifier = Modifier
                                .clickable {
                                    showMenu = false
                                    onSelected(it)
                                }
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = valueList[it],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ExpandableBlock(
    modifier: Modifier = Modifier,
    label: String,
    expand: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)?
) {
    val outline =
        if (LocalInspectionMode.current) Color(0xFF9D8E81)
        else md_theme_dark_outline

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, outline, RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .wrapContentHeight()
            .then(modifier)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onClick?.let { it() } }
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        if (expand) {
            Divider(color = outline, thickness = 1.dp)
        }
        AnimatedVisibility(visible = expand) {
            content?.let { it() }
        }
    }
}

@Composable
fun ToggleSwitch(
    modifier: Modifier = Modifier,
    selected: Int = 0,
    selectedBackground: Color = Color(0xFF6A3C00),
    onSelected: ((selected: VideoQuality) -> Unit)? = null,
    icons: @Composable (Int) -> Int
) {
    val bg = secondary_background

    val outline = MaterialTheme.colorScheme.outline

    val _1 = selected == 0
    val _2 = !_1

    val fraction by animateFloatAsState(
        targetValue = if (_1) 1f else 0f,
        label = "fraction"
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .wrapContentWidth()
            .height(50.dp)
            .then(modifier)
    ) {
        CenteredBox(
            modifier = Modifier
                .background(lerp(bg, selectedBackground, fraction))
                .fillMaxHeight()
                .padding(horizontal = 25.dp)
                .clickable { onSelected?.let { it(VideoQuality.HD) } }
        ) {
            Image(
                painter = awarePainterResource(icons(0)),
                contentDescription = "HD",
                modifier = Modifier
                    .size(30.dp),
                colorFilter = ColorFilter.tint(Color(0xFFFFDCBE))
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = outline
        )
        CenteredBox(
            modifier = Modifier
                .background(lerp(selectedBackground, bg, fraction))
                .fillMaxHeight()
                .padding(horizontal = 25.dp)
                .clickable { onSelected?.let { it(VideoQuality.UHD) } }
        ) {
            Image(
                painter = awarePainterResource(icons(1)),
                contentDescription = "UHD",
                modifier = Modifier
                    .size(30.dp),
                colorFilter = ColorFilter.tint(Color(0xFFFFDCBE))
            )
        }
    }
}

@Composable
fun AsyncImage(
    url: String?,
    modifier: Modifier = Modifier,
    overlayBrush: Brush? = null,
    loadColor: Color = Color(0xFF1E1E1E),
    contentDescription: String? = null,
) {
    var bitmapPainter by remember {
        mutableStateOf<BitmapPainter?>(null)
    }

    LaunchedEffect(key1 = url) {
        val bitmap: Bitmap? = withContext(Dispatchers.IO) {
            null
        }
        bitmapPainter = bitmap?.let { BitmapPainter(it.asImageBitmap()) }
    }

    Box(
        modifier = Modifier
            .then(modifier)
            .background(loadColor)
            .also {
                overlayBrush?.let { brush ->
                    it.drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush)
                        }
                    }
                }
            }
    ) {
        AnimatedVisibility(
            visible = bitmapPainter != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.matchParentSize()
        ) {
            if (bitmapPainter != null) Image(
                painter = bitmapPainter!!,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

// endregion

// region Previews
@SuppressLint("UnrememberedMutableState")
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun NavBarPreview() {
    var selectedItem by remember { mutableStateOf(0) }
    AniStalkerTheme {
        AniNavBar(
            items = arrayOf(
                NavItem(R.drawable.home, R.drawable.home, "Home"),
                NavItem(R.drawable.search, R.drawable.search, "Search"),
                NavItem(R.drawable.library, R.drawable.library, "Library"),
                NavItem(R.drawable.downloads, R.drawable.downloads, "Media"),
            ), selectedItem = 0
        ) {
            selectedItem = it
        }
    }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DropDownMenuPreview() {
    AniStalkerTheme {
        DropDownMenu(
            label = "Test Menu",
            valueList = listOf(
                "Bad", "Normal", "Good", "Great"
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {

        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun ExpandableBlockPreview() {
    var expand by remember { mutableStateOf(true) }
    AniStalkerTheme {
        ExpandableBlock(
            label = "Test Block",
            expand = expand,
            onClick = { expand = !expand }
        ) {
            Text(
                text = "Test",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun VideoQualitySwitchPreview() {
    AniStalkerTheme {
        ToggleSwitch { 0 }
    }
}

// endregion
