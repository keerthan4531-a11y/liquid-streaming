package com.cybersec.liquidstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.GlassQualityTier
import com.cybersec.liquidstream.core.glass.GlassThemePreset
import com.cybersec.liquidstream.core.glass.LiquidGlassState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationSheet(
    state: LiquidGlassState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF10121A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "iOS Liquid Glass Studio",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Live shader tuning & adaptive performance degradation",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware diagnostics card
            GlassCard(
                state = state,
                shape = RoundedCornerShape(14.dp),
                tintOverride = Color(0xF0120E22)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HARDWARE ENGINE BENCHMARK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = state.themePreset.primaryAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total System RAM: ${String.format("%.1f", state.totalRamGb)} GB | Low-RAM Mode: ${if (state.isLowRamDevice) "YES" else "NO"}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Active Tier: ${state.effectiveTier.displayName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quality Tiers
            Text(
                text = "Rendering Quality Tier",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassQualityTier.values().forEach { tier ->
                val isSelected = state.qualityTier == tier
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) state.themePreset.primaryAccent.copy(alpha = 0.18f) else Color(0x18FFFFFF))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) state.themePreset.primaryAccent else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { state.qualityTier = tier }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .background(if (isSelected) state.themePreset.primaryAccent else Color.Transparent)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tier.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = tier.description,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Theme Presets
            Text(
                text = "Liquid Glass Color Themes",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassThemePreset.values().forEach { preset ->
                    val isSelected = state.themePreset == preset
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) preset.primaryAccent.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) preset.primaryAccent else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { state.themePreset = preset }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(preset.primaryAccent)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset.displayName.substringBefore(" "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Blur Radius Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Backdrop Blur Radius", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("${state.blurRadius.value.toInt()} dp", fontSize = 13.sp, color = state.themePreset.primaryAccent)
            }
            Slider(
                value = state.blurRadius.value,
                onValueChange = { state.blurRadius = it.dp },
                valueRange = 0f..40f,
                colors = SliderDefaults.colors(
                    thumbColor = state.themePreset.primaryAccent,
                    activeTrackColor = state.themePreset.primaryAccent
                )
            )

            // Refraction Intensity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Specular Refraction Intensity", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("${(state.refractionIntensity * 100).toInt()}%", fontSize = 13.sp, color = state.themePreset.primaryAccent)
            }
            Slider(
                value = state.refractionIntensity,
                onValueChange = { state.refractionIntensity = it },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = state.themePreset.primaryAccent,
                    activeTrackColor = state.themePreset.primaryAccent
                )
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
