package com.redline.anistalker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource

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
fun awarePainterResource(resId: Int, color: Color = Color(0xFF151515)): Painter {
    return if (LocalInspectionMode.current) ColorPainter(color)
    else painterResource(id = resId)
}

private fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t
