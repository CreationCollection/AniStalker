package com.redline.anistalker.ui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.redline.anistalker.ui.theme.mid_background
import com.redline.anistalker.ui.theme.secondary_background

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
fun awarePainterResource(resId: Int): Painter {
    return painterResource(id = resId)
}

fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t

val simmerStart = secondary_background
val simmerEnd = mid_background

@Composable
fun rememberSimmerValue(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite simmer")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            tween(durationMillis = 3000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Simmer Color"
    )
}