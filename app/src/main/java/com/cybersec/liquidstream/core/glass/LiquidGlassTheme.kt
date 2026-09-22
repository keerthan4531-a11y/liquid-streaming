package com.cybersec.liquidstream.core.glass

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

/**
 * CINEVA-Style iOS Liquid Glass Theme — V4 Premium Purple-Navy Palette.
 *
 * Deep space purple background with violet accent system.
 * All glass tints are purple-shifted for cohesive glassmorphism.
 * Includes light mode variants per Apple HIG for system adaptation.
 */
enum class GlassThemePreset(
    val displayName: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val glassTint: Color,
    val glassHighTint: Color,
    // Light mode variants (Apple HIG)
    val lightGlassTint: Color,
    val lightBackground: Color
) {
    CINEVA_PURPLE(
        "CINEVA Purple",
        Color(0xFF8B5CF6),
        Color(0xFFA855F7),
        Color(0xD00C0818),
        Color(0xB8120E22),
        Color(0x60FFFFFF),
        Color(0xFFF5F0FF)
    ),
    OBSIDIAN_NIGHT(
        "Obsidian Night",
        Color(0xFF38BDF8),
        Color(0xFF7DD3FC),
        Color(0xD00A0E1A),
        Color(0xB8101828),
        Color(0x60F0F7FF),
        Color(0xFFF0F7FF)
    ),
    CYBER_NEON(
        "Cyber Neon",
        Color(0xFFFF2D55),
        Color(0xFFFF6B8A),
        Color(0xD01A0A10),
        Color(0xB8220E16),
        Color(0x60FFF0F3),
        Color(0xFFFFF0F3)
    ),
    EMERALD_GLASS(
        "Emerald Glass",
        Color(0xFF10B981),
        Color(0xFF6EE7B7),
        Color(0xD0061A12),
        Color(0xB80A2218),
        Color(0x60F0FFF8),
        Color(0xFFF0FFF8)
    ),
    AMETHYST(
        "Amethyst",
        Color(0xFFA855F7),
        Color(0xFFC084FC),
        Color(0xD0120A22),
        Color(0xB81A1030),
        Color(0x60F8F0FF),
        Color(0xFFF8F0FF)
    );

    companion object {
        val CRYSTAL_CLEAR get() = CINEVA_PURPLE
    }
}

object LiquidGlassColors {
    // Core background colors — Deep space purple (CINEVA Dark Mode)
    val Background = Color(0xFF0A0816)
    val Surface = Color(0xFF120E22)
    val SurfaceElevated = Color(0xFF1A1430)
    val CardSurface = Color(0xFF1E1638)

    // Light mode backgrounds (Apple HIG)
    val LightBackground = Color(0xFFF8F6FF)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceElevated = Color(0xFFF0EDFA)
    val LightCardSurface = Color(0xFFEDE8F8)

    // Text colors — Dark Mode
    val TextPrimary = Color(0xFFF0F0FA)
    val TextSecondary = Color(0xB0E8E0F8)
    val TextTertiary = Color(0x70D0C8E0)

    // Text colors — Light Mode
    val LightTextPrimary = Color(0xFF1A1A2E)
    val LightTextSecondary = Color(0xFF4A4A6A)
    val LightTextTertiary = Color(0xFF8A8AAA)

    // Glass surface colors — purple-shifted translucent
    val GlassSurfaceStrong = Color(0xD8100C20)
    val GlassSurfaceMedium = Color(0xC014102A)
    val GlassSurfaceLight = Color(0xA01A1434)

    // Light mode glass surfaces
    val LightGlassSurfaceStrong = Color(0xD8FFFFFF)
    val LightGlassSurfaceMedium = Color(0xC0FFFFFF)
    val LightGlassSurfaceLight = Color(0xA0FFFFFF)

    // Specular (glass edge light refraction)
    fun specularBorderBrush(intensity: Float = 1.0f): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.50f * intensity),
                Color.White.copy(alpha = 0.10f * intensity),
                Color.White.copy(alpha = 0.03f * intensity),
                Color.White.copy(alpha = 0.18f * intensity)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    fun accentGlow(accent: Color): Brush {
        return Brush.radialGradient(
            colors = listOf(
                accent.copy(alpha = 0.18f),
                Color.Transparent
            )
        )
    }
}
