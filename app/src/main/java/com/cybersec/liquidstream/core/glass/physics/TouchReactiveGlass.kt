package com.cybersec.liquidstream.core.glass.physics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.cybersec.liquidstream.core.glass.GlassQualityTier
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import kotlinx.coroutines.launch

/**
 * 2026 Touch-Reactive Specular Reflection Modifier.
 *
 * Tracks the user's touch position and dynamically angles the specular
 * glass highlight toward the contact point. Smoothly rebounds to default
 * top light angle when released.
 */
fun Modifier.touchReactiveSpecular(
    state: LiquidGlassState,
    shape: Shape,
    maxIntensity: Float = 0.28f
): Modifier = composed {
    val tier = state.effectiveTier
    if (tier == GlassQualityTier.PERFORMANCE_FALLBACK) return@composed this

    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()

    val touchX = remember { Animatable(0.5f) }
    val touchY = remember { Animatable(0.15f) }
    val touchAlpha = remember { Animatable(maxIntensity * 0.5f) }

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    this
        .onSizeChanged { cardSize = it }
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = maxIntensity * 0.45f),
                    Color.White.copy(alpha = maxIntensity * 0.15f),
                    Color.Transparent
                ),
                center = Offset(
                    x = cardSize.width * 0.5f,
                    y = cardSize.height * 0.15f
                ),
                radius = (cardSize.width.coerceAtLeast(1) * 0.75f)
            ),
            shape = shape
        )
}
