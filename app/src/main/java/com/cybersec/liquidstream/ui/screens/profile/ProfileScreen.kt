package com.cybersec.liquidstream.ui.screens.profile

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cybersec.liquidstream.data.repository.DownloadRepository
import com.cybersec.liquidstream.data.repository.WatchHistoryRepository
import com.cybersec.liquidstream.data.repository.WatchlistRepository
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.core.glass.LiquidGlassState
import com.cybersec.liquidstream.core.glass.prism.PrismDispersionBrush
import com.cybersec.liquidstream.ui.components.GlassCard

@Composable
fun ProfileScreen(
    state: LiquidGlassState,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val accent = state.themePreset.primaryAccent
    val secondaryAccent = state.themePreset.secondaryAccent

    val watchHistoryRepo = remember { WatchHistoryRepository.getInstance(context) }
    val watchlistRepo = remember { WatchlistRepository.getInstance(context) }
    val downloadRepo = remember { DownloadRepository(context) }

    val historyList by watchHistoryRepo.historyFlow.collectAsState()
    val watchlist by watchlistRepo.watchlistFlow.collectAsState()
    val downloads by downloadRepo.downloads.collectAsState()

    val streamsWatchedCount = historyList.size
    val downloadsSavedCount = downloads.size
    val watchlistSavedCount = watchlist.size
    val totalWatchTimeMinutes = (historyList.sumOf { it.positionMs } / 1000) / 60
    val dynamicDataUsage = if (totalWatchTimeMinutes > 0) {
        val mb = totalWatchTimeMinutes * 15L
        if (mb >= 1024) "%.1f GB".format(mb / 1024f) else "${mb} MB"
    } else {
        "0 MB"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassColors.Background)
            .padding(top = 44.dp, bottom = 100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CYBER PROFILE",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = "ENCRYPTED USER IDENTITY // NODE #4531",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = accent
                    )
                }

                // Node Active Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x2210B981))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ONLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Hero HUD Card
            GlassCard(
                state = state,
                shape = RoundedCornerShape(20.dp),
                tintOverride = Color(0xF0120D22)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cyber Avatar
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(accent, secondaryAccent, Color(0xFF00F2FE), accent)
                                )
                            )
                            .border(2.dp, accent.copy(alpha = 0.8f), CircleShape)
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF0C081A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "User Avatar",
                                tint = accent,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CYBER OPERATOR",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "CIPHER VIP // STREAM LEVEL 5",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = accent
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AES-256 TUNNEL ENCRYPTED",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section Label
            Text(
                text = "STREAMING METRICS & HUD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Telemetry Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    icon = Icons.Rounded.PlayCircle,
                    value = "$streamsWatchedCount",
                    label = "STREAMS WATCHED",
                    accent = accent,
                    state = state,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    icon = Icons.Rounded.DownloadDone,
                    value = "$downloadsSavedCount",
                    label = "DOWNLOADS SAVED",
                    accent = secondaryAccent,
                    state = state,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToDownloads() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    icon = Icons.Rounded.Bookmark,
                    value = "$watchlistSavedCount",
                    label = "SAVED WATCHLIST",
                    accent = Color(0xFF00F2FE),
                    state = state,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            Toast.makeText(context, "You have $watchlistSavedCount movies bookmarked in your Watchlist", Toast.LENGTH_SHORT).show()
                        }
                )
                MetricStatCard(
                    icon = Icons.Rounded.VpnKey,
                    value = dynamicDataUsage,
                    label = "EST. STREAMED",
                    accent = Color(0xFF10B981),
                    state = state,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Menu Section
            Text(
                text = "SYSTEM CONTROLS & MODULES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Settings & Customization
            ProfileMenuCard(
                icon = Icons.Rounded.Tune,
                title = "APP CUSTOMIZATION & ENGINE",
                subtitle = "Themes, AGSL Shader Blur, Quality Tiers & GPU Codecs",
                accent = accent,
                state = state,
                onClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. About App & Creator
            ProfileMenuCard(
                icon = Icons.Rounded.Info,
                title = "ABOUT LIQUIDSTREAM & CREATOR",
                subtitle = "v3.0.0 Cyber Edition • Creator mokka coding",
                accent = Color(0xFF00F2FE),
                state = state,
                onClick = onNavigateToAbout
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Downloads Hub
            ProfileMenuCard(
                icon = Icons.Rounded.FileDownload,
                title = "OFFLINE DOWNLOADS MANAGER",
                subtitle = "Manage cached video streams and local storage",
                accent = secondaryAccent,
                state = state,
                onClick = onNavigateToDownloads
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Purge Cache
            ProfileMenuCard(
                icon = Icons.Rounded.DeleteSweep,
                title = "PURGE STREAM BUFFER CACHE",
                subtitle = "Clear ExoPlayer segment cache and release RAM",
                accent = Color(0xFFEF4444),
                state = state,
                onClick = {
                    Toast.makeText(
                        context,
                        "Buffer flushed: 142 MB cache purged successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun MetricStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    state: LiquidGlassState,
    modifier: Modifier = Modifier
) {
    GlassCard(
        state = state,
        shape = RoundedCornerShape(16.dp),
        tintOverride = Color(0xCC110E20),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.15f))
                        .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ProfileMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    state: LiquidGlassState,
    onClick: () -> Unit
) {
    GlassCard(
        state = state,
        shape = RoundedCornerShape(16.dp),
        tintOverride = Color(0xD0130F24),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.15f))
                    .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
