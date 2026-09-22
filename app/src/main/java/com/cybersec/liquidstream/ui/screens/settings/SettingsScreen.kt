package com.cybersec.liquidstream.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.GlassQualityTier
import com.cybersec.liquidstream.core.glass.GlassThemePreset
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.ui.components.GlassCard

@Composable
fun SettingsScreen(
    state: LiquidGlassState,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var hwDecoding by remember { mutableStateOf(true) }
    var antiTracking by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
            .padding(top = 40.dp, bottom = 100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x25FFFFFF))
                            .border(1.dp, state.themePreset.primaryAccent.copy(alpha = 0.5f), CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column {
                    Text(
                        text = "Studio & Settings",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Liquid Glass Shader Engine & System Controls",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Diagnostic Box
            GlassCard(
                state = state,
                shape = RoundedCornerShape(16.dp),
                tintOverride = Color(0xF0120E22)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = state.themePreset.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HARDWARE CAPABILITY BENCHMARK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = state.themePreset.primaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "System RAM: ${String.format("%.1f", state.totalRamGb)} GB | Low-RAM Mode: ${if (state.isLowRamDevice) "ACTIVE" else "DISABLED"}",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Auto-Engine Mode: ${state.effectiveTier.displayName}",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quality Tiers
            Text(
                text = "Rendering Quality Engine",
                fontSize = 16.sp,
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) state.themePreset.primaryAccent.copy(alpha = 0.18f) else Color(0x18FFFFFF))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) state.themePreset.primaryAccent else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { state.qualityTier = tier }
                        .padding(14.dp),
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Theme Presets
            Text(
                text = "Liquid Glass Themes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Sliders
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Refraction Specular Highlight", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Video Engine Toggles
            Text(
                text = "Cyber Security & Video Engine",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(state = state, shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Zero-Ad Anti-Tracking", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Bypasses all ad popunders & redirect traps", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = antiTracking,
                            onCheckedChange = { antiTracking = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = state.themePreset.primaryAccent)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hardware Codec Acceleration", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Use GPU hardware decoders for 4K/1080p playback", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = hwDecoding,
                            onCheckedChange = { hwDecoding = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = state.themePreset.primaryAccent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
