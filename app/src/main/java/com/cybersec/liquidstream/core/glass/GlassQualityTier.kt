package com.cybersec.liquidstream.core.glass

/**
 * Quality tiers for iOS Liquid Glass effects.
 * Determines rendering complexity based on device capabilities.
 */
enum class GlassQualityTier(
    val displayName: String,
    val description: String
) {
    /** Full Backdrop blur + lens refraction + vibrancy — flagship devices */
    ULTRA_LIQUID(
        "Ultra Liquid Glass",
        "Full Backdrop blur + lens refraction + vibrancy (Flagship)"
    ),

    /** Backdrop blur only (no lens/vibrancy) — mid-range devices */
    BALANCED(
        "Balanced Glass",
        "Backdrop blur + standard specular reflection (Mid-range)"
    ),

    /** Fake glass (gradient overlays, zero allocation, no GPU blur) — low-end/old devices */
    PERFORMANCE_FALLBACK(
        "Zero-Allocation Fallback",
        "Optimized gradient-based glass emulation (Low-end / Battery saver)"
    );

    companion object {
        val PERFORMANCE get() = PERFORMANCE_FALLBACK
        val AUTO get() = BALANCED
    }
}
