package com.cybersec.liquidstream.core.glass.halo

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cybersec.liquidstream.core.glass.GlassQualityTier
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.data.model.Movie

/**
 * 2026 Dynamic Poster Color Absorption & Ambient Halo Engine.
 *
 * Casts a soft, movie-specific atmospheric glow beneath the glass card
 * based on the dominant aesthetic of the content.
 */
object AmbientHaloEngine {

    /**
     * Resolves a signature ambient halo color based on movie genre and title.
     */
    fun resolveMovieHaloColor(movie: Movie, defaultAccent: Color): Color {
        val g = movie.genre.lowercase()
        val t = movie.title.lowercase()
        return when {
            g.contains("sci-fi") || g.contains("fiction") || t.contains("interstellar") -> Color(0xFF06B6D4) // Cyan Halo
            g.contains("action") || t.contains("pushpa") || t.contains("blade") -> Color(0xFFE11D48)       // Crimson Halo
            g.contains("adventure") || t.contains("dune") -> Color(0xFFF59E0B)                             // Desert Amber Halo
            g.contains("drama") || t.contains("oppenheimer") -> Color(0xFF8B5CF6)                          // Deep Violet Halo
            g.contains("thriller") || g.contains("mystery") -> Color(0xFF3B82F6)                           // Electric Blue Halo
            g.contains("comedy") || g.contains("family") || g.contains("animation") -> Color(0xFF10B981) // Emerald Halo
            else -> defaultAccent
        }
    }
}

/**
 * Applies a cinematic colored ambient halo behind a card or poster.
 */
fun Modifier.ambientPosterHalo(
    state: LiquidGlassState,
    haloColor: Color,
    shape: Shape,
    elevation: Dp = 12.dp,
    intensity: Float = 0.55f
): Modifier = composed {
    val tier = state.effectiveTier
    if (tier == GlassQualityTier.PERFORMANCE_FALLBACK) {
        // Zero-allocation fallback: standard lightweight shadow
        this.shadow(
            elevation = 4.dp,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.45f),
            spotColor = Color.Black.copy(alpha = 0.60f)
        )
    } else {
        val alphaMultiplier = if (tier == GlassQualityTier.ULTRA_LIQUID) 1.0f else 0.70f
        this.shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = haloColor.copy(alpha = (intensity * 0.50f * alphaMultiplier).coerceIn(0f, 1f)),
            spotColor = haloColor.copy(alpha = (intensity * 0.75f * alphaMultiplier).coerceIn(0f, 1f))
        )
    }
}
