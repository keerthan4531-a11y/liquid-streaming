package com.cybersec.liquidstream.core.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.cybersec.liquidstream.core.glass.GlassQualityTier

object DeviceUtils {

    /**
     * Checks if the current phone is considered a low-end or low-RAM device.
     */
    fun isLowRamDevice(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return true
        return am.isLowRamDevice || getTotalMemoryGb(context) < 3.5f
    }

    /**
     * Returns total device RAM in Gigabytes.
     */
    fun getTotalMemoryGb(context: Context): Float {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 2.0f
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return memInfo.totalMem.toFloat() / (1024f * 1024f * 1024f)
    }

    /**
     * Evaluates hardware to recommend the optimal Liquid Glass quality tier.
     */
    fun getRecommendedQualityTier(context: Context): GlassQualityTier {
        val totalRamGb = getTotalMemoryGb(context)
        val isLowRam = isLowRamDevice(context)
        val api = Build.VERSION.SDK_INT

        return when {
            // Android 12+ (API 31+) with 6GB+ RAM and NOT low-RAM device
            api >= Build.VERSION_CODES.S && totalRamGb >= 5.5f && !isLowRam -> {
                GlassQualityTier.ULTRA_LIQUID
            }
            // Android 10+ (API 29+) with 4GB+ RAM
            api >= Build.VERSION_CODES.Q && totalRamGb >= 3.5f && !isLowRam -> {
                GlassQualityTier.BALANCED
            }
            // Low-RAM or older devices: use Zero-Allocation fallback
            else -> {
                GlassQualityTier.PERFORMANCE_FALLBACK
            }
        }
    }
}
