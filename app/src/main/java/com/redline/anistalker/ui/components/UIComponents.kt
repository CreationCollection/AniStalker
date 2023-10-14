package com.redline.anistalker.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.Dialog
import com.redline.anistalker.R
import com.redline.anistalker.models.VideoQuality
import com.redline.anistalker.ui.theme.AniStalkerTheme
import com.redline.anistalker.ui.theme.md_theme_dark_background
import com.redline.anistalker.ui.theme.md_theme_dark_outline
import com.redline.anistalker.ui.theme.md_theme_dark_primary
import com.redline.anistalker.ui.theme.secondary_background
import kotlin.math.min

data class NavItem(val unselectedIcon: Int, val selectedIcon: Int, val label: String)

@Composable
fun AniNavBar(
    items: Array<NavItem>,
    selectedItem: Int = 0,
    onSelected: (selected: Int) -> Unit
) {
    val bg =
        if (LocalInspectionMode.current) Color(0xFF1E1E1E)
        else secondary_background

    val chipColor =
        if (LocalInspectionMode.current) Color(0x0CFFB871)
        else md_theme_dark_primary.copy(alpha = 12F)

    val chipFg =
        if (LocalInspectionMode.current) Color(0xFFFFB871)
        else md_theme_dark_primary

    val duration = 600

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(bg)
            .navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 30.dp)
        ) {
            for (i in 0 until min(5, items.size)) {
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
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clickable {
                            onSelected(i)
                        }
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
                            fontWeight = FontWeight.Bold,
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
    valueList: Array<String>,
    modifier: Modifier = Modifier,
    title: String = label,
    showMenu: Boolean = false,
    onDismissed: () -> Unit,
    onSelected: (selected: Int) -> Unit,
) {

    val bg =
        if (LocalInspectionMode.current) Color(0xFF1E1E1E)
        else secondary_background
    val primaryColor =
        if (LocalInspectionMode.current) Color(0xFFFFB871)
        else md_theme_dark_primary

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 15.dp)
            .then(modifier)
            .height(50.dp)
    ) {
        Text(
            text = label,
            color = Color.White
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "",
            tint = Color.White
        )
    }

    if (showMenu) {
        val dbg =
            if (LocalInspectionMode.current) Color(0xFF151515)
            else md_theme_dark_background
        Dialog(
            onDismissRequest = { onDismissed() },
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
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable {
                                    onSelected(it)
                                }
                        ) {
                            Text(
                                text = valueList[it],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                color = Color.White,
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
            .fillMaxWidth()
            .wrapContentHeight()
            .then(modifier)
            .border(1.dp, outline, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clickable { onClick?.let { it() } }
        ) {
            Text(
                text = label,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "",
                tint = Color.White
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
    val bg =
        if (LocalInspectionMode.current) Color(0xFF1E1E1E)
        else secondary_background

    val outline =
        if (LocalInspectionMode.current) Color(0xFF9D8E81)
        else md_theme_dark_outline
    
    val _1 = selected == 0
    val _2 = !_1

    val fraction by animateFloatAsState(
        targetValue = if (_1) 1f else 0f,
        label = "fraction"
    )

    Row (
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .wrapContentWidth()
            .height(50.dp)
            .then(modifier)
    ) {
        CenteredBox (
            modifier = Modifier
                .background(lerp(bg, selectedBackground, fraction))
                .fillMaxHeight()
                .padding(horizontal = 25.dp)
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
        CenteredBox (
            modifier = Modifier
                .background(lerp(selectedBackground, bg, fraction))
                .fillMaxHeight()
                .padding(horizontal = 25.dp)
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
fun CenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        content?.let { it() }
    }
}


// region Helper Functions
@Composable
private fun awarePainterResource(resId: Int): Painter {
    return if (LocalInspectionMode.current) ColorPainter(Color(0xFF151515))
    else painterResource(id = resId)
}

private fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t

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
            valueList = arrayOf(
                "Bad", "Normal", "Good", "Great"
            ),
            showMenu = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onDismissed = {

            }
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
