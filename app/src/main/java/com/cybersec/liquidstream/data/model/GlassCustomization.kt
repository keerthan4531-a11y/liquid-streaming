package com.cybersec.liquidstream.data.model

import com.cybersec.liquidstream.core.glass.GlassQualityTier
import com.cybersec.liquidstream.core.glass.GlassThemePreset

data class GlassCustomization(
    val tier: GlassQualityTier = GlassQualityTier.BALANCED,
    val themePreset: GlassThemePreset = GlassThemePreset.CINEVA_PURPLE,
    val blurRadiusDp: Float = 20f,
    val refractionIntensity: Float = 1.0f,
    val isOledBlackEnabled: Boolean = true,
    val hardwareAcceleratedDecoders: Boolean = true
)
