package com.cybersec.liquidstream.core.glass.texture

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import com.cybersec.liquidstream.core.glass.GlassQualityTier
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import kotlin.random.Random

/**
 * 2026 Micro-Frosted Optical Glass Grain Texture.
 *
 * Emulates the micro-texture of sand-blasted optical glass (visionOS style).
 * Uses drawWithCache to precompute grain coordinates once per layout size,
 * ensuring 0 allocations during redraws and smooth 60/120 FPS performance.
 */
fun Modifier.microFrostedGrain(
    state: LiquidGlassState,
    densityFactor: Float = 0.0003f,
    grainAlpha: Float = 0.022f
): Modifier = composed {
    val tier = state.effectiveTier
    if (tier == GlassQualityTier.PERFORMANCE_FALLBACK) return@composed this

    this.drawWithCache {
        val count = (size.width * size.height * densityFactor).toInt().coerceIn(40, 240)
        // Deterministic pseudo-random seed based on size so it doesn't flicker
        val random = Random(size.width.toInt() xor size.height.toInt())
        val points = ArrayList<Offset>(count)
        for (i in 0 until count) {
            points.add(
                Offset(
                    x = random.nextFloat() * size.width,
                    y = random.nextFloat() * size.height
                )
            )
        }

        val grainColor = Color.White.copy(alpha = grainAlpha * (if (tier == GlassQualityTier.ULTRA_LIQUID) 1.2f else 0.8f))

        onDrawWithContent {
            drawContent()
            drawPoints(
                points = points,
                pointMode = PointMode.Points,
                color = grainColor,
                strokeWidth = 1.2f
            )
        }
    }
}
