package com.cybersec.liquidstream.core.glass.prism

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cybersec.liquidstream.core.glass.GlassQualityTier
import com.cybersec.liquidstream.core.glass.LiquidGlassState

/**
 * 2026 Optical Glass Prism Chromatic Dispersion Engine.
 *
 * Simulates physical light splitting at curved glass boundaries:
 * - Cyan refraction on leading edge (480nm wavelength)
 * - Pure specular white core reflection (direct illumination)
 * - Magenta/Violet inner dispersion (400nm wavelength)
 * - Amber-Gold caustics on trailing edge (580nm wavelength)
 */
object PrismDispersionBrush {

    /**
     * Optical chromatic prism border brush with angle calculation.
     */
    fun chromaticBorderBrush(
        intensity: Float = 1.0f,
        angleDeg: Float = 45f,
        primaryAccent: Color = Color(0xFFA855F7)
    ): Brush {
        val i = intensity.coerceIn(0.2f, 1.5f)
        val rad = Math.toRadians(angleDeg.toDouble())
        val startX = (100f * Math.cos(rad)).toFloat()
        val startY = (100f * Math.sin(rad)).toFloat()

        return Brush.linearGradient(
            0.00f to Color.White.copy(alpha = (0.75f * i).coerceAtMost(0.95f)),
            0.15f to Color(0xFF4DF0FF).copy(alpha = (0.45f * i).coerceAtMost(0.80f)), // Optical Cyan
            0.35f to Color.White.copy(alpha = (0.25f * i).coerceAtMost(0.50f)),
            0.55f to Color(0xFFFF59B3).copy(alpha = (0.40f * i).coerceAtMost(0.75f)), // Optical Magenta
            0.75f to primaryAccent.copy(alpha = (0.35f * i).coerceAtMost(0.70f)),     // Accent bleed
            0.90f to Color(0xFFFFD466).copy(alpha = (0.35f * i).coerceAtMost(0.65f)), // Optical Gold
            1.00f to Color.White.copy(alpha = (0.15f * i).coerceAtMost(0.40f)),
            start = Offset(0f, 0f),
            end = Offset(startX, startY)
        )
    }

    /**
     * Subtle horizontal prism refraction sweep for top rims of cards.
     */
    fun horizontalPrismSheen(intensity: Float = 1.0f): Brush {
        val i = intensity.coerceIn(0.1f, 1.0f)
        return Brush.horizontalGradient(
            0.0f to Color.Transparent,
            0.15f to Color(0xFF4DF0FF).copy(alpha = 0.22f * i),
            0.35f to Color.White.copy(alpha = 0.55f * i),
            0.65f to Color(0xFFFF59B3).copy(alpha = 0.28f * i),
            0.85f to Color(0xFFFFD466).copy(alpha = 0.20f * i),
            1.0f to Color.Transparent
        )
    }
}

/**
 * Applies a 2026 optical prism chromatic dispersion border to any composable.
 */
fun Modifier.prismDispersionBorder(
    state: LiquidGlassState,
    shape: Shape,
    borderWidth: Dp = 1.2.dp,
    angleDeg: Float = 55f
): Modifier = composed {
    val tier = state.effectiveTier
    if (tier == GlassQualityTier.PERFORMANCE_FALLBACK) {
        // Zero-allocation fallback: standard clean border
        this.border(
            width = borderWidth,
            color = Color.White.copy(alpha = 0.35f),
            shape = shape
        )
    } else {
        val intensity = when (tier) {
            GlassQualityTier.ULTRA_LIQUID -> 1.0f * state.refractionIntensity
            else -> 0.70f
        }
        val brush = PrismDispersionBrush.chromaticBorderBrush(
            intensity = intensity,
            angleDeg = angleDeg,
            primaryAccent = state.themePreset.primaryAccent
        )
        this.border(
            width = borderWidth,
            brush = brush,
            shape = shape
        )
    }
}
