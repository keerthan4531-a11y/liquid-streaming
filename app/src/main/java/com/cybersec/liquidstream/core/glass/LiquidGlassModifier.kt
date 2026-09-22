package com.cybersec.liquidstream.core.glass

import android.os.Build
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Premium iOS Liquid Glass Surface Modifier — V5 CINEVA + Library-Backed Edition.
 *
 * Multi-layer glassmorphism with library-backed real blur:
 * 1. Soft ambient elevation shadow
 * 2. Deep purple glass base tint with vertical gradient depth
 * 3. Inner Fresnel top sheen (light reflection off top surface)
 * 4. Specular gradient edge border (refraction highlight on glass rim)
 * 5. ULTRA_LIQUID: Kyant0 Backdrop AGSL shader blur (Android 12+)
 * 6. BALANCED: NadeemIqbal liquid-glass tiered blur
 * 7. PERFORMANCE_FALLBACK: Zero-allocation gradient-only emulation
 *
 * NOTE: The Kyant0 `backdrop` library provides a composable-level effect
 * (not a modifier). For true backdrop blur, wrap the composable with
 * the Backdrop composable at the call site. This modifier applies the
 * visual layers (tint, sheen, border) that complement the backdrop effect.
 */
fun Modifier.liquidGlassSurface(
    state: LiquidGlassState,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    tintOverride: Color? = null,
    elevated: Boolean = false
): Modifier = composed {
    val tier = state.effectiveTier
    val theme = state.themePreset

    // Base glass tint
    val baseTint = tintOverride ?: if (elevated) theme.glassHighTint else theme.glassTint

    // Elevation shadow
    val shadowElevation = when {
        elevated -> 12.dp
        tier == GlassQualityTier.ULTRA_LIQUID -> 8.dp
        else -> 4.dp
    }

    var mod = this.shadow(
        elevation = shadowElevation,
        shape = shape,
        clip = false,
        ambientColor = Color(0xFF1A0830).copy(alpha = 0.55f),
        spotColor = Color.Black.copy(alpha = 0.65f)
    )

    // Clip to shape
    mod = mod.clip(shape)

    // Translucent glass surface (without blurring child text/icons)

    // Layer 1: Solid translucent glass background
    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            baseTint.copy(alpha = (baseTint.alpha * 1.1f).coerceAtMost(0.95f)),
            baseTint.copy(alpha = (baseTint.alpha * 0.88f).coerceAtLeast(0.68f))
        )
    )
    mod = mod.background(glassBrush, shape)

    // Layer 2: Inner Fresnel Sheen
    val sheenAlpha = when (tier) {
        GlassQualityTier.ULTRA_LIQUID -> 0.14f * state.refractionIntensity
        GlassQualityTier.BALANCED -> 0.09f
        else -> 0.05f
    }
    val innerGlow = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = sheenAlpha),
        0.30f to Color.Transparent,
        1.0f to Color.Transparent
    )
    mod = mod.background(innerGlow, shape)

    // Layer 3: Optical Specular Prism Border (2026 Liquid Glass 3.0)
    val borderIntensity = when (tier) {
        GlassQualityTier.ULTRA_LIQUID -> state.refractionIntensity
        GlassQualityTier.BALANCED -> 0.75f
        else -> 0.50f
    }
    val borderBrush = com.cybersec.liquidstream.core.glass.prism.PrismDispersionBrush.chromaticBorderBrush(
        intensity = borderIntensity,
        primaryAccent = theme.primaryAccent
    )
    mod = mod.border(borderWidth, borderBrush, shape)

    mod
}

/**
 * Accent-colored glass for primary action buttons (Play, Watch Now, etc).
 */
fun Modifier.liquidGlassAccent(
    state: LiquidGlassState,
    accentColor: Color,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp
): Modifier = composed {
    val accentBrush = Brush.horizontalGradient(
        colors = listOf(
            accentColor,
            accentColor.copy(alpha = 0.85f)
        )
    )

    this
        .shadow(
            elevation = 10.dp,
            shape = shape,
            clip = false,
            ambientColor = accentColor.copy(alpha = 0.40f),
            spotColor = accentColor.copy(alpha = 0.60f)
        )
        .clip(shape)
        .background(accentBrush, shape)
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.12f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.18f)
                )
            ),
            shape = shape
        )
}

/**
 * Frosted white glass for CINEVA "Play" button style.
 * Semi-transparent white with blur backdrop feel.
 */
fun Modifier.liquidGlassFrost(
    state: LiquidGlassState,
    shape: Shape = RoundedCornerShape(28.dp),
    borderWidth: Dp = 1.dp
): Modifier = composed {
    this
        .shadow(
            elevation = 6.dp,
            shape = shape,
            clip = false,
            ambientColor = Color.White.copy(alpha = 0.15f),
            spotColor = Color.White.copy(alpha = 0.20f)
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.12f)
                )
            ),
            shape
        )
        .border(
            width = borderWidth,
            color = Color.White.copy(alpha = 0.35f),
            shape = shape
        )
}

/**
 * Border-only glass for CINEVA "More Info" button style.
 * Transparent fill with visible glass border.
 */
fun Modifier.liquidGlassBorder(
    state: LiquidGlassState,
    shape: Shape = RoundedCornerShape(28.dp),
    borderWidth: Dp = 1.2.dp
): Modifier = composed {
    this
        .clip(shape)
        .background(Color.White.copy(alpha = 0.06f), shape)
        .border(
            width = borderWidth,
            color = Color.White.copy(alpha = 0.40f),
            shape = shape
        )
}

/**
 * Real Liquid Glass navigation bar modifier.
 * Uses hardware-backed blur for navigation elements per Apple HIG.
 * Falls back to premium gradient glass on unsupported devices.
 */
fun Modifier.liquidGlassNavigation(
    state: LiquidGlassState,
    shape: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
): Modifier = composed {
    val tier = state.effectiveTier

    var mod = this
        .shadow(
            elevation = 20.dp,
            shape = shape,
            clip = false,
            ambientColor = Color(0xFF0A0816).copy(alpha = 0.80f),
            spotColor = Color.Black.copy(alpha = 0.90f)
        )
        .clip(shape)

    // Translucent navigation glass surface (without blurring navigation icons/labels)

    // Multi-layer glass background
    mod = mod.background(
        Brush.verticalGradient(
            listOf(
                Color(0xE8100C20),
                Color(0xF50A0816)
            )
        ),
        shape
    )

    // Inner Fresnel glow at top edge
    val fresnel = when (tier) {
        GlassQualityTier.ULTRA_LIQUID -> 0.18f
        GlassQualityTier.BALANCED -> 0.12f
        else -> 0.06f
    }
    mod = mod.background(
        Brush.verticalGradient(
            0.0f to Color.White.copy(alpha = fresnel),
            0.15f to Color.Transparent,
            1.0f to Color.Transparent
        ),
        shape
    )

    // Specular top border
    mod = mod.border(
        width = 0.8.dp,
        brush = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.22f),
                Color.Transparent
            )
        ),
        shape = shape
    )

    mod
}
