package com.cybersec.liquidstream.core.glass

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cybersec.liquidstream.core.util.DeviceUtils

/**
 * Centralized state for iOS Liquid Glass rendering.
 * Manages theme preset, quality tier, blur intensity, device capability detection,
 * and dark/light mode adaptation per Apple HIG.
 */
@Stable
class LiquidGlassState(context: Context) {
    var themePreset by mutableStateOf(GlassThemePreset.CINEVA_PURPLE)
    var refractionIntensity by mutableFloatStateOf(1.0f)
    var blurRadius: Dp by mutableStateOf(20.dp)

    /** Manual override for quality tier; null means auto-detect based on hardware */
    var qualityTier: GlassQualityTier? by mutableStateOf(null)

    val totalRamGb: Float = DeviceUtils.getTotalMemoryGb(context)
    val isLowRamDevice: Boolean = DeviceUtils.isLowRamDevice(context)
    val recommendedTier: GlassQualityTier = DeviceUtils.getRecommendedQualityTier(context)

    /** Whether real backdrop blur is supported on this device */
    var backdropEnabled by mutableStateOf(true)

    /** Dark/Light mode state — defaults to dark (CINEVA default) */
    var isDarkMode by mutableStateOf(
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                || true // Default to dark for CINEVA theme
    )

    /** Whether Kyant0 Backdrop library shader is available (Android 12+ / API 31+) */
    val isBackdropShaderAvailable: Boolean = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S

    val effectiveTier: GlassQualityTier
        get() {
            qualityTier?.let { return it }
            if (!backdropEnabled) return GlassQualityTier.PERFORMANCE_FALLBACK
            return recommendedTier
        }
}

@Composable
fun rememberLiquidGlassState(): LiquidGlassState {
    val context = LocalContext.current
    return remember { LiquidGlassState(context) }
}
